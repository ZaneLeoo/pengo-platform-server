-- BOM AI 多图识别演示数据重置
--
-- 清理范围：物料分类、物料、BOM，以及会引用旧物料的采购和库存演示数据。
-- 保留供应商、仓库、库位、系统菜单、Agent 和 Dify 配置。
-- 脚本使用固定编码，便于识别结果中的编码直接匹配物料主数据。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE inventory_transaction;
TRUNCATE TABLE inventory_balance;
TRUNCATE TABLE purchase_inbound_line;
TRUNCATE TABLE purchase_inbound;
TRUNCATE TABLE purchase_receipt_line;
TRUNCATE TABLE purchase_receipt;
TRUNCATE TABLE purchase_order_line;
TRUNCATE TABLE purchase_order;
TRUNCATE TABLE purchase_supplier_quote_line;
TRUNCATE TABLE purchase_supplier_quote;
TRUNCATE TABLE bom_item;
TRUNCATE TABLE bom_version;
TRUNCATE TABLE bom_master;
TRUNCATE TABLE material;
TRUNCATE TABLE material_category;

INSERT INTO material_category
    (category_id, parent_id, ancestors, category_code, category_name, order_num, status, create_by, create_time, remark)
VALUES
    (1, 0, '0', 'FINISHED', '成品', 1, '0', 'admin', NOW(), '最终产品'),
    (2, 0, '0', 'SEMI_FINISHED', '半成品', 2, '0', 'admin', NOW(), '装配组件'),
    (3, 0, '0', 'ELECTRONIC', '电子元器件', 3, '0', 'admin', NOW(), '光源、电源和控制器件'),
    (4, 0, '0', 'STRUCTURAL', '结构件', 4, '0', 'admin', NOW(), '灯杆、外壳和机构件'),
    (5, 0, '0', 'HARDWARE', '紧固件', 5, '0', 'admin', NOW(), '螺钉、垫圈和限位件'),
    (6, 0, '0', 'PACKAGE', '包装及辅料', 6, '0', 'admin', NOW(), '包装材料和产品附件');

INSERT INTO material
    (material_id, material_code, material_name, material_type, category_id, spec, model, unit, drawing_no,
     material_version, source_type, lot_control_flag, shelf_life_control_flag, shelf_life_days,
     expiry_warning_days, sn_control_flag, inspection_flag, safe_stock, status, create_by, create_time, remark)
VALUES
    (1, 'TL-1000-1200', '灯杆组件（半成品）', 'SEMI_FINISHED', 2, '', NULL, '套', 'TL-1000-1200-0000', 'A', 'MAKE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), 'AI识别母件'),
    (2, 'TL-1000-1100', '灯头组件（半成品）', 'SEMI_FINISHED', 2, '', NULL, '套', 'TL-1000-1100-0000', 'A', 'MAKE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), 'AI识别母件'),
    (3, 'TL-1000', '智能护眼台灯（成品）', 'FINISHED', 1, '', NULL, '台', 'TL-1000-0000', 'A', 'MAKE', 'N', 'N', NULL, NULL, 'Y', 'Y', 0, '0', 'admin', NOW(), 'AI识别母件'),
    (4, 'TL-1000-1300', '底座组件', 'SEMI_FINISHED', 2, '/', NULL, '套', NULL, NULL, 'MAKE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '灯具底座半成品'),
    (5, 'TL-1000-1400', '控制面板组件', 'SEMI_FINISHED', 2, '/', NULL, '套', NULL, NULL, 'MAKE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '灯具控制面板半成品'),

    (6, '60001201', '上灯杆', 'RAW', 4, '铝合金/银色/Φ16', NULL, '根', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '阳极氧化'),
    (7, '60001202', '下灯杆', 'RAW', 4, '铝合金/银色/Φ20', NULL, '根', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '阳极氧化'),
    (8, '60001203', '转轴组件', 'RAW', 4, 'Φ20*60', NULL, '个', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '调节角度'),
    (9, '60001204', '阻尼轴', 'RAW', 4, 'Φ8*40', NULL, '个', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '阻尼调节'),
    (10, '60001205', '限位片', 'RAW', 5, '不锈钢/1.0mm', NULL, '片', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '角度限位'),
    (11, '60001206', '转轴盖', 'RAW', 4, 'ABS/白色', NULL, '个', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '装饰盖'),
    (12, '60001207', '内六角螺钉', 'RAW', 5, 'M4*10', NULL, '个', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '不锈钢'),
    (13, '60001208', '弹簧垫圈', 'RAW', 5, 'M4', NULL, '个', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '不锈钢'),
    (14, '60001209', '平垫圈', 'RAW', 5, 'M4', NULL, '个', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '不锈钢'),

    (15, '60001101', 'LED灯板', 'RAW', 3, '2835/0.5W/4000K', NULL, '个', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '高显色Ra>95'),
    (16, '60001102', '驱动电源板', 'RAW', 3, '24V/0.8A', NULL, '个', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '恒流驱动'),
    (17, '60001103', '导光板', 'RAW', 4, 'PC/3mm', NULL, '个', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '透光率>92%'),
    (18, '60001104', '扩散板', 'RAW', 4, 'PC/2mm', NULL, '个', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '均匀光效'),
    (19, '60001105', '反光纸', 'RAW', 6, '铝箔反光膜', NULL, '张', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '提升光效'),
    (20, '60001106', '灯罩', 'RAW', 4, 'PC/磨砂', NULL, '个', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '防眩光'),
    (21, '60001107', '灯头外壳上盖', 'RAW', 4, 'ABS/白色', NULL, '个', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '注塑件'),
    (22, '60001108', '灯头外壳下盖', 'RAW', 4, 'ABS/白色', NULL, '个', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '注塑件'),
    (23, '60001109', '固定螺钉', 'RAW', 5, 'M2*6', NULL, '个', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '不锈钢'),
    (24, '60001110', '散热片', 'RAW', 4, '铝型材', NULL, '个', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '散热用'),

    (25, '60001023', '电源适配器', 'RAW', 3, '24V/1A', NULL, '个', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '输入AC100-240V'),
    (26, '60001024', '电源线', 'RAW', 3, '2*0.75mm²/1.5m', NULL, '根', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '国标两插'),
    (27, '60001025', '硅胶防滑垫', 'RAW', 4, 'Φ50*2mm', NULL, '个', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '底部防滑'),
    (28, '60001026', '产品铭牌', 'RAW', 6, '铝牌/印刷', NULL, '个', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '含品牌、参数等信息'),
    (29, '60001027', '包装盒', 'RAW', 6, '350*180*120mm', NULL, '个', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'Y', 0, '0', 'admin', NOW(), '彩盒'),
    (30, '60001028', '说明书', 'RAW', 6, 'A5/多语言', NULL, '份', NULL, NULL, 'PURCHASE', 'N', 'N', NULL, NULL, 'N', 'N', 0, '0', 'admin', NOW(), '用户使用说明书');

INSERT INTO bom_master
    (id, bom_code, parent_item_id, parent_item_code, parent_item_name, parent_item_spec, parent_item_unit,
     bom_type, status, source_system, source_id, create_by, create_time, remark)
VALUES
    (1, 'BOM-TL-1000-1200', 1, 'TL-1000-1200', '灯杆组件（半成品）', '', '套', 'MANUFACTURING', 'ENABLED', 'AI_IMPORT', 'BOM_OCR_PAGE_1', 'admin', NOW(), 'AI识别演示数据'),
    (2, 'BOM-TL-1000-1100', 2, 'TL-1000-1100', '灯头组件（半成品）', '', '套', 'MANUFACTURING', 'ENABLED', 'AI_IMPORT', 'BOM_OCR_PAGE_2', 'admin', NOW(), 'AI识别演示数据'),
    (3, 'BOM-TL-1000', 3, 'TL-1000', '智能护眼台灯（成品）', '', '台', 'MANUFACTURING', 'ENABLED', 'AI_IMPORT', 'BOM_OCR_PAGE_3', 'admin', NOW(), 'AI识别演示数据');

INSERT INTO bom_version
    (id, bom_master_id, version_code, version_name, version_desc, base_qty, usage_type, effective_date,
     status, approve_status, default_flag, source_system, source_id, check_by, check_time, create_by, create_time, remark)
VALUES
    (1, 1, 'V1.0', 'AI识别版本', '灯杆组件半成品 BOM', 1, 'GENERAL', CURDATE(), 'EFFECTIVE', 'APPROVED', 1, 'AI_IMPORT', 'BOM_OCR_PAGE_1', 'admin', NOW(), 'admin', NOW(), 'AI识别演示数据'),
    (2, 2, 'V1.0', 'AI识别版本', '灯头组件半成品 BOM', 1, 'GENERAL', CURDATE(), 'EFFECTIVE', 'APPROVED', 1, 'AI_IMPORT', 'BOM_OCR_PAGE_2', 'admin', NOW(), 'admin', NOW(), 'AI识别演示数据'),
    (3, 3, 'V1.0', 'AI识别版本', '智能护眼台灯成品 BOM', 1, 'GENERAL', CURDATE(), 'EFFECTIVE', 'APPROVED', 1, 'AI_IMPORT', 'BOM_OCR_PAGE_3', 'admin', NOW(), 'admin', NOW(), 'AI识别演示数据');

INSERT INTO bom_item
    (bom_version_id, line_no, parent_item_code, component_item_id, component_item_code, component_item_name,
     component_item_spec, component_item_unit, component_qty, fixed_loss_qty, change_loss_rate, supply_type,
     is_virtual, mrp_expand_flag, source_system, create_by, create_time, remark)
VALUES
    (1, 1, 'TL-1000-1200', 6, '60001201', '上灯杆', '铝合金/银色/Φ16', '根', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '阳极氧化'),
    (1, 2, 'TL-1000-1200', 7, '60001202', '下灯杆', '铝合金/银色/Φ20', '根', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '阳极氧化'),
    (1, 3, 'TL-1000-1200', 8, '60001203', '转轴组件', 'Φ20*60', '个', 2, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '调节角度'),
    (1, 4, 'TL-1000-1200', 9, '60001204', '阻尼轴', 'Φ8*40', '个', 2, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '阻尼调节'),
    (1, 5, 'TL-1000-1200', 10, '60001205', '限位片', '不锈钢/1.0mm', '片', 2, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '角度限位'),
    (1, 6, 'TL-1000-1200', 11, '60001206', '转轴盖', 'ABS/白色', '个', 2, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '装饰盖'),
    (1, 7, 'TL-1000-1200', 12, '60001207', '内六角螺钉', 'M4*10', '个', 4, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '不锈钢'),
    (1, 8, 'TL-1000-1200', 13, '60001208', '弹簧垫圈', 'M4', '个', 4, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '不锈钢'),
    (1, 9, 'TL-1000-1200', 14, '60001209', '平垫圈', 'M4', '个', 4, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '不锈钢'),

    (2, 1, 'TL-1000-1100', 15, '60001101', 'LED灯板', '2835/0.5W/4000K', '个', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '高显色Ra>95'),
    (2, 2, 'TL-1000-1100', 16, '60001102', '驱动电源板', '24V/0.8A', '个', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '恒流驱动'),
    (2, 3, 'TL-1000-1100', 17, '60001103', '导光板', 'PC/3mm', '个', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '透光率>92%'),
    (2, 4, 'TL-1000-1100', 18, '60001104', '扩散板', 'PC/2mm', '个', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '均匀光效'),
    (2, 5, 'TL-1000-1100', 19, '60001105', '反光纸', '铝箔反光膜', '张', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '提升光效'),
    (2, 6, 'TL-1000-1100', 20, '60001106', '灯罩', 'PC/磨砂', '个', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '防眩光'),
    (2, 7, 'TL-1000-1100', 21, '60001107', '灯头外壳上盖', 'ABS/白色', '个', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '注塑件'),
    (2, 8, 'TL-1000-1100', 22, '60001108', '灯头外壳下盖', 'ABS/白色', '个', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '注塑件'),
    (2, 9, 'TL-1000-1100', 23, '60001109', '固定螺钉', 'M2*6', '个', 6, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '不锈钢'),
    (2, 10, 'TL-1000-1100', 24, '60001110', '散热片', '铝型材', '个', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '散热用'),

    (3, 1, 'TL-1000', 2, 'TL-1000-1100', '灯头组件（半成品）', '/', '套', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '包含LED光源、驱动电源等'),
    (3, 2, 'TL-1000', 1, 'TL-1000-1200', '灯杆组件（半成品）', '/', '套', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '包含灯杆、转轴等机构'),
    (3, 3, 'TL-1000', 4, 'TL-1000-1300', '底座组件', '/', '套', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '包含底座外壳、配重等'),
    (3, 4, 'TL-1000', 5, 'TL-1000-1400', '控制面板组件', '/', '套', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '触摸按键、指示灯等'),
    (3, 5, 'TL-1000', 25, '60001023', '电源适配器', '24V/1A', '个', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '输入AC100-240V'),
    (3, 6, 'TL-1000', 26, '60001024', '电源线', '2*0.75mm²/1.5m', '根', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '国标两插'),
    (3, 7, 'TL-1000', 27, '60001025', '硅胶防滑垫', 'Φ50*2mm', '个', 4, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '底部防滑'),
    (3, 8, 'TL-1000', 28, '60001026', '产品铭牌', '铝牌/印刷', '个', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '含品牌、参数等信息'),
    (3, 9, 'TL-1000', 29, '60001027', '包装盒', '350*180*120mm', '个', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '彩盒'),
    (3, 10, 'TL-1000', 30, '60001028', '说明书', 'A5/多语言', '份', 1, 0, 0, 'PUSH', 0, 1, 'AI_IMPORT', 'admin', NOW(), '用户使用说明书');

UPDATE material SET default_bom_id = 1 WHERE material_code = 'TL-1000-1200';
UPDATE material SET default_bom_id = 2 WHERE material_code = 'TL-1000-1100';
UPDATE material SET default_bom_id = 3 WHERE material_code = 'TL-1000';

ALTER TABLE material_category AUTO_INCREMENT = 7;
ALTER TABLE material AUTO_INCREMENT = 31;
ALTER TABLE bom_master AUTO_INCREMENT = 4;
ALTER TABLE bom_version AUTO_INCREMENT = 4;
ALTER TABLE bom_item AUTO_INCREMENT = 30;

SET FOREIGN_KEY_CHECKS = 1;
