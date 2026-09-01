-- 移除多计量单位、换算公式及其历史快照。
-- 保留 material.unit 和 purchase_order_line.unit/order_quantity，系统继续使用单一计量单位。

DELETE rm
FROM sys_role_menu rm
JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE m.menu_id IN (2110, 2116)
   OR m.parent_id IN (2110, 2116)
   OR m.perms LIKE 'mes:unit:%';

DELETE FROM sys_menu
WHERE menu_id IN (2110, 2116)
   OR parent_id IN (2110, 2116)
   OR perms LIKE 'mes:unit:%';

DROP TABLE IF EXISTS unit_conversion_formula;
DROP TABLE IF EXISTS unit_group_detail;
DROP TABLE IF EXISTS unit_group;

DELIMITER $$
CREATE PROCEDURE drop_multi_unit_column(IN table_name_value VARCHAR(64), IN column_name_value VARCHAR(64))
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = DATABASE()
          AND table_name = table_name_value
          AND column_name = column_name_value
    ) THEN
        SET @drop_column_sql = CONCAT(
            'ALTER TABLE `', table_name_value, '` DROP COLUMN `', column_name_value, '`'
        );
        PREPARE drop_column_statement FROM @drop_column_sql;
        EXECUTE drop_column_statement;
        DEALLOCATE PREPARE drop_column_statement;
    END IF;
END$$
DELIMITER ;

CALL drop_multi_unit_column('material', 'unit_group_code');
CALL drop_multi_unit_column('material', 'length');
CALL drop_multi_unit_column('material', 'width');
CALL drop_multi_unit_column('material', 'height');
CALL drop_multi_unit_column('material', 'weight');
CALL drop_multi_unit_column('material', 'yards');
CALL drop_multi_unit_column('material', 'standard_weight_per_sqm');

CALL drop_multi_unit_column('purchase_order_line', 'input_unit_code');
CALL drop_multi_unit_column('purchase_order_line', 'input_unit_name');
CALL drop_multi_unit_column('purchase_order_line', 'input_qty');
CALL drop_multi_unit_column('purchase_order_line', 'unit1_code');
CALL drop_multi_unit_column('purchase_order_line', 'unit1_name');
CALL drop_multi_unit_column('purchase_order_line', 'unit1_qty');
CALL drop_multi_unit_column('purchase_order_line', 'unit2_code');
CALL drop_multi_unit_column('purchase_order_line', 'unit2_name');
CALL drop_multi_unit_column('purchase_order_line', 'unit2_qty');
CALL drop_multi_unit_column('purchase_order_line', 'unit3_code');
CALL drop_multi_unit_column('purchase_order_line', 'unit3_name');
CALL drop_multi_unit_column('purchase_order_line', 'unit3_qty');

DROP PROCEDURE IF EXISTS drop_multi_unit_column;
