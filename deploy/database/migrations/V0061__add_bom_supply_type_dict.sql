-- 确保通过 mysql 客户端执行时，中文字典内容按 UTF-8 发送。
SET NAMES utf8mb4;

-- 当前业务支持的 BOM 子件发料/扣料方式。
INSERT INTO sys_dict_type(dict_name, dict_type, status, create_by, create_time, remark)
SELECT 'BOM发料方式', 'mes_bom_supply_type', '0', 'admin', NOW(), '当前支持直接领料、倒冲领料'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_type WHERE dict_type = 'mes_bom_supply_type'
);

INSERT INTO sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
SELECT 1, '直接领料', 'PUSH', 'mes_bom_supply_type', 'primary', '0', 'admin', NOW(), 'SupplyType.PUSH'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_data WHERE dict_type = 'mes_bom_supply_type' AND dict_value = 'PUSH'
);
INSERT INTO sys_dict_data(dict_sort, dict_label, dict_value, dict_type, list_class, status, create_by, create_time, remark)
SELECT 2, '倒冲领料', 'STOCK_BACKFLUSH', 'mes_bom_supply_type', 'warning', '0', 'admin', NOW(), 'SupplyType.STOCK_BACKFLUSH'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_dict_data WHERE dict_type = 'mes_bom_supply_type' AND dict_value = 'STOCK_BACKFLUSH'
);
