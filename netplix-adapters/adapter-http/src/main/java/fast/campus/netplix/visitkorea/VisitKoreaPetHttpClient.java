package fast.campus.netplix.visitkorea;

import fast.campus.netplix.client.HttpClient;
import fast.campus.netplix.tour.PetFriendlyPoi;
import fast.campus.netplix.tour.PetFriendlyPoiPort;
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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 한국관광공사 반려동물 동반여행(KorPetTourService) HTTP 어댑터.
 *
 * <p>특징:
 * <ul>
 *   <li>areaBased/locationBased/searchKeyword 3종 + detailCommon2/detailPetTour2 2종 지원</li>
 *   <li>(areaCode + contentTypeId) 키 단위로 in-memory TTL 캐시 (기본 6h)</li>
 *   <li>totalCount=0 응답의 {@code items:""} 를 사전 치환하여 파싱 실패 방지</li>
 *   <li>serviceKey 미설정 또는 URL 미설정 시 모든 조회가 빈 리스트를 반환 (기동 계속)</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisitKoreaPetHttpClient implements PetFriendlyPoiPort {

    private static final int MAX_PAGE_SIZE = 50;

    private final HttpClient httpClient;

    @Value("${visitkorea.auth.service-key:}")
    private String serviceKey;

    @Value("${visitkorea.pet.area-based:}")
    private String areaBasedUrl;

    @Value("${visitkorea.pet.location-based:}")
    private String locationBasedUrl;

    @Value("${visitkorea.pet.search-keyword:}")
    private String searchKeywordUrl;

    @Value("${visitkorea.pet.detail-common:}")
    private String detailCommonUrl;

    @Value("${visitkorea.pet.detail-pet-tour:}")
    private String detailPetTourUrl;

    @Value("${visitkorea.pet.cache-minutes:360}")
    private long cacheMinutes;

    /** key = areaCode|contentTypeId, value = (loadedAt, list) */
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Override
    public boolean isConfigured() {
        return serviceKey != null && !serviceKey.isBlank()
                && areaBasedUrl != null && !areaBasedUrl.isBlank();
    }

    @Override
    public List<PetFriendlyPoi> fetchByArea(String areaCode, String contentTypeId, int limit) {
        if (!isConfigured()) return List.of();
        String key = (areaCode == null ? "_" : areaCode) + "|" + (contentTypeId == null ? "_" : contentTypeId);
        CacheEntry cached = cache.get(key);
        long now = Instant.now().toEpochMilli();
        if (cached != null && (now - cached.loadedAtMs) < Duration.ofMinutes(cacheMinutes).toMillis()) {
            return take(cached.items, limit);
        }
        List<PetFriendlyPoi> items = callListApi(areaBasedUrl, Map.of(
                "areaCode", nullSafe(areaCode),
                "contentTypeId", nullSafe(contentTypeId),
                "arrange", "Q"
        ));
        cache.put(key, new CacheEntry(items, now));
        return take(items, limit);
    }

    /**
     * KorPetTourService 상세 조회.
     * <ol>
     *   <li>{@code detailCommon2} 로 overview/홈페이지 등 공통 정보 조회</li>
     *   <li>{@code detailPetTour2} 로 반려동물 동반 정책 세부 병합</li>
     * </ol>
     */
    @Override
    public Optional<PetFriendlyPoi> fetchDetail(String contentId, String contentTypeId) {
        if (!isConfigured() || contentId == null || contentId.isBlank()) return Optional.empty();

        Optional<PetFriendlyPoi> common = Optional.empty();
        if (detailCommonUrl != null && !detailCommonUrl.isBlank()) {
            List<PetFriendlyPoi> items = callListApi(detailCommonUrl, Map.of(
                    "contentId", contentId,
                    "contentTypeId", nullSafe(contentTypeId)
            ));
            common = items.stream().findFirst();
        }

        PetDetailPacket pet = PetDetailPacket.EMPTY;
        if (detailPetTourUrl != null && !detailPetTourUrl.isBlank()) {
            pet = callPetDetailApi(detailPetTourUrl, Map.of(
                    "contentId", contentId,
                    "contentTypeId", nullSafe(contentTypeId)
            ));
        }

        if (common.isEmpty() && pet.isEmpty()) return Optional.empty();

        PetFriendlyPoi base = common.orElseGet(() -> PetFriendlyPoi.builder()
                .contentId(contentId)
                .contentTypeId(contentTypeId)
                .build());
        return Optional.of(base.toBuilder()
                .petAcceptance(pet.petAcceptance)
                .acmpyTypeCd(pet.acmpyTypeCd != null ? pet.acmpyTypeCd : base.getAcmpyTypeCd())
                .build());
    }

    @Override
    public List<PetFriendlyPoi> fetchByLocation(double mapX, double mapY, int radius,
                                                String contentTypeId, int limit) {
        if (!isConfigured()) return List.of();
        if (locationBasedUrl == null || locationBasedUrl.isBlank()) return List.of();
        List<PetFriendlyPoi> items = callListApi(locationBasedUrl, Map.of(
                "mapX", String.valueOf(mapX),
                "mapY", String.valueOf(mapY),
                "radius", String.valueOf(Math.max(100, Math.min(radius, 20000))),
                "contentTypeId", nullSafe(contentTypeId),
                "arrange", "E"
        ));
        return take(items, limit);
    }

    @Override
    public List<PetFriendlyPoi> fetchByKeyword(String keyword, String contentTypeId, int limit) {
        if (!isConfigured() || keyword == null || keyword.isBlank()) return List.of();
        if (searchKeywordUrl == null || searchKeywordUrl.isBlank()) return List.of();
        List<PetFriendlyPoi> items = callListApi(searchKeywordUrl, Map.of(
                "keyword", keyword,
                "contentTypeId", nullSafe(contentTypeId)
        ));
        return take(items, limit);
    }

    // -----------------------------
    // internal
    // -----------------------------

    private List<PetFriendlyPoi> callListApi(String baseUrl, Map<String, String> extraParams) {
        String url = buildUrl(baseUrl, extraParams);
        VisitKoreaPetResponse parsed = fetchAndParse(url, baseUrl);
        if (parsed == null) return List.of();

        List<PetFriendlyPoi> result = new ArrayList<>();
        for (VisitKoreaPetResponse.Item i : parsed.getResponse().getBody().getItems().getItem()) {
            result.add(toDomain(i));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * detailPetTour2 응답에서 반려동물 정책을 추출해 (라벨 맵 + acmpyTypeCd) 로 반환.
     */
    private PetDetailPacket callPetDetailApi(String baseUrl, Map<String, String> extraParams) {
        String url = buildUrl(baseUrl, extraParams);
        VisitKoreaPetResponse parsed = fetchAndParse(url, baseUrl);
        if (parsed == null) return PetDetailPacket.EMPTY;

        Map<String, String> acc = new LinkedHashMap<>();
        String acmpy = null;
        for (VisitKoreaPetResponse.Item i : parsed.getResponse().getBody().getItems().getItem()) {
            if (acmpy == null && i.getAcmpyTypeCd() != null && !i.getAcmpyTypeCd().isBlank()) {
                acmpy = i.getAcmpyTypeCd().trim();
            }
            putIfPresent(acc, "동반 가능한 반려동물", i.getAcmpyPsblCpam());
            putIfPresent(acc, "동반 시 유의사항", i.getAcmpyNeedMtr());
            putIfPresent(acc, "반려동물 에티켓", i.getEtiquetteMtr());
            putIfPresent(acc, "사고 방지 수칙", i.getRelaAcdntRiskMtr());
            putIfPresent(acc, "온열질환 관련 정보", i.getHeatstrkinfo());
            putIfPresent(acc, "관련 시설", i.getRelaPosesFclty());
            putIfPresent(acc, "비치 품목", i.getRelaFrnshPrdlst());
            putIfPresent(acc, "대여 가능 품목", i.getRelaRntlPrdlst());
            putIfPresent(acc, "구매 가능 품목", i.getRelaPurcPrdlst());
        }
        return new PetDetailPacket(acc, acmpy);
    }

    private static void putIfPresent(Map<String, String> acc, String label, String raw) {
        if (raw == null) return;
        String v = raw.trim();
        if (v.isEmpty()) return;
        acc.merge(label, v, (a, b) -> a.contains(b) ? a : a + " / " + b);
    }

    private String buildUrl(String baseUrl, Map<String, String> extraParams) {
        StringBuilder url = new StringBuilder(baseUrl);
        url.append(baseUrl.contains("?") ? "&" : "?")
                .append("serviceKey=").append(serviceKey)
                .append("&_type=json")
                .append("&MobileOS=ETC")
                .append("&MobileApp=touraz-dvdholic")
                .append("&numOfRows=").append(MAX_PAGE_SIZE)
                .append("&pageNo=1");
        for (Map.Entry<String, String> e : extraParams.entrySet()) {
            if (e.getValue() == null || e.getValue().isBlank()) continue;
            url.append("&").append(e.getKey()).append("=")
                    .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
        }
        return url.toString();
    }

    private VisitKoreaPetResponse fetchAndParse(String url, String baseUrl) {
        String raw;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.ACCEPT, "application/json");
            raw = httpClient.request(url, HttpMethod.GET, headers, Map.of());
        } catch (Exception ex) {
            log.warn("[KOR-PET] 호출 실패 url={} err={}", baseUrl, ex.getMessage());
            return null;
        }
        if (raw == null || raw.isBlank()) return null;

        VisitKoreaPetResponse parsed;
        try {
            String safe = raw.replace("\"items\":\"\"", "\"items\":null");
            parsed = ObjectMapperUtil.toObject(safe, VisitKoreaPetResponse.class);
        } catch (Exception ex) {
            log.warn("[KOR-PET] 파싱 실패 url={} err={} sample={}", baseUrl, ex.getMessage(),
                    raw.length() > 200 ? raw.substring(0, 200) : raw);
            return null;
        }

        if (parsed == null || parsed.getResponse() == null
                || parsed.getResponse().getBody() == null
                || parsed.getResponse().getBody().getItems() == null
                || parsed.getResponse().getBody().getItems().getItem() == null) {
            return null;
        }
        return parsed;
    }

    private static PetFriendlyPoi toDomain(VisitKoreaPetResponse.Item i) {
        return PetFriendlyPoi.builder()
                .contentId(i.getContentid())
                .contentTypeId(i.getContenttypeid())
                .title(i.getTitle())
                .addr1(i.getAddr1())
                .addr2(i.getAddr2())
                .areaCode(i.getAreacode())
                .sigunguCode(i.getSigungucode())
                .firstImage(i.getFirstimage())
                .firstImageThumb(i.getFirstimage2() != null ? i.getFirstimage2() : i.getFirstimage())
                .tel(i.getTel())
                .mapX(parseD(i.getMapx()))
                .mapY(parseD(i.getMapy()))
                .overview(i.getOverview())
                .homepage(i.getHomepage())
                .acmpyTypeCd(i.getAcmpyTypeCd())
                .build();
    }

    private static Double parseD(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return null; }
    }

    private static String nullSafe(String v) { return v == null ? "" : v; }

    private static <T> List<T> take(List<T> list, int limit) {
        if (list == null || list.isEmpty()) return List.of();
        if (limit <= 0 || limit >= list.size()) return list;
        return list.subList(0, limit);
    }

    private record CacheEntry(List<PetFriendlyPoi> items, long loadedAtMs) {}

    /** detailPetTour2 응답에서 추출한 (정책 맵 + acmpyTypeCd) 패킷. */
    private record PetDetailPacket(Map<String, String> petAcceptance, String acmpyTypeCd) {
        static final PetDetailPacket EMPTY = new PetDetailPacket(Collections.emptyMap(), null);
        boolean isEmpty() { return (petAcceptance == null || petAcceptance.isEmpty()) && acmpyTypeCd == null; }
    }
}
