package fast.campus.netplix.visitkorea;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 한국관광공사 PhotoGalleryService1 응답 포맷.
 * - /galleryList1, /gallerySearchList1, /galleryDetailList1 모두 동일한 response.body.items.item 구조.
 * - items 가 0 건일 때 빈 문자열("")로 내려올 수 있어 어댑터에서 사전 치환.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VisitKoreaGalleryResponse {

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
     * galleryList1 / gallerySearchList1 공통 필드 (공식 명세 기준).
     * - galContentId / galContentTypeId
     * - galTitle, galPhotographer, galPhotographyMonth, galPhotographyLocation
     * - galSearchKeyword
     * - galWebImageUrl (대형), createdtime / modifiedtime
     * 일부 응답은 대/소문자가 섞여 나오므로 느슨한 매핑.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String galContentId;
        private String galContentTypeId;
        private String galTitle;
        private String galPhotographer;
        private String galPhotographyMonth;
        private String galPhotographyLocation;
        private String galSearchKeyword;
        private String galWebImageUrl;
        private String createdtime;
        private String modifiedtime;
    }
}
