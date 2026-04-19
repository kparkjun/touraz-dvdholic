-- App Store Guideline 5.1.1: 전화번호를 필수가 아닌 선택 입력으로 변경
ALTER TABLE `users` MODIFY COLUMN `PHONE` VARCHAR(255) NULL COMMENT '전화번호 (선택)';
