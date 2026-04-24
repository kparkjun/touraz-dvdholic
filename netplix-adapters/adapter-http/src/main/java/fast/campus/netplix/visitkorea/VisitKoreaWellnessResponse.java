package fast.campus.netplix.visitkorea;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 한국관광공사 웰니스관광(WellnessTursmService) 공통 응답 스키마.
 *
 * <p>GoCamping 과 동일하게 response.body.items.item 구조를 사용하지만, item 필드명은
 * KTO 표준(contentid, mapx, firstimage 등)을 따른다(camping 은 facltNm/mapX 처럼 camelCase 혼재).
 * 0건 응답 시 items 가 빈 문자열("")로 내려오는 경우가 있어 어댑터에서 사전 치환.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VisitKoreaWellnessResponse {

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
     * WellnessTursmService /areaBasedList · /locationBasedList · /searchKeyword 공용 item.
     * 필드명은 API 응답 원문(소문자 snake-ish)을 그대로 따른다.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String contentid;
        private String contenttypeid;
        private String title;
        private String addr1;
        private String addr2;
        private String zipcode;
        private String tel;
        private String firstimage;
        private String firstimage2;
        private String mapx;          // 경도(longitude)
        private String mapy;          // 위도(latitude)
        private String cat1;
        private String cat2;
        private String cat3;
        private String areacode;
        private String sigungucode;
        /** locationBasedList 에서 내려주는 거리(m). */
        private String dist;
        private String createdtime;
        private String modifiedtime;
    }
}
