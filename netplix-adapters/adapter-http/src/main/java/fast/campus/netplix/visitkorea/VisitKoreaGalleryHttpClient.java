package fast.campus.netplix.visitkorea;

import fast.campus.netplix.client.HttpClient;
import fast.campus.netplix.tour.TourGallery;
import fast.campus.netplix.tour.TourGalleryPort;
import fast.campus.netplix.util.ObjectMapperUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 한국관광공사 관광사진갤러리(PhotoGalleryService1) HTTP 어댑터.
 *
 * <p>전략:
 * - 키워드 없는 전체 목록은 한 번 로딩 후 24h in-memory 캐시.
 * - 키워드 검색은 키워드별 개별 캐시(최대 64개, LRU 근사치).
 * - serviceKey 미설정 또는 Forbidden 시 빈 리스트 반환 → UI 섹션 자연 숨김.
 * - 일일 쿼터 1,000회 고려해 호출 폭주 방지.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisitKoreaGalleryHttpClient implements TourGalleryPort {

    private static final int MAX_PAGE_SIZE = 200;
    private static final String ALL_KEY = "__ALL__";
    private static final int MAX_KEYWORD_CACHE = 64;
    // 안전 상한 — 현재 galleryList1 totalCount ≈ 6,100 장. 50 페이지(= 10,000장) 가드로
    // 공급자가 급격히 데이터를 늘려도 쿼터 폭주 방지.
    private static final int MAX_PAGES_ALL = 50;
    // 키워드 검색은 보통 훨씬 적음. 넉넉히 20 페이지(= 4,000장) 가드.
    private static final int MAX_PAGES_KEYWORD = 20;

    private final HttpClient httpClient;

    @Value("${visitkorea.gallery.api-key:}")
    private String serviceKey;

    @Value("${visitkorea.gallery.list-url:https://apis.data.go.kr/B551011/PhotoGalleryService1/galleryList1}")
    private String listUrl;

    @Value("${visitkorea.gallery.search-url:https://apis.data.go.kr/B551011/PhotoGalleryService1/gallerySearchList1}")
    private String searchUrl;

    @Value("${visitkorea.gallery.cache-minutes:1440}")
    private long cacheMinutes;

    private final Map<String, CacheSnapshot> cache = new ConcurrentHashMap<>();

    @Override
    public boolean isConfigured() {
        return serviceKey != null && !serviceKey.isBlank()
                && listUrl != null && !listUrl.isBlank();
    }

    @PostConstruct
    void prewarm() {
        if (!isConfigured()) {
            log.info("[GALLERY] serviceKey/URL 미설정 - 프리워밍 생략");
            return;
        }
        try {
            refreshAll();
            log.info("[GALLERY] 프리워밍 완료 - {} 건 로드", getSnapshot(ALL_KEY).photos.size());
        } catch (Exception ex) {
            log.warn("[GALLERY] 프리워밍 실패 (다음 호출에서 재시도): {}", ex.getMessage());
        }
    }

    @Override
    public List<TourGallery> fetchAll(int limit) {
        if (!isConfigured()) return List.of();
        CacheSnapshot snap = getSnapshot(ALL_KEY);
        if (isStale(snap)) {
            try { refreshAll(); } catch (Exception ex) {
                log.warn("[GALLERY] 전체 갱신 실패 (stale 반환): {}", ex.getMessage());
            }
            snap = getSnapshot(ALL_KEY);
        }
        return take(snap.photos, limit);
    }

    @Override
    public List<TourGallery> fetchByKeyword(String keyword, int limit) {
        if (!isConfigured()) return List.of();
        if (keyword == null || keyword.isBlank()) return fetchAll(limit);
        String key = keyword.trim().toLowerCase(Locale.ROOT);

        CacheSnapshot snap = cache.get(key);
        if (snap == null || isStale(snap)) {
            try {
                refreshByKeyword(keyword.trim(), key);
            } catch (Exception ex) {
                log.warn("[GALLERY] 키워드 검색 실패 keyword={} err={}", keyword, ex.getMessage());
                if (snap == null) return List.of();
            }
            snap = cache.getOrDefault(key, snap);
        }
        if (snap == null) return List.of();
        return take(snap.photos, limit);
    }

    private synchronized void refreshAll() {
        List<TourGallery> photos = requestAllPages(listUrl, Map.of(), MAX_PAGES_ALL);
        cache.put(ALL_KEY, new CacheSnapshot(photos, Instant.now().toEpochMilli()));
        log.info("[GALLERY] 전체 캐시 갱신 - {} 건", photos.size());
    }

    private synchronized void refreshByKeyword(String keyword, String cacheKey) {
        List<TourGallery> photos = requestAllPages(searchUrl, Map.of("keyword", keyword), MAX_PAGES_KEYWORD);
        if (cache.size() >= MAX_KEYWORD_CACHE) {
            // 가장 오래된 항목 1개 제거 (근사치 LRU)
            cache.entrySet().stream()
                    .filter(e -> !e.getKey().equals(ALL_KEY))
                    .min((a, b) -> Long.compare(a.getValue().loadedAtEpochMs, b.getValue().loadedAtEpochMs))
                    .ifPresent(e -> cache.remove(e.getKey()));
        }
        cache.put(cacheKey, new CacheSnapshot(photos, Instant.now().toEpochMilli()));
        log.info("[GALLERY] 키워드={} 캐시 갱신 - {} 건", keyword, photos.size());
    }

    /**
     * totalCount 기반으로 페이지를 전부 순회해 한 번에 적재한다.
     * - 1페이지를 먼저 받아 totalCount 를 확인하고 남은 페이지를 순차 호출.
     * - 응답이 이상하거나 중간 실패 시 지금까지 모은 것만 반환(복구 가능성 유지).
     * - 일일 쿼터 1,000회 고려: 전체(~31페이지) + 키워드 수 개 수준 → 넉넉.
     */
    private List<TourGallery> requestAllPages(String baseUrl, Map<String, String> extraParams, int maxPages) {
        PageResult first = requestPage(baseUrl, extraParams, 1);
        if (first == null) return List.of();

        List<TourGallery> acc = new ArrayList<>(first.items);
        int totalCount = first.totalCount;
        if (totalCount <= first.items.size()) {
            return Collections.unmodifiableList(acc);
        }

        int totalPages = (int) Math.min(
                maxPages,
                (long) Math.ceil(totalCount / (double) MAX_PAGE_SIZE));

        for (int page = 2; page <= totalPages; page++) {
            PageResult pr = requestPage(baseUrl, extraParams, page);
            if (pr == null || pr.items.isEmpty()) {
                log.warn("[GALLERY] page={} 빈/실패 - 지금까지 {}건으로 마감", page, acc.size());
                break;
            }
            acc.addAll(pr.items);
        }
        log.info("[GALLERY] 페이지 순회 완료 - totalCount={} 로드={} (maxPages={})",
                totalCount, acc.size(), maxPages);
        return Collections.unmodifiableList(acc);
    }

    private PageResult requestPage(String baseUrl, Map<String, String> extraParams, int pageNo) {
        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append(baseUrl.contains("?") ? "&" : "?");
        sb.append("serviceKey=").append(serviceKey);
        sb.append("&_type=json");
        sb.append("&MobileOS=ETC");
        sb.append("&MobileApp=touraz-dvdholic");
        sb.append("&numOfRows=").append(MAX_PAGE_SIZE);
        sb.append("&pageNo=").append(pageNo);
        extraParams.forEach((k, v) -> sb.append('&').append(k).append('=')
                .append(URLEncoder.encode(v, StandardCharsets.UTF_8)));

        String raw;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.ACCEPT, "application/json");
            raw = httpClient.request(sb.toString(), HttpMethod.GET, headers, Map.of());
        } catch (Exception ex) {
            log.error("[GALLERY] 호출 실패 page={} err={}", pageNo, ex.getMessage());
            return null;
        }

        if (raw == null || raw.isBlank()) {
            log.warn("[GALLERY] 빈 응답 page={}", pageNo);
            return null;
        }

        // 서비스 키 미승인/쿼터 초과 시 비-JSON (XML / "Forbidden") 응답이 올 수 있음
        String trimmed = raw.trim();
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            log.warn("[GALLERY] 비-JSON 응답 (미승인/쿼터 초과 가능) page={} prefix={}",
                    pageNo, trimmed.substring(0, Math.min(80, trimmed.length())));
            return null;
        }

        VisitKoreaGalleryResponse parsed;
        try {
            String safe = trimmed.replace("\"items\":\"\"", "\"items\":null");
            parsed = ObjectMapperUtil.toObject(safe, VisitKoreaGalleryResponse.class);
        } catch (Exception ex) {
            log.error("[GALLERY] 파싱 실패 page={} err={}", pageNo, ex.getMessage());
            return null;
        }

        if (parsed == null || parsed.getResponse() == null
                || parsed.getResponse().getBody() == null) {
            return new PageResult(List.of(), 0);
        }

        VisitKoreaGalleryResponse.Body body = parsed.getResponse().getBody();
        int totalCount = body.getTotalCount() != null ? body.getTotalCount() : 0;

        if (body.getItems() == null || body.getItems().getItem() == null) {
            return new PageResult(List.of(), totalCount);
        }

        List<TourGallery> list = body.getItems().getItem().stream()
                .filter(i -> i.getGalWebImageUrl() != null && !i.getGalWebImageUrl().isBlank())
                .map(VisitKoreaGalleryHttpClient::toDomain)
                .collect(Collectors.toList());
        return new PageResult(list, totalCount);
    }

    private static TourGallery toDomain(VisitKoreaGalleryResponse.Item i) {
        String img = i.getGalWebImageUrl();
        return TourGallery.builder()
                .galContentId(i.getGalContentId())
                .galContentTypeId(i.getGalContentTypeId())
                .title(i.getGalTitle())
                .photographer(i.getGalPhotographer())
                .photoMonth(i.getGalPhotographyMonth())
                .photoLocation(i.getGalPhotographyLocation())
                .searchKeyword(i.getGalSearchKeyword())
                .imageUrl(img)
                .thumbnailUrl(img) // 별도 썸네일 필드가 없음 → 대형 이미지로 대체
                .createdTime(i.getCreatedtime())
                .modifiedTime(i.getModifiedtime())
                .build();
    }

    private CacheSnapshot getSnapshot(String key) {
        return cache.getOrDefault(key, CacheSnapshot.empty());
    }

    private boolean isStale(CacheSnapshot snap) {
        if (snap == null || snap.photos.isEmpty()) return true;
        long ageMin = (Instant.now().toEpochMilli() - snap.loadedAtEpochMs) / 60_000L;
        return ageMin >= cacheMinutes;
    }

    private static <T> List<T> take(List<T> list, int limit) {
        if (list == null) return List.of();
        if (limit <= 0 || limit >= list.size()) return list;
        return list.subList(0, limit);
    }

    private record CacheSnapshot(List<TourGallery> photos, long loadedAtEpochMs) {
        static CacheSnapshot empty() { return new CacheSnapshot(List.of(), 0L); }
    }

    /** 단일 페이지 응답 묶음 (items + 서버 측 totalCount). */
    private record PageResult(List<TourGallery> items, int totalCount) {}
}
