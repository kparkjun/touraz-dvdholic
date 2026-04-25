package fast.campus.netplix.controller.tour;

import java.util.List;

/**
 * "기준 관광지 → 함께 다녀간 연관 관광지 N건" 묶음 DTO.
 *
 * @param baseSpot   기준 관광지명
 * @param areaName   기준 관광지 광역명
 * @param signguName 기준 관광지 시·군·구명
 * @param related    연관 관광지 목록 (rank 오름차순 정렬)
 */
public record RelatedTouristGroupResponse(
        String baseSpot,
        String areaName,
        String signguName,
        List<RelatedTouristSpotResponse> related
) {}
