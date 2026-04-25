package fast.campus.netplix.controller.tour;

import fast.campus.netplix.tour.RelatedTouristSpot;

/**
 * TarRlteTarService1 단건 응답 DTO.
 *
 * @param baseSpot          기준 관광지명 (사용자가 검색한/지역 대표 관광지)
 * @param relatedSpot       연관 관광지명
 * @param areaName          기준 관광지의 광역명
 * @param signguName        기준 관광지의 시·군·구명
 * @param relatedAreaCd     연관 관광지의 BJD 광역 코드(2자리, 예: "46"=전남) — 후속 brief 조회용
 * @param relatedAreaName   연관 관광지의 광역명 (있을 때)
 * @param relatedSignguCd   연관 관광지의 BJD 시·군·구 코드(5자리)
 * @param relatedSignguName 연관 관광지의 시·군·구명 (있을 때)
 * @param category          카테고리 라벨 (소→중→대 우선 순) — UI 노출용
 * @param rank              연관 순위 (작을수록 함께 방문 빈도 높음)
 * @param hashtags          해시태그/연관도 텍스트
 */
public record RelatedTouristSpotResponse(
        String baseSpot,
        String relatedSpot,
        String areaName,
        String signguName,
        String relatedAreaCd,
        String relatedAreaName,
        String relatedSignguCd,
        String relatedSignguName,
        String category,
        Integer rank,
        String hashtags
) {
    public static RelatedTouristSpotResponse from(RelatedTouristSpot s) {
        return new RelatedTouristSpotResponse(
                s.getTAtsNm(),
                s.getRlteTatsNm(),
                s.getAreaName(),
                s.getSignguName(),
                s.getRlteRegnCd(),
                s.getRlteRegnNm(),
                s.getRlteSignguCd(),
                s.getRlteSignguNm(),
                pickCategory(s),
                s.getRlteRank(),
                s.getHashtags()
        );
    }

    /** UI 가 한 줄로 보여주기 좋도록 (소>중>대) 우선 순으로 가장 구체적인 카테고리를 골라 반환. */
    private static String pickCategory(RelatedTouristSpot s) {
        if (notBlank(s.getRlteCtgrySclsNm())) return s.getRlteCtgrySclsNm();
        if (notBlank(s.getRlteCtgryMclsNm())) return s.getRlteCtgryMclsNm();
        if (notBlank(s.getRlteCtgryLclsNm())) return s.getRlteCtgryLclsNm();
        return null;
    }

    private static boolean notBlank(String v) {
        return v != null && !v.isBlank();
    }
}
