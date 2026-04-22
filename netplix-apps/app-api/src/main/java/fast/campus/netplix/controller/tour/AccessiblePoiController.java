package fast.campus.netplix.controller.tour;

import fast.campus.netplix.controller.NetplixApiResponse;
import fast.campus.netplix.tour.GetAccessiblePoiUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 한국관광공사 무장애 여행정보(KorWithService2) 공개 API.
 *
 * <p>엔드포인트 (비로그인 노출 가능):
 * <ul>
 *   <li>GET /api/v1/tour/accessible?areaCode=1&type=12&limit=12</li>
 *   <li>GET /api/v1/tour/accessible/location?mapX=126.97&mapY=37.57&radius=2000&type=39</li>
 *   <li>GET /api/v1/tour/accessible/search?q=경복궁&type=12</li>
 *   <li>GET /api/v1/tour/accessible/{contentId}?type=12</li>
 *   <li>GET /api/v1/tour/accessible/status — serviceKey 설정 여부</li>
 * </ul>
 *
 * <p>contentTypeId (type 파라미터):
 * <ul>
 *   <li>12 관광지 / 14 문화시설 / 15 축제공연 / 25 여행코스</li>
 *   <li>28 레포츠 / 32 숙박 / 38 쇼핑 / 39 음식점</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tour/accessible")
@RequiredArgsConstructor
public class AccessiblePoiController {

    private final GetAccessiblePoiUseCase useCase;

    @GetMapping
    public NetplixApiResponse<List<AccessiblePoiResponse>> byArea(
            @RequestParam(required = false) String areaCode,
            @RequestParam(name = "type", required = false) String contentTypeId,
            @RequestParam(defaultValue = "12") int limit) {
        List<AccessiblePoiResponse> body = useCase.byArea(areaCode, contentTypeId, limit)
                .stream().map(AccessiblePoiResponse::from).toList();
        return NetplixApiResponse.ok(body);
    }

    @GetMapping("/location")
    public NetplixApiResponse<List<AccessiblePoiResponse>> byLocation(
            @RequestParam double mapX,
            @RequestParam double mapY,
            @RequestParam(defaultValue = "3000") int radius,
            @RequestParam(name = "type", required = false) String contentTypeId,
            @RequestParam(defaultValue = "12") int limit) {
        List<AccessiblePoiResponse> body = useCase.byLocation(mapX, mapY, radius, contentTypeId, limit)
                .stream().map(AccessiblePoiResponse::from).toList();
        return NetplixApiResponse.ok(body);
    }

    @GetMapping("/search")
    public NetplixApiResponse<List<AccessiblePoiResponse>> byKeyword(
            @RequestParam String q,
            @RequestParam(name = "type", required = false) String contentTypeId,
            @RequestParam(defaultValue = "12") int limit) {
        List<AccessiblePoiResponse> body = useCase.byKeyword(q, contentTypeId, limit)
                .stream().map(AccessiblePoiResponse::from).toList();
        return NetplixApiResponse.ok(body);
    }

    @GetMapping("/status")
    public NetplixApiResponse<StatusResponse> status() {
        return NetplixApiResponse.ok(new StatusResponse(useCase.isConfigured()));
    }

    @GetMapping("/{contentId}")
    public NetplixApiResponse<AccessiblePoiResponse> detail(
            @PathVariable String contentId,
            @RequestParam(name = "type", required = false) String contentTypeId) {
        AccessiblePoiResponse body = useCase.detail(contentId, contentTypeId)
                .map(AccessiblePoiResponse::from)
                .orElse(null);
        return NetplixApiResponse.ok(body);
    }

    public record StatusResponse(boolean configured) {}
}
