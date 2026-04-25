package fast.campus.netplix.tour;

import lombok.Builder;
import lombok.Getter;

/**
 * 단건 관광지의 "간단 상세" 정보. UI 모달이 한눈에 보여주기 위한 최소 필드 묶음.
 *
 * <p>출처는 KTO KorWithService2 / KorPetTourService2 / EngService2 의 {@code searchKeyword2}
 * 응답을 폴백 체인으로 묶어 가장 먼저 매칭되는 결과를 채운다. 어떤 서비스에서 왔는지는
 * {@link #source} 로 노출해 (예: "with" / "pet" / "eng") 디버깅·UX 표기에 활용.
 *
 * <p>좌표(mapX, mapY) 와 이미지(firstImage) 가 포함되므로 모달이 사진/지도 미리보기까지
 * 자연스럽게 그려낼 수 있다. KTO TarRlteTarService1 응답에는 이런 필드가 없어 별도 페치가 필요했다.
 */
@Getter
@Builder
public class TouristSpotBrief {

    /** 관광지 명칭 (KTO {@code title}). */
    private final String title;

    /** 도로명/지번 주소 (KTO {@code addr1}). */
    private final String address;

    /** 부주소 — 동·층 등 보조 정보 (KTO {@code addr2}). */
    private final String addressSub;

    /** 대표 이미지 URL (KTO {@code firstimage}). */
    private final String firstImage;

    /** 대표 이미지 보조 URL (KTO {@code firstimage2}). 모달 갤러리 보조 슬롯. */
    private final String firstImage2;

    /** 경도(longitude) — KTO 응답 {@code mapx}. */
    private final String mapX;

    /** 위도(latitude) — KTO 응답 {@code mapy}. */
    private final String mapY;

    /** 전화번호 (KTO {@code tel}). */
    private final String tel;

    /** 관광지 콘텐츠 ID (KTO {@code contentid}). detailCommon2 후속 호출 시 사용. */
    private final String contentId;

    /** 콘텐츠 타입 ID (KTO {@code contenttypeid}). 12=관광지/14=문화시설/... */
    private final String contentTypeId;

    /** KTO 광역 코드(1~39, 경기=31 등). 응답 {@code areacode}. */
    private final String areaCode;

    /** KTO 시·군·구 코드 (광역 내 1~26 범위). 응답 {@code sigungucode}. */
    private final String signguCode;

    /** 어느 KTO 서비스에서 채워진 데이터인지 ("with"/"pet"/"eng") — UI 라벨링·디버깅. */
    private final String source;
}
