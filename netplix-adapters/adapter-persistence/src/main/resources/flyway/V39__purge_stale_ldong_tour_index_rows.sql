-- V39: tour_index_snapshots 에서 KTO lDongRegnCd(11,26,27,28,29,30,31,36,41,42,43,44,45,46,47,48,50,51,52) 체계로
-- 저장됐던 레거시 row 들을 삭제한다. 2026-04 배포에서 VisitKoreaDataLabHttpClient 가 lDongRegnCd 를
-- KorService2 areaCode(1~8, 31~39) 로 변환한 뒤 upsert 하도록 변경됐기 때문에, 이전에 쌓인 row 들은
-- 동일 지자체를 중복 표현하며 CineTrip 매핑 조인에 사용되지 않아 의미가 없다.
--
-- 주의: KorService2 areaCode 와 lDongRegnCd 가 우연히 일치하는 값(31=경기 vs 울산, 36=경남 vs 세종)
-- 은 regionName 으로 구분한다. 잘못 삭제되는 케이스를 피하기 위해 WHERE 절을 보수적으로 작성.
DELETE FROM tour_index_snapshots
WHERE area_code IN ('11', '26', '27', '28', '29', '30', '41', '42', '43', '44', '45', '46', '47', '48', '50', '51', '52')
   OR (area_code = '31' AND region_name IN ('울산광역시', '울산'))
   OR (area_code = '36' AND region_name IN ('세종특별자치시', '세종'));
