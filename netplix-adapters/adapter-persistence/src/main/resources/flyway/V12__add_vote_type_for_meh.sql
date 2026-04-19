-- 꿀꿀해(meh) 투표 지원: VOTE_TYPE 컬럼 추가
-- like: 좋아요, unlike: 싫어요, meh: 꿀꿀해
ALTER TABLE `user_movie_likes`
ADD COLUMN `VOTE_TYPE` VARCHAR(10) DEFAULT 'like' COMMENT '투표 타입 (like/unlike/meh)' AFTER `CONTENT_TYPE`;

-- 기존 데이터 마이그레이션: LIKE_YN 1->like, 0->unlike
UPDATE `user_movie_likes` SET `VOTE_TYPE` = IF(`LIKE_YN` = 1, 'like', 'unlike');
