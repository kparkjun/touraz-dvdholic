package fast.campus.netplix.entity.dvdstore;

import fast.campus.netplix.dvdstore.DvdStore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "dvd_stores")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DvdStoreEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "MANAGEMENT_NO", nullable = false)
    private String managementNo;

    @Column(name = "AREA_CODE")
    private String areaCode;

    @Column(name = "BUSINESS_NAME")
    private String businessName;

    @Column(name = "STATUS_NAME")
    private String statusName;

    @Column(name = "STATUS_CODE")
    private String statusCode;

    @Column(name = "ROAD_ADDRESS", length = 500)
    private String roadAddress;

    @Column(name = "JIBUN_ADDRESS", length = 500)
    private String jibunAddress;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "POSTAL_CODE")
    private String postalCode;

    @Column(name = "LICENSE_DATE")
    private String licenseDate;

    @Column(name = "CLOSURE_DATE")
    private String closureDate;

    @Column(name = "COORDINATE_X")
    private String coordinateX;

    @Column(name = "COORDINATE_Y")
    private String coordinateY;

    @Column(name = "PRODUCT_INFO", length = 500)
    private String productInfo;

    @Column(name = "FACILITY_AREA")
    private String facilityArea;

    @Column(name = "LAST_DATA_MODIFIED")
    private String lastDataModified;

    @Column(name = "LATITUDE")
    private Double latitude;

    @Column(name = "LONGITUDE")
    private Double longitude;

    @Column(name = "BUSINESS_TYPE")
    private String businessType;

    @Column(name = "DETAIL_STATUS_NAME")
    private String detailStatusName;

    @Column(name = "DETAIL_STATUS_CODE")
    private String detailStatusCode;

    @Column(name = "SUSPEND_START_DATE")
    private String suspendStartDate;

    @Column(name = "SUSPEND_END_DATE")
    private String suspendEndDate;

    @Column(name = "LICENSE_CANCEL_DATE")
    private String licenseCancelDate;

    @Column(name = "DATA_UPDATE_DATE")
    private String dataUpdateDate;

    @Column(name = "BUILDING_USAGE")
    private String buildingUsage;

    @Column(name = "FLOOR_ABOVE")
    private String floorAbove;

    @Column(name = "FLOOR_BELOW")
    private String floorBelow;

    @Column(name = "FLOOR_TOTAL")
    private String floorTotal;

    @Column(name = "REGION_TYPE")
    private String regionType;

    @Column(name = "GAME_NAME")
    private String gameName;

    @Column(name = "SURROUNDING_ENV")
    private String surroundingEnv;

    @Column(name = "DATA_UPDATE_TYPE")
    private String dataUpdateType;

    @Column(name = "PREV_GAME_BIZ_TYPE")
    private String previousGameBusinessType;

    @Column(name = "CULTURAL_BIZ_TYPE")
    private String culturalBusinessType;

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    public DvdStore toDomain() {
        return DvdStore.builder()
                .id(id)
                .managementNo(managementNo)
                .areaCode(areaCode)
                .businessName(businessName)
                .statusName(statusName)
                .statusCode(statusCode)
                .roadAddress(roadAddress)
                .jibunAddress(jibunAddress)
                .phone(phone)
                .postalCode(postalCode)
                .licenseDate(licenseDate)
                .closureDate(closureDate)
                .coordinateX(coordinateX)
                .coordinateY(coordinateY)
                .productInfo(productInfo)
                .facilityArea(facilityArea)
                .lastDataModified(lastDataModified)
                .latitude(latitude)
                .longitude(longitude)
                .businessType(businessType)
                .detailStatusName(detailStatusName)
                .detailStatusCode(detailStatusCode)
                .suspendStartDate(suspendStartDate)
                .suspendEndDate(suspendEndDate)
                .licenseCancelDate(licenseCancelDate)
                .dataUpdateDate(dataUpdateDate)
                .buildingUsage(buildingUsage)
                .floorAbove(floorAbove)
                .floorBelow(floorBelow)
                .floorTotal(floorTotal)
                .regionType(regionType)
                .gameName(gameName)
                .surroundingEnv(surroundingEnv)
                .dataUpdateType(dataUpdateType)
                .previousGameBusinessType(previousGameBusinessType)
                .culturalBusinessType(culturalBusinessType)
                .build();
    }

    public static DvdStoreEntity fromDomain(DvdStore d) {
        LocalDateTime now = LocalDateTime.now();
        DvdStoreEntity e = new DvdStoreEntity();
        e.managementNo = d.getManagementNo();
        e.areaCode = d.getAreaCode();
        e.businessName = d.getBusinessName();
        e.statusName = d.getStatusName();
        e.statusCode = d.getStatusCode();
        e.roadAddress = d.getRoadAddress();
        e.jibunAddress = d.getJibunAddress();
        e.phone = d.getPhone();
        e.postalCode = d.getPostalCode();
        e.licenseDate = d.getLicenseDate();
        e.closureDate = d.getClosureDate();
        e.coordinateX = d.getCoordinateX();
        e.coordinateY = d.getCoordinateY();
        e.productInfo = d.getProductInfo();
        e.facilityArea = d.getFacilityArea();
        e.lastDataModified = d.getLastDataModified();
        e.latitude = d.getLatitude();
        e.longitude = d.getLongitude();
        e.businessType = d.getBusinessType();
        e.detailStatusName = d.getDetailStatusName();
        e.detailStatusCode = d.getDetailStatusCode();
        e.suspendStartDate = d.getSuspendStartDate();
        e.suspendEndDate = d.getSuspendEndDate();
        e.licenseCancelDate = d.getLicenseCancelDate();
        e.dataUpdateDate = d.getDataUpdateDate();
        e.buildingUsage = d.getBuildingUsage();
        e.floorAbove = d.getFloorAbove();
        e.floorBelow = d.getFloorBelow();
        e.floorTotal = d.getFloorTotal();
        e.regionType = d.getRegionType();
        e.gameName = d.getGameName();
        e.surroundingEnv = d.getSurroundingEnv();
        e.dataUpdateType = d.getDataUpdateType();
        e.previousGameBusinessType = d.getPreviousGameBusinessType();
        e.culturalBusinessType = d.getCulturalBusinessType();
        e.createdAt = now;
        e.updatedAt = now;
        return e;
    }
}
