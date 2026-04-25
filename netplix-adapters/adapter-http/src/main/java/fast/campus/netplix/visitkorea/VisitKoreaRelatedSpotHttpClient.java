package fast.campus.netplix.visitkorea;

import fast.campus.netplix.client.HttpClient;
import fast.campus.netplix.tour.RelatedTouristSpot;
import fast.campus.netplix.tour.RelatedTouristSpotPort;
import fast.campus.netplix.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 한국관광공사 "관광지별 연관 관광지 정보"(TarRlteTarService1) 어댑터.
 *
 * <p>Base URL: {@code apis.data.go.kr/B551011/TarRlteTarService1}
 *
 * <p>일일 트래픽 1,000 회 / operation 의 KTO 쿼터를 고려해
 * (모드, 키) 단위로 6h(default) in-memory TTL 캐시를 유지한다.
 *
 * <p>응답 데이터는 좌표 정보가 없어 후속 결합이 필요할 때만 KorService2 등 다른 어댑터로 위임한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisitKoreaRelatedSpotHttpClient implements RelatedTouristSpotPort {

    private static final int MAX_PAGE_SIZE = 100;

    private final HttpClient httpClient;

    @Value("${visitkorea.related.service-key:${visitkorea.auth.service-key:}}")
    private String serviceKey;

    @Value("${visitkorea.related.area-based:}")
    private String areaBasedUrl;

    @Value("${visitkorea.related.search-keyword:}")
    private String searchKeywordUrl;

    @Value("${visitkorea.related.cache-minutes:360}")
    private long cacheMinutes;

    /** key = mode|param1|param2, value = (loadedAt, list) */
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Override
    public boolean isConfigured() {
        return serviceKey != null && !serviceKey.isBlank()
                && (notBlank(areaBasedUrl) || notBlank(searchKeywordUrl));
    }

    @Override
    public List<RelatedTouristSpot> fetchByKeyword(String keyword, int limit) {
        if (!isConfigured() || keyword == null || keyword.isBlank()) return List.of();
        if (!notBlank(searchKeywordUrl)) return List.of();
        String key = "kw|" + keyword.trim();
        List<RelatedTouristSpot> cached = readCache(key);
        if (cached != null) return take(cached, limit);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("keyword", keyword.trim());
        List<RelatedTouristSpot> items = callListApi(searchKeywordUrl, params);
        writeCache(key, items);
        return take(items, limit);
    }

    @Override
    public List<RelatedTouristSpot> fetchByArea(String areaCode, String signguCode, int limit) {
        if (!isConfigured()) return List.of();
        if (!notBlank(areaBasedUrl)) return List.of();
        if (areaCode == null || areaCode.isBlank()) return List.of();

        String key = "area|" + areaCode.trim() + "|" + (signguCode == null ? "" : signguCode.trim());
        List<RelatedTouristSpot> cached = readCache(key);
        if (cached != null) return take(cached, limit);

        Map<String, String> params = new LinkedHashMap<>();
        params.put("areaCd", areaCode.trim());
        if (signguCode != null && !signguCode.isBlank()) {
            params.put("signguCd", signguCode.trim());
        }
        List<RelatedTouristSpot> items = callListApi(areaBasedUrl, params);
        writeCache(key, items);
        return take(items, limit);
    }

    // -------------------- internal --------------------

    private List<RelatedTouristSpot> callListApi(String baseUrl, Map<String, String> extraParams) {
        String url = buildUrl(baseUrl, extraParams, MAX_PAGE_SIZE, 1);
        VisitKoreaRelatedSpotResponse parsed = fetchAndParse(url, baseUrl);
        if (parsed == null) return List.of();

        List<RelatedTouristSpot> result = new ArrayList<>();
        VisitKoreaRelatedSpotResponse.Items items = parsed.getResponse().getBody().getItems();
        if (items == null || items.getItem() == null) return List.of();
        for (VisitKoreaRelatedSpotResponse.Item i : items.getItem()) {
            result.add(toDomain(i));
        }
        return Collections.unmodifiableList(result);
    }

    private String buildUrl(String baseUrl, Map<String, String> extraParams, int numOfRows, int pageNo) {
        StringBuilder url = new StringBuilder(baseUrl);
        url.append(baseUrl.contains("?") ? "&" : "?")
                .append("serviceKey=").append(serviceKey)
                .append("&_type=json")
                .append("&MobileOS=ETC")
                .append("&MobileApp=touraz-dvdholic")
                .append("&numOfRows=").append(numOfRows)
                .append("&pageNo=").append(Math.max(1, pageNo));
        for (Map.Entry<String, String> e : extraParams.entrySet()) {
            if (e.getValue() == null || e.getValue().isBlank()) continue;
            url.append("&").append(e.getKey()).append("=")
                    .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
        }
        return url.toString();
    }

    private VisitKoreaRelatedSpotResponse fetchAndParse(String url, String baseUrl) {
        String raw;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.ACCEPT, "application/json");
            raw = httpClient.request(url, HttpMethod.GET, headers, Map.of());
        } catch (Exception ex) {
            log.warn("[KOR-RLT] 호출 실패 url={} err={}", baseUrl, ex.getMessage());
            return null;
        }
        if (raw == null || raw.isBlank()) return null;

        VisitKoreaRelatedSpotResponse parsed;
        try {
            String safe = raw.replace("\"items\":\"\"", "\"items\":null");
            parsed = ObjectMapperUtil.toObject(safe, VisitKoreaRelatedSpotResponse.class);
        } catch (Exception ex) {
            log.warn("[KOR-RLT] 파싱 실패 url={} err={} sample={}", baseUrl, ex.getMessage(),
                    raw.length() > 200 ? raw.substring(0, 200) : raw);
            return null;
        }

        if (parsed == null || parsed.getResponse() == null
                || parsed.getResponse().getBody() == null) {
            return null;
        }
        return parsed;
    }

    private static RelatedTouristSpot toDomain(VisitKoreaRelatedSpotResponse.Item i) {
        return RelatedTouristSpot.builder()
                .areaCode(i.getAreaCd())
                .areaName(i.getAreaNm())
                .signguCode(i.getSignguCd())
                .signguName(i.getSignguNm())
                .tAtsNm(i.getTAtsNm())
                .rlteTatsNm(i.getRlteTatsNm())
                .rlteRegnCd(i.getRlteRegnCd())
                .rlteRegnNm(i.getRlteRegnNm())
                .rlteSignguCd(i.getRlteSignguCd())
                .rlteSignguNm(i.getRlteSignguNm())
                .rlteCtgryLclsNm(i.getRlteCtgryLclsNm())
                .rlteCtgryMclsNm(i.getRlteCtgryMclsNm())
                .rlteCtgrySclsNm(i.getRlteCtgrySclsNm())
                .rlteRank(parseInt(i.getRlteRank()))
                .hashtags(i.getHashtags())
                .build();
    }

    private static Integer parseInt(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }

    private List<RelatedTouristSpot> readCache(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) return null;
        long now = Instant.now().toEpochMilli();
        if (now - entry.loadedAtMs > Duration.ofMinutes(cacheMinutes).toMillis()) {
            cache.remove(key);
            return null;
        }
        return entry.items;
    }

    private void writeCache(String key, List<RelatedTouristSpot> items) {
        cache.put(key, new CacheEntry(items, Instant.now().toEpochMilli()));
    }

    private static <T> List<T> take(List<T> list, int limit) {
        if (list == null || list.isEmpty()) return List.of();
        if (limit <= 0 || limit >= list.size()) return list;
        return list.subList(0, limit);
    }

    private record CacheEntry(List<RelatedTouristSpot> items, long loadedAtMs) {}
}
