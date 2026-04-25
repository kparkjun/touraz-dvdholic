package fast.campus.netplix.controller.tour;

import fast.campus.netplix.tour.TouristSpotBrief;

/**
 * 관광지 단건 "간단 상세" API 응답 DTO. 모달이 사진/주소/좌표/전화 를 한눈에 그리도록 평탄화.
 *
 * @param title         관광지명
 * @param address       대표 주소
 * @param addressSub    부주소
 * @param firstImage    대표 이미지 URL
 * @param firstImage2   보조 이미지 URL
 * @param mapX          longitude (KTO mapx)
 * @param mapY          latitude  (KTO mapy)
 * @param tel           전화번호
 * @param contentId     KTO contentid
 * @param contentTypeId KTO contenttypeid
 * @param areaCode      KTO 광역코드 (1~8/31~39)
 * @param signguCode    KTO 시군구 코드
 * @param source        가져온 KTO 서비스 ("with"/"pet"/"eng")
 */
public record TouristSpotBriefResponse(
        String title,
        String address,
        String addressSub,
        String firstImage,
        String firstImage2,
        String mapX,
        String mapY,
        String tel,
        String contentId,
        String contentTypeId,
        String areaCode,
        String signguCode,
        String source
) {
    public static TouristSpotBriefResponse from(TouristSpotBrief b) {
        return new TouristSpotBriefResponse(
                b.getTitle(),
                b.getAddress(),
                b.getAddressSub(),
                b.getFirstImage(),
                b.getFirstImage2(),
                b.getMapX(),
                b.getMapY(),
                b.getTel(),
                b.getContentId(),
                b.getContentTypeId(),
                b.getAreaCode(),
                b.getSignguCode(),
                b.getSource()
        );
    }
}
