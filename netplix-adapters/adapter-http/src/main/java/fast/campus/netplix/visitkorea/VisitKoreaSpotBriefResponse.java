package fast.campus.netplix.visitkorea;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * KTO {@code searchKeyword2} 공통 응답 포맷 (KorWith / KorPet / Eng 서비스 공유).
 *
 * <p>모달의 "간단 상세" 표시에 필요한 최소 필드만 매핑한다. 응답 필드명이 모두 소문자 1단어
 * (title, addr1, firstimage 등) 라 Jackson 기본 매퍼가 그대로 인식한다.
 *
 * <p>{@code totalCount=0} 일 때 KTO 가 {@code "items": ""} 로 내려주는 관례는 어댑터에서
 * 사전 치환 후 파싱한다.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VisitKoreaSpotBriefResponse {

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
        private String areacode;
        private String sigungucode;
        private String firstimage;
        private String firstimage2;
        private String tel;
        private String mapx;
        private String mapy;
    }
}
