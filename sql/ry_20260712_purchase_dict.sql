-- 采购模块数据字典

-- 字典类型
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('采购单据状态', 'mes_purchase_status', '0', 'admin', NOW(), '采购订单/到货单/入库单通用状态'),
('到货单检验状态', 'mes_receipt_inspection_status', '0', 'admin', NOW(), '到货单质检状态'),
('采购订单单据类型', 'mes_purchase_order_bill_type', '0', 'admin', NOW(), '采购订单单据类型'),
('到货单单据类型', 'mes_purchase_receipt_bill_type', '0', 'admin', NOW(), '到货单单据类型'),
('入库单单据类型', 'mes_purchase_inbound_bill_type', '0', 'admin', NOW(), '入库单单据类型'),
('库存状态', 'mes_inventory_status', '0', 'admin', NOW(), '库存余额状态');

-- 字典数据 - 状态
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, list_class, create_by, create_time) VALUES
('mes_purchase_status', '草稿', 'DRAFT', 1, 'default', 'admin', NOW()),
('mes_purchase_status', '已审核', 'APPROVED', 2, 'success', 'admin', NOW());

-- 字典数据 - 检验状态
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, list_class, create_by, create_time) VALUES
('mes_receipt_inspection_status', '待检', 'PENDING', 1, 'warning', 'admin', NOW()),
('mes_receipt_inspection_status', '合格', 'PASSED', 2, 'success', 'admin', NOW()),
('mes_receipt_inspection_status', '部分合格', 'PARTIAL', 3, 'warning', 'admin', NOW()),
('mes_receipt_inspection_status', '不合格', 'FAILED', 4, 'danger', 'admin', NOW());

-- 字典数据 - 采购订单类型
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, list_class, create_by, create_time) VALUES
('mes_purchase_order_bill_type', '正常采购', 'NORMAL', 1, 'primary', 'admin', NOW());

-- 字典数据 - 到货单类型
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, list_class, create_by, create_time) VALUES
('mes_purchase_receipt_bill_type', '无来源', 'DIRECT', 1, 'default', 'admin', NOW()),
('mes_purchase_receipt_bill_type', '采购订单', 'PURCHASE_ORDER', 2, 'primary', 'admin', NOW());

-- 字典数据 - 入库单类型
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, list_class, create_by, create_time) VALUES
('mes_purchase_inbound_bill_type', '无来源', 'DIRECT', 1, 'default', 'admin', NOW()),
('mes_purchase_inbound_bill_type', '到货单', 'RECEIPT', 2, 'primary', 'admin', NOW());

-- 字典数据 - 库存状态
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, list_class, create_by, create_time) VALUES
('mes_inventory_status', '正常', 'NORMAL', 1, 'success', 'admin', NOW()),
('mes_inventory_status', '冻结', 'FROZEN', 2, 'danger', 'admin', NOW()),
('mes_inventory_status', '锁定', 'LOCKED', 3, 'warning', 'admin', NOW());
