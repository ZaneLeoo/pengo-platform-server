-- ============================================================
-- 计量单位主档
-- 创建日期：2026-07-31
--
-- 计量单位只维护编码、名称和状态；单位组、主副属性、换算公式
-- 均由 unit_group_detail 维护。
-- ============================================================

CREATE TABLE IF NOT EXISTS unit (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    unit_code   VARCHAR(64)  NOT NULL COMMENT '单位编码',
    unit_name   VARCHAR(64)  NOT NULL COMMENT '单位名称',
    status      CHAR(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    create_by   VARCHAR(64)  NULL COMMENT '创建者',
    create_time DATETIME     NULL COMMENT '创建时间',
    update_by   VARCHAR(64)  NULL COMMENT '更新者',
    update_time DATETIME     NULL COMMENT '更新时间',
    remark      VARCHAR(500) NULL COMMENT '备注',
    UNIQUE KEY uk_unit_code (unit_code)
) COMMENT '计量单位主档';

START TRANSACTION;

INSERT INTO unit (unit_code, unit_name, status, create_by, create_time, remark)
SELECT 'SQ', '平方米', '0', 'admin', NOW(), '卷材面积库存基准单位'
WHERE NOT EXISTS (SELECT 1 FROM unit WHERE unit_code = 'SQ');

INSERT INTO unit (unit_code, unit_name, status, create_by, create_time, remark)
SELECT 'ROL', '卷', '0', 'admin', NOW(), '卷材辅助单位'
WHERE NOT EXISTS (SELECT 1 FROM unit WHERE unit_code = 'ROL');

INSERT INTO unit (unit_code, unit_name, status, create_by, create_time, remark)
SELECT 'BOX', '箱', '0', 'admin', NOW(), '卷材辅助单位'
WHERE NOT EXISTS (SELECT 1 FROM unit WHERE unit_code = 'BOX');

INSERT INTO unit (unit_code, unit_name, status, create_by, create_time, remark)
SELECT 'M', '米', '0', 'admin', NOW(), '长度单位'
WHERE NOT EXISTS (SELECT 1 FROM unit WHERE unit_code = 'M');

INSERT INTO unit (unit_code, unit_name, status, create_by, create_time, remark)
SELECT 'KG', '千克', '0', 'admin', NOW(), '重量单位'
WHERE NOT EXISTS (SELECT 1 FROM unit WHERE unit_code = 'KG');

INSERT INTO unit (unit_code, unit_name, status, create_by, create_time, remark)
SELECT 'PCS', '个', '0', 'admin', NOW(), '数量单位'
WHERE NOT EXISTS (SELECT 1 FROM unit WHERE unit_code = 'PCS');

UPDATE unit_group_detail d
JOIN unit u ON u.unit_code = d.unit_code
SET d.unit_name = u.unit_name;

UPDATE unit_group_detail d
JOIN unit_group g ON g.id = d.group_id
SET d.sort_order = CASE d.unit_code
    WHEN 'SQ' THEN 10
    WHEN 'ROL' THEN 20
    WHEN 'BOX' THEN 30
    ELSE d.sort_order
END
WHERE g.group_code = 'SQ_ROLL_BOX';

COMMIT;
