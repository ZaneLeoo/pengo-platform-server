-- BOM 交付改为关联系统中的明确 BOM 版本，并保留每次提交的对象快照。

ALTER TABLE pm_project_deliverable_submission
    ADD COLUMN business_type VARCHAR(32) NULL COMMENT '业务对象类型' AFTER external_url,
    ADD COLUMN business_id VARCHAR(128) NULL COMMENT '业务对象ID' AFTER business_type,
    ADD COLUMN business_code VARCHAR(128) NULL COMMENT '业务对象编码快照' AFTER business_id,
    ADD COLUMN business_name VARCHAR(255) NULL COMMENT '业务对象名称快照' AFTER business_code,
    ADD COLUMN business_version VARCHAR(64) NULL COMMENT '业务对象版本快照' AFTER business_name,
    ADD INDEX idx_pm_deliverable_submission_business (business_type, business_id);

ALTER TABLE pm_project_deliverable
    ADD INDEX idx_pm_project_deliverable_business (business_type, business_id);

UPDATE pm_project_deliverable_type
SET submission_mode = 'BUSINESS_OBJECT', update_by = 'admin', update_time = SYSDATE()
WHERE type_code = 'BOM';

DELETE f
FROM pm_project_deliverable_type_format f
JOIN pm_project_deliverable_type t ON t.type_id = f.type_id
WHERE t.type_code = 'BOM';

-- 尚未形成有效交付结果的 BOM 要求同步使用新的提交方式；历史已交付文件保持不变。
UPDATE pm_project_deliverable
SET submission_mode = 'BUSINESS_OBJECT', allowed_extensions = NULL, update_time = SYSDATE()
WHERE deliverable_type = 'BOM'
  AND status IN ('PENDING', 'RETURNED');
