-- ============================================================
-- 多计量单位换算演示数据
-- 创建日期：2026-07-31
--
-- 前置条件：已执行 ry_20260730_unit_conversion.sql，且存在
-- SQ_ROLL_BOX（平方米/卷/箱）单位组及对应公式。
-- 本脚本可以重复执行，不会重复创建分类或物料。
-- ============================================================

START TRANSACTION;

-- 一、卷材/面料分类
INSERT INTO material_category (
    parent_id, ancestors, category_code, category_name,
    order_num, status, create_by, create_time, remark
)
SELECT
    0, '0', 'ROLL_MATERIAL', '卷材/面料',
    20, '0', 'admin', NOW(), '多计量单位换算演示分类'
WHERE NOT EXISTS (
    SELECT 1 FROM material_category WHERE category_code = 'ROLL_MATERIAL'
);

SET @roll_category_id = (
    SELECT category_id
    FROM material_category
    WHERE category_code = 'ROLL_MATERIAL'
    LIMIT 1
);

-- 二、物料：兼容字段 unit 使用 SQ，采购换算不以该字段为中心
INSERT INTO material (
    material_code, material_name, material_type, category_id,
    spec, model, unit, unit_group_code,
    length, width, yards, standard_weight_per_sqm,
    source_type, lot_control_flag, shelf_life_control_flag,
    sn_control_flag, inspection_flag, safe_stock, status,
    create_by, create_time, remark
)
SELECT
    'ROLL-FABRIC-001', '涤纶面料卷', 'RAW', @roll_category_id,
    '幅宽1500mm，单卷50m', 'FABRIC-PET-1500', 'SQ', 'SQ_ROLL_BOX',
    50.0000, 1.5000, 1.0000, 0.1800,
    'PURCHASE', 'Y', 'N',
    'N', 'Y', 150.000000, '0',
    'admin', NOW(), '1卷=75平方米；1箱=4卷=300平方米'
WHERE NOT EXISTS (
    SELECT 1 FROM material WHERE material_code = 'ROLL-FABRIC-001'
);

INSERT INTO material (
    material_code, material_name, material_type, category_id,
    spec, model, unit, unit_group_code,
    length, width, yards, standard_weight_per_sqm,
    source_type, lot_control_flag, shelf_life_control_flag,
    sn_control_flag, inspection_flag, safe_stock, status,
    create_by, create_time, remark
)
SELECT
    'ROLL-FABRIC-002', 'PVC涂层布卷', 'RAW', @roll_category_id,
    '幅宽1800mm，单卷40m', 'PVC-COATED-1800', 'SQ', 'SQ_ROLL_BOX',
    40.0000, 1.8000, 1.0936, 0.6500,
    'PURCHASE', 'Y', 'N',
    'N', 'Y', 120.000000, '0',
    'admin', NOW(), '1卷约78.7392平方米；1箱按4卷换算'
WHERE NOT EXISTS (
    SELECT 1 FROM material WHERE material_code = 'ROLL-FABRIC-002'
);

INSERT INTO material (
    material_code, material_name, material_type, category_id,
    spec, model, unit, unit_group_code,
    length, width, yards, standard_weight_per_sqm,
    source_type, lot_control_flag, shelf_life_control_flag,
    sn_control_flag, inspection_flag, safe_stock, status,
    create_by, create_time, remark
)
SELECT
    'ROLL-FABRIC-003', '无纺布卷', 'RAW', @roll_category_id,
    '幅宽2000mm，单卷30m', 'NONWOVEN-2000', 'SQ', 'SQ_ROLL_BOX',
    30.0000, 2.0000, 1.0000, 0.1200,
    'PURCHASE', 'Y', 'N',
    'N', 'Y', 120.000000, '0',
    'admin', NOW(), '1卷=60平方米；1箱=4卷=240平方米'
WHERE NOT EXISTS (
    SELECT 1 FROM material WHERE material_code = 'ROLL-FABRIC-003'
);

COMMIT;

-- 结果核对
SELECT category_id, category_code, category_name
FROM material_category
WHERE category_code = 'ROLL_MATERIAL';

SELECT material_code, material_name, unit, unit_group_code,
       length, width, yards, standard_weight_per_sqm
FROM material
WHERE material_code IN (
    'ROLL-FABRIC-001', 'ROLL-FABRIC-002', 'ROLL-FABRIC-003'
)
ORDER BY material_code;
