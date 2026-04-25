package fast.campus.netplix.controller.tour;

import fast.campus.netplix.controller.NetplixApiResponse;
import fast.campus.netplix.tour.GetTouristSpotBriefUseCase;
import fast.campus.netplix.tour.TouristSpotBrief;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * 관광지 단건 "간단 상세" 조회 API.
 *
 * <p>{@code /related-spots} 모달이 마운트될 때 비동기로 호출.
 *
 * <ul>
 *   <li>GET /api/v1/tour/spot/brief?q=오동도&areaCode=46 — 키워드 + (선택) BJD 광역코드</li>
 *   <li>GET /api/v1/tour/spot/brief/status — 어댑터 설정 상태</li>
 * </ul>
 *
 * <p>응답이 비어 있을 수 있으므로 (KTO 데이터 부재) UI 는 빈 결과를 우아하게 폴백 처리해야 한다.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tour/spot/brief")
@RequiredArgsConstructor
public class TouristSpotBriefController {

    private final GetTouristSpotBriefUseCase useCase;

    @GetMapping
    public NetplixApiResponse<TouristSpotBriefResponse> brief(
            @RequestParam String q,
            @RequestParam(required = false) String areaCode) {
        Optional<TouristSpotBrief> found = useCase.findFirst(q, areaCode);
        return NetplixApiResponse.ok(found.map(TouristSpotBriefResponse::from).orElse(null));
    }

    @GetMapping("/status")
    public NetplixApiResponse<StatusResponse> status() {
        return NetplixApiResponse.ok(new StatusResponse(useCase.isConfigured()));
    }

    public record StatusResponse(boolean configured) {}
}
