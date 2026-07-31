-- ============================================================
-- 多计量单位采购行对称模型迁移
-- 1. 采购行不再以 material.unit 作为换算中心
-- 2. 采购行保存录入单位及单位组内三个单位的数量快照
-- 3. 旧 unit/order_quantity 保留为兼容字段，保存时等于录入单位/录入数量
-- 仅适用于已执行旧版单位换算脚本和 v2 脚本的现有数据库，执行一次。
-- ============================================================

ALTER TABLE purchase_order_line
    ADD COLUMN input_qty        DECIMAL(20,6) NULL COMMENT '录入数量',
    ADD COLUMN unit1_code       VARCHAR(64)  NULL COMMENT '单位组成员1编码',
    ADD COLUMN unit1_name       VARCHAR(64)  NULL COMMENT '单位组成员1名称',
    ADD COLUMN unit1_qty        DECIMAL(20,6) NULL COMMENT '单位组成员1数量',
    ADD COLUMN unit2_code       VARCHAR(64)  NULL COMMENT '单位组成员2编码',
    ADD COLUMN unit2_name       VARCHAR(64)  NULL COMMENT '单位组成员2名称',
    ADD COLUMN unit2_qty        DECIMAL(20,6) NULL COMMENT '单位组成员2数量',
    ADD COLUMN unit3_code       VARCHAR(64)  NULL COMMENT '单位组成员3编码',
    ADD COLUMN unit3_name       VARCHAR(64)  NULL COMMENT '单位组成员3名称',
    ADD COLUMN unit3_qty        DECIMAL(20,6) NULL COMMENT '单位组成员3数量';

-- 旧数据无法恢复历史录入单位对应的原始数量时，以旧采购数量作为兼容回填值。
-- 新增和修改的采购行由服务端重新计算三个单位数量。
UPDATE purchase_order_line pol
LEFT JOIN unit u0 ON u0.unit_code = pol.unit
LEFT JOIN unit u1 ON u1.unit_code = pol.sub_unit1
LEFT JOIN unit u2 ON u2.unit_code = pol.sub_unit2
SET pol.input_unit_code = COALESCE(NULLIF(pol.input_unit_code, ''), pol.unit),
    pol.input_unit_name = COALESCE(NULLIF(pol.input_unit_name, ''), u0.unit_name, pol.unit),
    pol.input_qty = COALESCE(pol.input_qty, pol.order_quantity),
    pol.unit1_code = COALESCE(pol.unit1_code, pol.unit),
    pol.unit1_name = COALESCE(pol.unit1_name, u0.unit_name, pol.unit),
    pol.unit1_qty = COALESCE(pol.unit1_qty, pol.order_quantity),
    pol.unit2_code = COALESCE(pol.unit2_code, pol.sub_unit1),
    pol.unit2_name = COALESCE(pol.unit2_name, u1.unit_name, pol.sub_unit1),
    pol.unit2_qty = COALESCE(pol.unit2_qty, pol.sub_unit1_qty),
    pol.unit3_code = COALESCE(pol.unit3_code, pol.sub_unit2),
    pol.unit3_name = COALESCE(pol.unit3_name, u2.unit_name, pol.sub_unit2),
    pol.unit3_qty = COALESCE(pol.unit3_qty, pol.sub_unit2_qty);
