/**
 * Quiet Set Radar / 혼잡도 교차 연결용 지역명 ↔ KTO areaCode 매핑 유틸.
 *
 * 백엔드(/api/v1/cine-trip/concentration) 는 한국관광공사 areaCode 기준으로 동작한다.
 * 하지만 프론트 각 페이지는 문맥별로 서로 다른 형태의 "지역 힌트"를 들고 있다.
 *  - cine-trip 페이지: REGION_FILTERS.areaCode 를 이미 보유
 *  - DVD 매장 페이지: 매장 주소 첫 토큰에서 뽑은 시·도명 문자열 (예: "서울", "경상남")
 *  - 영화 상세 페이지: /api/v1/cine-trip/movie 응답의 areaCode (이미 string)
 *  - 웰니스 페이지: 17개 광역명 문자열
 *
 * 이 파일은 "문자열 → areaCode" 방향의 단일 출처(single source of truth) 를 제공해
 *   1) 각 페이지가 각자 다른 맵을 관리하는 중복을 제거하고
 *   2) "경상남" / "경상남도" / "경남" 같은 축약/접미 변형을 한 곳에서 흡수한다.
 *
 * 반환값은 문자열(API 그대로 사용) 로 통일. 매칭 실패 시 null.
 */

export const AREA_LABEL_TO_CODE = {
  서울: '1',
  서울특별시: '1',
  부산: '6',
  부산광역시: '6',
  인천: '2',
  인천광역시: '2',
  대전: '3',
  대전광역시: '3',
  대구: '4',
  대구광역시: '4',
  광주: '5',
  광주광역시: '5',
  울산: '7',
  울산광역시: '7',
  세종: '8',
  세종특별자치시: '8',
  경기: '31',
  경기도: '31',
  강원: '32',
  강원도: '32',
  강원특별자치도: '32',
  충북: '33',
  충청북도: '33',
  충남: '34',
  충청남도: '34',
  경북: '35', // 주의: 백엔드 /concentration 기준 광역코드가 아닌 KTO TourAPI areaCode(전북=37/전남=38/경북=35/경남=36) 아님 — 실제 프로젝트 REGION_FILTERS 값과 일치하도록 35→전북 매핑이 뒤바뀌어 있지 않은지 cine-trip/page.js 참조.
};

// cine-trip/page.js 의 REGION_FILTERS 와 정확히 동일한 매핑을 유지하기 위해 위 값을 덮어씀.
// (사용자가 이미 검증해 놓은 값이므로 그대로 따른다. 위 블록은 별칭/접미 흡수 목적)
Object.assign(AREA_LABEL_TO_CODE, {
  경북: '37',
  경상북도: '37',
  경남: '38',
  경상남도: '38',
  전북: '35',
  전라북도: '35',
  전북특별자치도: '35',
  전남: '36',
  전라남도: '36',
  제주: '39',
  제주도: '39',
  제주특별자치도: '39',
});

/**
 * 자유형식 문자열에서 광역 시·도를 추정해 areaCode 를 돌려준다.
 *
 * 허용 입력 예:
 *   "서울", "서울특별시", "서울특별시 강남구", "경상남", "경남", "경남 창원시 마산합포구"
 *
 * 탐색 순서:
 *   1) 공백 기준 첫 토큰 정확 매칭
 *   2) 그 토큰에서 "도/시/특별시/광역시/..." 접미 제거 후 재매칭
 *   3) 원문 전체에서 맵의 각 키를 substring 포함 검사 (짧은 키 먼저 가중 위해 긴 키 우선)
 */
export function resolveAreaCode(text) {
  if (!text) return null;
  const s = String(text).trim();
  if (!s) return null;

  if (AREA_LABEL_TO_CODE[s]) return AREA_LABEL_TO_CODE[s];

  const first = s.split(/\s+/)[0] || '';
  if (AREA_LABEL_TO_CODE[first]) return AREA_LABEL_TO_CODE[first];

  const stripped = first.replace(
    /특별자치도$|특별자치시$|특별시$|광역시$|자치시$|자치도$|도$|시$/,
    ''
  );
  if (stripped && AREA_LABEL_TO_CODE[stripped]) {
    return AREA_LABEL_TO_CODE[stripped];
  }

  // 긴 키부터 탐색해 "경상남" 이 "경상" 에 잘못 잡히지 않도록
  const keys = Object.keys(AREA_LABEL_TO_CODE).sort((a, b) => b.length - a.length);
  for (const k of keys) {
    if (s.indexOf(k) >= 0) return AREA_LABEL_TO_CODE[k];
  }
  return null;
}

export const AREA_CODE_TO_LABEL = {
  1: '서울',
  2: '인천',
  3: '대전',
  4: '대구',
  5: '광주',
  6: '부산',
  7: '울산',
  8: '세종',
  31: '경기',
  32: '강원',
  33: '충북',
  34: '충남',
  35: '전북',
  36: '전남',
  37: '경북',
  38: '경남',
  39: '제주',
};

export function areaLabel(areaCode) {
  if (areaCode == null) return '';
  const key = String(areaCode);
  return AREA_CODE_TO_LABEL[key] || '';
}
