package fast.campus.netplix.controller.cinetrip;

import fast.campus.netplix.cinetrip.CineTripItem;
import fast.campus.netplix.cinetrip.CineTripUseCase;
import fast.campus.netplix.controller.NetplixApiResponse;
import fast.campus.netplix.controller.tour.AccessiblePoiResponse;
import fast.campus.netplix.tour.GetAccessiblePoiUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CineTrip 큐레이션 API.
 * - GET /curate: 전체 트렌딩 기반 큐레이션
 * - GET /region/{areaCode}: 특정 지자체의 영화 큐레이션
 * - GET /movie: 특정 영화의 연결 지역 카드
 * - POST /import (인증): CSV 업로드로 매핑 시드
 * - GET /count: 현재 매핑 수 (디버그/대시보드용)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/cine-trip")
@RequiredArgsConstructor
public class CineTripController {

    private final CineTripUseCase cineTripUseCase;
    private final GetAccessiblePoiUseCase accessiblePoiUseCase;

    /** CineTrip 상세 모달에서 "이 지역 함께 가볼만한 곳" 섹션에 쓰는 키. */
    private static final Map<String, String> ACCESSIBLE_BUCKETS = Map.of(
            "attractions", "12", // 관광지
            "restaurants", "39", // 음식점
            "accommodations", "32" // 숙박
    );

    @GetMapping("/curate")
    public NetplixApiResponse<List<CineTripResponse>> curate(
            @RequestParam(defaultValue = "12") int limit) {
        List<CineTripItem> items = cineTripUseCase.curate(limit);
        return NetplixApiResponse.ok(items.stream().map(CineTripResponse::from).toList());
    }

    @GetMapping("/region/{areaCode}")
    public NetplixApiResponse<List<CineTripResponse>> byRegion(
            @PathVariable String areaCode,
            @RequestParam(defaultValue = "12") int limit) {
        List<CineTripItem> items = cineTripUseCase.curateByRegion(areaCode, limit);
        return NetplixApiResponse.ok(items.stream().map(CineTripResponse::from).toList());
    }

    @GetMapping("/movie")
    public NetplixApiResponse<List<CineTripResponse>> byMovie(@RequestParam String name) {
        List<CineTripItem> items = cineTripUseCase.getByMovieName(name);
        return NetplixApiResponse.ok(items.stream().map(CineTripResponse::from).toList());
    }

    @PostMapping(value = "/import", consumes = {"multipart/form-data", "text/plain", "application/octet-stream"})
    public NetplixApiResponse<Map<String, Integer>> importCsv(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestBody(required = false) String body) throws Exception {
        String csv = resolveCsv(file, body);
        if (csv == null || csv.isBlank()) {
            return NetplixApiResponse.ok(Map.of("imported", 0));
        }
        int count = cineTripUseCase.importFromCsv(csv);
        log.info("[CINE-TRIP] CSV 업로드: {}행 반영", count);
        return NetplixApiResponse.ok(Map.of("imported", count));
    }

    @GetMapping("/count")
    public NetplixApiResponse<Long> count() {
        return NetplixApiResponse.ok(cineTripUseCase.count());
    }

    /**
     * CineTrip 상세 모달용 "이 영화 배경지 근처 무장애 스팟" 묶음 조회.
     * 관광지/음식점/숙박 3종을 각 {@code perBucket} 개씩 반환. KorWithService2 미설정 시 빈 맵.
     */
    @GetMapping("/region/{areaCode}/accessible")
    public NetplixApiResponse<Map<String, List<AccessiblePoiResponse>>> accessibleByRegion(
            @PathVariable String areaCode,
            @RequestParam(defaultValue = "5") int perBucket) {
        Map<String, List<AccessiblePoiResponse>> result = new LinkedHashMap<>();
        if (!accessiblePoiUseCase.isConfigured()) {
            ACCESSIBLE_BUCKETS.keySet().forEach(k -> result.put(k, List.of()));
            return NetplixApiResponse.ok(result);
        }
        ACCESSIBLE_BUCKETS.forEach((bucket, typeId) -> {
            List<AccessiblePoiResponse> pois = accessiblePoiUseCase
                    .byArea(areaCode, typeId, perBucket)
                    .stream()
                    .map(AccessiblePoiResponse::from)
                    .toList();
            result.put(bucket, pois);
        });
        return NetplixApiResponse.ok(result);
    }

    private String resolveCsv(MultipartFile file, String body) throws Exception {
        if (file != null && !file.isEmpty()) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }
        return body;
    }
}
