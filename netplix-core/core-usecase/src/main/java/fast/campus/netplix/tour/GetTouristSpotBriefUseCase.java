package fast.campus.netplix.tour;

import java.util.Optional;

/**
 * 관광지 단건 "간단 상세" (이미지·주소·좌표·전화) 조회 유스케이스.
 *
 * <p>{@code /related-spots} 모달이 마운트될 때 비동기로 호출되어, KTO TarRlteTarService1
 * 응답에 빠져 있는 풍부 메타를 보완 표시한다.
 */
public interface GetTouristSpotBriefUseCase {

    /**
     * @param keyword     관광지 명칭. blank 면 빈 결과.
     * @param bjdAreaCode 행정 BJD 광역 2자리(예: 11/46). null 가능.
     */
    Optional<TouristSpotBrief> findFirst(String keyword, String bjdAreaCode);

    boolean isConfigured();
}
