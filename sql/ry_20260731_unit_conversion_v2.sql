-- ============================================================
-- 多计量单位换算模型调整
-- 1. 单位组不再固定主单位
-- 2. 历史物料 unit 作为库存兼容字段（后续采购换算不再以它为中心）
-- 3. 公式增加精度和舍入规则
-- 仅适用于已执行旧版脚本的现有数据库，仅执行一次；新建数据库请执行
-- ry_20260730_unit_conversion.sql，不要重复执行本脚本。
-- ============================================================

-- 旧模型存在主单位标志时，先为缺少库存基准单位的物料补齐数据。
UPDATE material m
JOIN unit_group g ON g.group_code = m.unit_group_code
JOIN unit_group_detail d ON d.group_id = g.id AND d.main_unit_flag = 'Y'
SET m.unit = d.unit_code
WHERE (m.unit IS NULL OR m.unit = '');

ALTER TABLE unit_conversion_formula
    ADD COLUMN decimal_scale INT NOT NULL DEFAULT 4 COMMENT '公式结果小数位数(0-6)',
    ADD COLUMN rounding_mode VARCHAR(16) NOT NULL DEFAULT 'HALF_UP' COMMENT '公式结果舍入模式';

ALTER TABLE purchase_order_line
    MODIFY COLUMN sub_unit1_qty DECIMAL(20,6) NULL COMMENT '换算单位数量1',
    MODIFY COLUMN sub_unit2_qty DECIMAL(20,6) NULL COMMENT '换算单位数量2';

ALTER TABLE unit_group_detail
    DROP COLUMN main_unit_flag;
