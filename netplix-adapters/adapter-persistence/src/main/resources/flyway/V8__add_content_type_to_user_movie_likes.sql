-- user_movie_likes 테이블에 CONTENT_TYPE 컬럼 추가
-- movie: 영화, dvd: DVD 구분용
ALTER TABLE `user_movie_likes` 
ADD COLUMN `CONTENT_TYPE` VARCHAR(20) DEFAULT 'movie' COMMENT '콘텐츠 타입 (movie/dvd)' AFTER `MOVIE_ID`;

-- 기존 데이터는 'movie'로 설정 (이미 DEFAULT로 설정됨)

-- 인덱스 추가 (검색 성능 향상)
CREATE INDEX idx_user_movie_likes_content_type ON `user_movie_likes` (`MOVIE_ID`, `CONTENT_TYPE`);
