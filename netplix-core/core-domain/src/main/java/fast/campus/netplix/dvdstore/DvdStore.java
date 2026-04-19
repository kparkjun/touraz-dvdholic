package fast.campus.netplix.dvdstore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DvdStore {
    private final Long id;
    private final String managementNo;
    private final String areaCode;
    private final String businessName;
    private final String statusName;
    private final String statusCode;
    private final String roadAddress;
    private final String jibunAddress;
    private final String phone;
    private final String postalCode;
    private final String licenseDate;
    private final String closureDate;
    private final String coordinateX;
    private final String coordinateY;
    private final String productInfo;
    private final String facilityArea;
    private final String lastDataModified;
    private final Double latitude;
    private final Double longitude;
    private Double distance;

    private final String businessType;
    private final String detailStatusName;
    private final String detailStatusCode;
    private final String suspendStartDate;
    private final String suspendEndDate;
    private final String licenseCancelDate;
    private final String dataUpdateDate;
    private final String buildingUsage;
    private final String floorAbove;
    private final String floorBelow;
    private final String floorTotal;
    private final String regionType;
    private final String gameName;
    private final String surroundingEnv;
    private final String dataUpdateType;
    private final String previousGameBusinessType;
    private final String culturalBusinessType;

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public boolean isOperating() {
        return "01".equals(statusCode);
    }
}
