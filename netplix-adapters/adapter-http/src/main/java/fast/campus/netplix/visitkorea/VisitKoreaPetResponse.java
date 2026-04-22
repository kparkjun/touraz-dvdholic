package fast.campus.netplix.visitkorea;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 한국관광공사 반려동물 동반여행(KorPetTourService) 공통 응답 포맷.
 *
 * <p>다음 오퍼레이션들이 동일한 {@code response.body.items.item} 구조를 공유:
 * <ul>
 *   <li>/areaBasedList2     : 지역/콘텐츠타입 기반 목록</li>
 *   <li>/locationBasedList2 : 좌표 반경 목록</li>
 *   <li>/searchKeyword2     : 키워드 검색</li>
 *   <li>/detailCommon2      : 공통 정보 (overview/homepage 등)</li>
 *   <li>/detailPetTour2     : 반려동물 동반 정책 세부(acmpyTypeCd / acmpyNeedMtr / etiquetteMtr ...)</li>
 * </ul>
 *
 * <p>totalCount=0 일 때 items 가 빈 문자열("")로 내려오는 KTO 관례는 클라이언트에서
 * {@code null} 로 사전 치환한다.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VisitKoreaPetResponse {

    private Response response;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private Header header;
        private Body body;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private Items items;
        private Integer numOfRows;
        private Integer pageNo;
        private Integer totalCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        private List<Item> item;
    }

    /**
     * areaBased/locationBased/searchKeyword/detailCommon + detailPetTour 필드를 느슨하게 수용.
     * 미존재 필드는 Jackson 이 null 로 둔다.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        // 공통(areaBased / locationBased / searchKeyword / detailCommon2)
        private String contentid;
        private String contenttypeid;
        private String title;
        private String addr1;
        private String addr2;
        private String areacode;
        private String sigungucode;
        private String firstimage;
        private String firstimage2;
        private String tel;
        private String mapx;
        private String mapy;
        private String overview;
        private String homepage;

        // detailPetTour2 - 반려동물 동반 정책 필드 (KTO 공식 필드명 소문자)
        /** 동반 구분 코드. 1=동반가능 / 2=일부제한적 / 3=불가 */
        private String acmpyTypeCd;
        /** 관련 사고 방지 관련 사항 */
        private String relaAcdntRiskMtr;
        /** 동반 시 필요 사항 */
        private String acmpyNeedMtr;
        /** 반려동물 에티켓 */
        private String etiquetteMtr;
        /** 온열질환 관련 정보 */
        private String heatstrkinfo;
        /** 동반 가능한 반려동물 종류/제한(크기/무게) */
        private String acmpyPsblCpam;
        /** 관련 보유 시설 */
        private String relaPosesFclty;
        /** 관련 비치 품목 */
        private String relaFrnshPrdlst;
        /** 관련 구매 품목 */
        private String relaPurcPrdlst;
        /** 관련 대여 품목 */
        private String relaRntlPrdlst;
    }
}
