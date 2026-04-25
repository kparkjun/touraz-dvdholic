package fast.campus.netplix.tour;

import lombok.Builder;
import lombok.Getter;

/**
 * 한국관광공사 "관광지별 연관 관광지 정보"(TarRlteTarService1) 응답 1행.
 *
 * <p>Base URL: {@code apis.data.go.kr/B551011/TarRlteTarService1}
 *
 * <p>의미: 기준 관광지({@link #tAtsNm})를 다녀간 관광객들이 함께 / 이어서 방문한 연관 관광지
 * ({@link #rlteTatsNm}) 를 KTO 빅데이터(통신·카드·내비)로 산출한 결과. {@link #rlteRank}
 * 가 작을수록 함께 방문되는 빈도가 높음.
 *
 * <p>주의: 응답에는 좌표(mapX/mapY)가 포함되지 않는다. 좌표가 필요한 경우
 * {@code KorService2/searchKeyword2} 등으로 후속 조회해 결합한다.
 */
@Getter
@Builder(toBuilder = true)
public class RelatedTouristSpot {

    /** 기준 관광지가 속한 광역 코드 (예: 11=서울, 26=부산). */
    private final String areaCode;
    private final String areaName;

    /** 기준 관광지가 속한 시·군·구 코드(5자리)와 명칭. */
    private final String signguCode;
    private final String signguName;

    /** 기준 관광지명. */
    private final String tAtsNm;

    /** 연관 관광지명 — 기준 관광지와 함께/이어서 방문된 곳. */
    private final String rlteTatsNm;

    /** 연관 관광지의 광역/시군구. 일부 응답에서는 미제공. */
    private final String rlteRegnCd;
    private final String rlteRegnNm;
    private final String rlteSignguCd;
    private final String rlteSignguNm;

    /** 연관 관광지 카테고리 분류 (대/중/소). 빈 값일 수 있음. */
    private final String rlteCtgryLclsNm;
    private final String rlteCtgryMclsNm;
    private final String rlteCtgrySclsNm;

    /** 연관 순위 (1 이 함께 방문 빈도 1위). */
    private final Integer rlteRank;

    /** 연관도/해시태그 텍스트(있을 때). */
    private final String hashtags;
}
