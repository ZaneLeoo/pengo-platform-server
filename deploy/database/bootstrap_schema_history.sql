CREATE TABLE IF NOT EXISTS pm_schema_history (
    installed_rank BIGINT NOT NULL AUTO_INCREMENT COMMENT '安装顺序',
    version VARCHAR(50) NOT NULL COMMENT '版本号',
    description VARCHAR(200) NOT NULL COMMENT '变更说明',
    type VARCHAR(20) NOT NULL DEFAULT 'MIGRATION' COMMENT 'BASELINE/MIGRATION',
    script VARCHAR(255) NOT NULL COMMENT '脚本名称',
    checksum CHAR(64) NULL COMMENT 'SHA-256',
    installed_by VARCHAR(100) NOT NULL COMMENT '执行用户',
    installed_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    execution_time_ms BIGINT NOT NULL DEFAULT 0 COMMENT '执行耗时毫秒',
    success TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否成功',
    PRIMARY KEY (installed_rank),
    UNIQUE KEY uk_pm_schema_history_version (version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目管理数据库版本记录';
