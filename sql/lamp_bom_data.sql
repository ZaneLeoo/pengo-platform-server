-- ==========================================
-- MES 系统：多级自制台灯及BOM多级测试数据脚本
-- 适用数据库：MySQL
-- 执行方式：在 MySQL / Navicat 中直接运行此脚本
-- ==========================================

-- 1. 新增物料分类
INSERT INTO material_category (category_code, category_name, parent_id, ancestors, order_num, status, create_by, create_time, remark)
VALUES ('CAT-LAMP', '台灯系列', 0, '0', 10, '0', 'admin', SYSDATE(), '智能台灯产品分类');
SET @cat_id = LAST_INSERT_ID();

-- 2. 新增物料主数据 (所有物料均为自制 MAKE)
-- 2.1 插入成品：智能台灯
INSERT INTO material (material_code, material_name, material_type, category_id, spec, model, unit, source_type, status, lot_control_flag, sn_control_flag, inspection_flag, safe_stock, create_by, create_time, remark)
VALUES ('FIN-001', '智能台灯', 'FINISHED', @cat_id, 'LED-12W', 'TD-PRO', '台', 'MAKE', '0', 'N', 'Y', 'Y', 10.000000, 'admin', SYSDATE(), '智能LED台灯成品');
SET @m_lamp_id = LAST_INSERT_ID();

-- 2.2 插入自制半成品：台灯灯罩组件
INSERT INTO material (material_code, material_name, material_type, category_id, spec, model, unit, source_type, status, lot_control_flag, sn_control_flag, inspection_flag, safe_stock, create_by, create_time, remark)
VALUES ('SEMI-001', '台灯灯罩组件', 'SEMI_FINISHED', @cat_id, 'DZ-V1', 'TD-DZ', '套', 'MAKE', '0', 'Y', 'N', 'Y', 20.000000, 'admin', SYSDATE(), '自制台灯灯罩半成品');
SET @m_shade_id = LAST_INSERT_ID();

-- 2.3 插入自制原材料/子件物料
INSERT INTO material (material_code, material_name, material_type, category_id, spec, model, unit, source_type, status, lot_control_flag, sn_control_flag, inspection_flag, safe_stock, create_by, create_time, remark)
VALUES ('RAW-001', '塑料灯罩主体', 'RAW', @cat_id, 'PP-ABS', 'SL-HZ', '个', 'MAKE', '0', 'N', 'N', 'N', 100.000000, 'admin', SYSDATE(), '灯罩塑料注塑件');
SET @m_pp_id = LAST_INSERT_ID();

INSERT INTO material (material_code, material_name, material_type, category_id, spec, model, unit, source_type, status, lot_control_flag, sn_control_flag, inspection_flag, safe_stock, create_by, create_time, remark)
VALUES ('RAW-002', 'LED发光板组件', 'RAW', @cat_id, 'LED-3V-5W', 'LED-V1', '个', 'MAKE', '0', 'Y', 'N', 'Y', 150.000000, 'admin', SYSDATE(), '灯头光源发光板');
SET @m_led_id = LAST_INSERT_ID();

INSERT INTO material (material_code, material_name, material_type, category_id, spec, model, unit, source_type, status, lot_control_flag, sn_control_flag, inspection_flag, safe_stock, create_by, create_time, remark)
VALUES ('RAW-003', '灯座底座支架', 'RAW', @cat_id, 'METAL-H20', 'ZJ-01', '个', 'MAKE', '0', 'N', 'N', 'N', 50.000000, 'admin', SYSDATE(), '台灯金属配重底座与支架');
SET @m_metal_id = LAST_INSERT_ID();

INSERT INTO material (material_code, material_name, material_type, category_id, spec, model, unit, source_type, status, lot_control_flag, sn_control_flag, inspection_flag, safe_stock, create_by, create_time, remark)
VALUES ('RAW-004', '控制开关板', 'RAW', @cat_id, 'PCB-SWITCH', 'PCB-V1', '个', 'MAKE', '0', 'Y', 'N', 'Y', 80.000000, 'admin', SYSDATE(), '触摸控制电路板');
SET @m_pcb_id = LAST_INSERT_ID();

INSERT INTO material (material_code, material_name, material_type, category_id, spec, model, unit, source_type, status, lot_control_flag, sn_control_flag, inspection_flag, safe_stock, create_by, create_time, remark)
VALUES ('RAW-005', 'USB电源线', 'RAW', @cat_id, 'USB-C-1.5M', 'CABLE-C', '根', 'MAKE', '0', 'N', 'N', 'N', 200.000000, 'admin', SYSDATE(), 'TYPE-C供电电源线');
SET @m_cable_id = LAST_INSERT_ID();


-- 3. 新增【台灯灯罩组件】的BOM (半成品BOM)
-- 3.1 插入BOM Master
INSERT INTO bom_master (bom_code, parent_item_id, parent_item_code, parent_item_name, parent_item_spec, parent_item_unit, bom_type, status, source_system, create_by, create_time, remark)
VALUES ('BOM-SEMI-001', @m_shade_id, 'SEMI-001', '台灯灯罩组件', 'DZ-V1', '套', 'MANUFACTURING', 'ENABLED', 'MANUAL', 'admin', SYSDATE(), '台灯灯罩组件生产BOM');
SET @b_shade_master_id = LAST_INSERT_ID();

-- 3.2 插入BOM Version
INSERT INTO bom_version (bom_master_id, version_code, version_name, version_desc, base_qty, usage_type, effective_date, expire_date, status, approve_status, default_flag, create_by, create_time, remark)
VALUES (@b_shade_master_id, 'V1.0', '台灯灯罩组件标准版', '初代设计生效标准BOM', 1.000000, 'GENERAL', '2026-06-26', '2036-06-26', 'ENABLED', 'APPROVED', 1, 'admin', SYSDATE(), '系统自动生成');
SET @b_shade_version_id = LAST_INSERT_ID();

-- 更新默认BOM ID关联回物料表
UPDATE material SET default_bom_id = @b_shade_master_id WHERE material_id = @m_shade_id;

-- 3.3 插入BOM子件明细 (包含塑料灯罩主体 RAW-001 与 LED发光板组件 RAW-002)
INSERT INTO bom_item (bom_version_id, line_no, parent_item_code, component_item_id, component_item_code, component_item_name, component_item_spec, component_item_unit, component_qty, fixed_loss_qty, change_loss_rate, supply_type, is_virtual, mrp_expand_flag, create_by, create_time, remark)
VALUES (@b_shade_version_id, 10, 'SEMI-001', @m_pp_id, 'RAW-001', '塑料灯罩主体', 'PP-ABS', '个', 1.000000, 0.000000, 2.000000, 'PUSH', 0, 1, 'admin', SYSDATE(), '灯罩塑料件 用量1 损耗率2%');

INSERT INTO bom_item (bom_version_id, line_no, parent_item_code, component_item_id, component_item_code, component_item_name, component_item_spec, component_item_unit, component_qty, fixed_loss_qty, change_loss_rate, supply_type, is_virtual, mrp_expand_flag, create_by, create_time, remark)
VALUES (@b_shade_version_id, 20, 'SEMI-001', @m_led_id, 'RAW-002', 'LED发光板组件', 'LED-3V-5W', '个', 1.000000, 0.000000, 0.500000, 'PUSH', 0, 1, 'admin', SYSDATE(), '光源发光板 用量1');


-- 4. 新增【智能台灯】的BOM (成品BOM - 引用半成品灯罩)
-- 4.1 插入BOM Master
INSERT INTO bom_master (bom_code, parent_item_id, parent_item_code, parent_item_name, parent_item_spec, parent_item_unit, bom_type, status, source_system, create_by, create_time, remark)
VALUES ('BOM-FIN-001', @m_lamp_id, 'FIN-001', '智能台灯', 'LED-12W', '台', 'MANUFACTURING', 'ENABLED', 'MANUAL', 'admin', SYSDATE(), '智能台灯成品生产BOM');
SET @b_lamp_master_id = LAST_INSERT_ID();

-- 4.2 插入BOM Version
INSERT INTO bom_version (bom_master_id, version_code, version_name, version_desc, base_qty, usage_type, effective_date, expire_date, status, approve_status, default_flag, create_by, create_time, remark)
VALUES (@b_lamp_master_id, 'V1.0', '智能台灯标准量产版', '标准智能台灯多级结构BOM', 1.000000, 'GENERAL', '2026-06-26', '2036-06-26', 'ENABLED', 'APPROVED', 1, 'admin', SYSDATE(), '系统自动生成');
SET @b_lamp_version_id = LAST_INSERT_ID();

-- 更新默认BOM ID关联回物料表
UPDATE material SET default_bom_id = @b_lamp_master_id WHERE material_id = @m_lamp_id;

-- 4.3 插入BOM子件明细 (包含台灯灯罩组件半成品及其他底座电路配线)
-- 子件1：自制半成品「台灯灯罩组件」
INSERT INTO bom_item (bom_version_id, line_no, parent_item_code, component_item_id, component_item_code, component_item_name, component_item_spec, component_item_unit, component_qty, fixed_loss_qty, change_loss_rate, supply_type, is_virtual, mrp_expand_flag, create_by, create_time, remark)
VALUES (@b_lamp_version_id, 10, 'FIN-001', @m_shade_id, 'SEMI-001', '台灯灯罩组件', 'DZ-V1', '套', 1.000000, 0.000000, 0.000000, 'PUSH', 0, 1, 'admin', SYSDATE(), '核心半成品：灯罩发光组件 用量1');

-- 子件2：金属支架
INSERT INTO bom_item (bom_version_id, line_no, parent_item_code, component_item_id, component_item_code, component_item_name, component_item_spec, component_item_unit, component_qty, fixed_loss_qty, change_loss_rate, supply_type, is_virtual, mrp_expand_flag, create_by, create_time, remark)
VALUES (@b_lamp_version_id, 20, 'FIN-001', @m_metal_id, 'RAW-003', '灯座底座支架', 'METAL-H20', '个', 1.000000, 0.000000, 1.000000, 'PUSH', 0, 1, 'admin', SYSDATE(), '支架 用量1');

-- 子件3：控制开关板
INSERT INTO bom_item (bom_version_id, line_no, parent_item_code, component_item_id, component_item_code, component_item_name, component_item_spec, component_item_unit, component_qty, fixed_loss_qty, change_loss_rate, supply_type, is_virtual, mrp_expand_flag, create_by, create_time, remark)
VALUES (@b_lamp_version_id, 30, 'FIN-001', @m_pcb_id, 'RAW-004', '控制开关板', 'PCB-SWITCH', '个', 1.000000, 0.000000, 0.500000, 'PUSH', 0, 1, 'admin', SYSDATE(), '控制板 用量1');

-- 子件4：电源线
INSERT INTO bom_item (bom_version_id, line_no, parent_item_code, component_item_id, component_item_code, component_item_name, component_item_spec, component_item_unit, component_qty, fixed_loss_qty, change_loss_rate, supply_type, is_virtual, mrp_expand_flag, create_by, create_time, remark)
VALUES (@b_lamp_version_id, 40, 'FIN-001', @m_cable_id, 'RAW-005', 'USB电源线', 'USB-C-1.5M', '根', 1.000000, 0.000000, 0.000000, 'PUSH', 0, 1, 'admin', SYSDATE(), '电源线 用量1');
