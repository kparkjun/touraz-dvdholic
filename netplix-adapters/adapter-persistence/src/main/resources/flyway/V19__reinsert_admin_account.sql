-- 기존 admin/3819 레코드 삭제 후 재삽입 (admin 아이디, 비밀번호 3819)
DELETE FROM `admin` WHERE ADMIN_ID = 'admin/3819';

INSERT INTO `admin` (ADMIN_ID, ADMIN_EMAIL, PASSWORD, ADMIN_NAME, ROLE, CREATED_AT, CREATED_BY, MODIFIED_AT, MODIFIED_BY)
VALUES (
    'admin',
    'admin@touraz-dvdholic.com',
    '$2a$10$u0l0cgHMUv3mOgO28Uox4eQlBImDsY8L0sbj1MdmCjFB5BFfRLA0a',
    'admin',
    'ADMIN',
    NOW(),
    'system',
    NOW(),
    'system'
);
