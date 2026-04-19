-- 기본 관리자 계정 (아이디·비밀번호: admin/3819)
INSERT INTO `admin` (ADMIN_ID, ADMIN_EMAIL, PASSWORD, ADMIN_NAME, ROLE, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY)
VALUES (
    'admin/3819',
    'admin/3819',
    '$2b$12$5/X1Us7Bxv/hW7LKQ9G/9eFPH6QMpYALP3oJETgKAJrmaoMSfhVs6',
    '관리자',
    'ADMIN',
    NOW(),
    'system',
    NOW(),
    'system'
)
ON DUPLICATE KEY UPDATE MODIFIED_AT = NOW();
