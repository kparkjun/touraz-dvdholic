package fast.campus.netplix.tour;

import java.util.Optional;

/**
 * 관광지 단건 "간단 상세" 조회 Port.
 *
 * <p>구현체는 가용한 KTO 검색 API 들(KorWithService2 / KorPetTourService2 / EngService2) 을
 * 폴백 체인으로 호출해 가장 먼저 매칭되는 결과를 채워 돌려준다. 선택된 출처는
 * {@link TouristSpotBrief#getSource()} 로 노출된다.
 *
 * <p>일일 트래픽을 보호하기 위해 어댑터 내부에 TTL 캐시를 유지하는 것을 가정한다.
 */
public interface TouristSpotBriefPort {

    /**
     * 키워드 + (선택) 광역 BJD 코드로 단건 정보 조회.
     *
     * @param keyword     관광지 명칭. blank 면 빈 결과.
     * @param bjdAreaCode 행정 BJD 광역 2자리(예: "11"=서울, "46"=전남). 폴백 동안 KTO areaCode 로 변환.
     *                    null/blank 허용 — 전국 매칭 후 1순위 채택.
     * @return 매칭 결과(이미지/주소/좌표 포함). 모든 폴백 경로에서 비어 있으면 {@link Optional#empty()}.
     */
    Optional<TouristSpotBrief> findFirstByKeyword(String keyword, String bjdAreaCode);

    /** 어댑터 호출 가능 상태 여부 (serviceKey + URL 설정 충족). */
    boolean isConfigured();
}
