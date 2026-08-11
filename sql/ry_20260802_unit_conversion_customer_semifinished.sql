-- ============================================================
-- 客户示例：平方米-KG-支（普通半成品类）
-- 创建日期：2026-08-02
--
-- 客户规则：
--   单支半成品 KG = (宽 - 0.02) * 长 * 每平方标准重量
--
-- 说明：
--   1. 长、宽使用米；standardWeightPerSqm 使用 kg/m²。
--   2. widthLoss 以公式默认参数 0.02 记录，可在公式配置中调整。
--   3. Excel 未提供具体物料的长、宽和标准重量，本脚本不虚构物料数据。
--   4. 公式使用单位组级别，支持从 SQ、KG、EA 任意一个单位开始换算。
--
-- 前置脚本：
--   ry_20260730_unit_conversion.sql
--   ry_20260731_unit_master.sql
-- ============================================================

START TRANSACTION;

-- 一、单位主档：支
INSERT INTO unit (unit_code, unit_name, status, create_by, create_time, remark)
SELECT 'EA', '支', '0', 'admin', NOW(), '普通半成品计数单位'
WHERE NOT EXISTS (
    SELECT 1 FROM unit WHERE unit_code = 'EA'
);

-- 二、单位组：平方米-KG-支
INSERT INTO unit_group (group_code, group_name, group_type, status,
                        create_by, create_time, remark)
SELECT 'SQ_KG_EA', '平方米-KG-支', 'SEMI_FINISHED', '0',
       'admin', NOW(), '客户示例：普通半成品按平方米、KG、支换算'
WHERE NOT EXISTS (
    SELECT 1 FROM unit_group WHERE group_code = 'SQ_KG_EA'
);

SET @sq_kg_ea_group_id = (
    SELECT id FROM unit_group WHERE group_code = 'SQ_KG_EA' LIMIT 1
);

-- 三、单位组成员；成员没有主次之分，排序仅用于展示
INSERT INTO unit_group_detail (group_id, unit_code, unit_name,
                               formula_id, sort_order,
                               create_by, create_time, remark)
SELECT @sq_kg_ea_group_id, 'SQ', '平方米', NULL, 10,
       'admin', NOW(), '面积单位'
WHERE NOT EXISTS (
    SELECT 1 FROM unit_group_detail
    WHERE group_id = @sq_kg_ea_group_id AND unit_code = 'SQ'
);

INSERT INTO unit_group_detail (group_id, unit_code, unit_name,
                               formula_id, sort_order,
                               create_by, create_time, remark)
SELECT @sq_kg_ea_group_id, 'KG', '千克', NULL, 20,
       'admin', NOW(), '重量单位'
WHERE NOT EXISTS (
    SELECT 1 FROM unit_group_detail
    WHERE group_id = @sq_kg_ea_group_id AND unit_code = 'KG'
);

INSERT INTO unit_group_detail (group_id, unit_code, unit_name,
                               formula_id, sort_order,
                               create_by, create_time, remark)
SELECT @sq_kg_ea_group_id, 'EA', '支', NULL, 30,
       'admin', NOW(), '半成品计数单位'
WHERE NOT EXISTS (
    SELECT 1 FROM unit_group_detail
    WHERE group_id = @sq_kg_ea_group_id AND unit_code = 'EA'
);

-- 四、支 -> 平方米：单支有效面积
INSERT INTO unit_conversion_formula (
    formula_code, formula_name, formula_type, expression,
    unit_group_id, input_unit, output_unit,
    scope_type, scope_id, reverse_mode, reverse_expression,
    decimal_scale, rounding_mode,
    param1_name, param1_field, param1_default,
    param2_name, param2_field, param2_default,
    param3_name, param3_field, param3_default,
    param4_name, param4_field, param4_default,
    param5_name, param5_field, param5_default,
    is_active, sort_order, status, create_by, create_time, remark
)
SELECT
    'EA_TO_SQM_SEMI', '支转平方米（扣边0.02m）', 'CUSTOM',
    '(${width} - ${widthLoss}) * ${length}',
    @sq_kg_ea_group_id, 'EA', 'SQ',
    'UNIT_GROUP', NULL, 'DIVIDE', NULL,
    6, 'HALF_UP',
    'width', 'width', NULL,
    'length', 'length', NULL,
    'widthLoss', NULL, 0.02,
    NULL, NULL, NULL,
    NULL, NULL, NULL,
    'Y', 10, '0', 'admin', NOW(),
    '客户示例：单支平方米=(宽-0.02)*长'
WHERE NOT EXISTS (
    SELECT 1 FROM unit_conversion_formula
    WHERE formula_code = 'EA_TO_SQM_SEMI'
);

-- 五、支 -> KG：单支重量
-- standardWeightPerSqm 使用 Java 物料字段名，以便公式引擎反射读取。
INSERT INTO unit_conversion_formula (
    formula_code, formula_name, formula_type, expression,
    unit_group_id, input_unit, output_unit,
    scope_type, scope_id, reverse_mode, reverse_expression,
    decimal_scale, rounding_mode,
    param1_name, param1_field, param1_default,
    param2_name, param2_field, param2_default,
    param3_name, param3_field, param3_default,
    param4_name, param4_field, param4_default,
    param5_name, param5_field, param5_default,
    is_active, sort_order, status, create_by, create_time, remark
)
SELECT
    'EA_TO_KG_SEMI', '支转千克（扣边0.02m）', 'WEIGHT_FROM_DIMS',
    '(${width} - ${widthLoss}) * ${length} * ${standardWeightPerSqm}',
    @sq_kg_ea_group_id, 'EA', 'KG',
    'UNIT_GROUP', NULL, 'DIVIDE', NULL,
    6, 'HALF_UP',
    'width', 'width', NULL,
    'length', 'length', NULL,
    'widthLoss', NULL, 0.02,
    'standardWeightPerSqm', 'standardWeightPerSqm', NULL,
    NULL, NULL, NULL,
    'Y', 20, '0', 'admin', NOW(),
    '客户示例：单支KG=(宽-0.02)*长*每平方标准重量'
WHERE NOT EXISTS (
    SELECT 1 FROM unit_conversion_formula
    WHERE formula_code = 'EA_TO_KG_SEMI'
);

COMMIT;

-- 结果核对
SELECT id, group_code, group_name, group_type, status
FROM unit_group
WHERE group_code = 'SQ_KG_EA';

SELECT d.group_id, d.unit_code, d.unit_name, d.sort_order
FROM unit_group_detail d
JOIN unit_group g ON g.id = d.group_id
WHERE g.group_code = 'SQ_KG_EA'
ORDER BY d.sort_order, d.id;

SELECT formula_code, formula_name, input_unit, output_unit,
       expression, scope_type, reverse_mode, decimal_scale
FROM unit_conversion_formula
WHERE formula_code IN ('EA_TO_SQM_SEMI', 'EA_TO_KG_SEMI')
ORDER BY sort_order, id;
