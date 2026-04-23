package fast.campus.netplix.tour;

import lombok.Builder;
import lombok.Getter;

/**
 * 한국관광공사 관광사진갤러리(PhotoGalleryService1) 항목.
 * 출처: /galleryList1, /gallerySearchList1
 *
 * <p>주요 용도:
 * <ul>
 *   <li>영화 상세 "이 영화 배경 사진첩" (키워드=지역/촬영지)</li>
 *   <li>/cine-trip 지역 상세 보조 갤러리 (phoko 수상작 보완)</li>
 *   <li>/dvd-stores 매장 상세 "이 동네 풍경"</li>
 * </ul>
 *
 * <p>이미지 URL 이 없는 항목은 어댑터 단에서 필터링하여 여기까지 오지 않는다.
 */
@Getter
@Builder
public class TourGallery {

    private final String galContentId;
    private final String galContentTypeId;
    private final String title;
    private final String photographer;
    private final String photoMonth;        // 촬영월 (yyyyMM)
    private final String photoLocation;     // 촬영지
    private final String searchKeyword;     // 검색 키워드(원문)
    private final String thumbnailUrl;
    private final String imageUrl;
    private final String createdTime;
    private final String modifiedTime;
}
