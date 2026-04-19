-- 접속 플랫폼 통계용 (Android / iOS / Web 유입 집계)
CREATE TABLE IF NOT EXISTS `access_logs`
(
    ACCESS_LOG_ID BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'PK',
    USER_ID       VARCHAR(255) NULL COMMENT '사용자 ID (비로그인 시 NULL)',
    PLATFORM      VARCHAR(20)  NOT NULL COMMENT '접속 플랫폼 (ANDROID, IOS, WEB, UNKNOWN)',
    REQ_IP        VARCHAR(45)  NULL COMMENT '요청 IP',
    CREATED_AT    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '접속 시각',

    PRIMARY KEY (ACCESS_LOG_ID),
    INDEX idx_access_logs_created_at (CREATED_AT),
    INDEX idx_access_logs_platform (PLATFORM)
);
