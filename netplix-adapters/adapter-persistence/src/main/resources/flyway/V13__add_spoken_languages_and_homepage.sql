-- 상영 언어(spoken_languages) 및 공식 홈페이지(homepage) 필드 추가
ALTER TABLE `movies`
ADD COLUMN `SPOKEN_LANGUAGES` VARCHAR(255) COMMENT '상영 언어 (영어, 한국어 등)' AFTER `VOTE_COUNT`,
ADD COLUMN `HOMEPAGE` VARCHAR(500) COMMENT '공식 홈페이지 URL' AFTER `SPOKEN_LANGUAGES`;
