-- 修复因历史采购菜单迁移未找到父节点而产生的空 parent_id。
-- 该异常会导致 /getRouters 构建菜单树时抛出空指针。
UPDATE sys_menu
SET parent_id = 2003,
    update_by = 'admin',
    update_time = NOW(),
    remark = CONCAT(IFNULL(remark, ''), '；恢复后修复空父菜单')
WHERE menu_id = 2039
  AND parent_id IS NULL;

-- 修复历史脚本硬编码菜单 ID 在重建后错位造成的空路由目录，
-- 否则 Vue Router 会生成「有空 path 的无名子路由」。
UPDATE sys_menu
SET path = 'purchaseManagement',
    route_name = 'PurchaseManagement',
    update_by = 'admin',
    update_time = NOW()
WHERE menu_id = 2079
  AND menu_type = 'M';

UPDATE sys_menu
SET path = 'inventoryManagement',
    route_name = 'InventoryManagement',
    update_by = 'admin',
    update_time = NOW()
WHERE menu_id = 2080
  AND menu_type = 'M';

UPDATE sys_menu
SET parent_id = 2079,
    update_by = 'admin',
    update_time = NOW()
WHERE menu_id = 2039;

UPDATE sys_menu
SET parent_id = 2080,
    update_by = 'admin',
    update_time = NOW()
WHERE menu_id = 2044;

-- 数据库重建后，项目管理根菜单被保留为隐藏、停用状态，且管理员角色未授权。
-- 恢复项目管理菜单的显示及管理员（role_id=1）对整棵菜单树的访问权限。
UPDATE sys_menu
SET visible = '0',
    status = '0',
    update_by = 'admin',
    update_time = NOW()
WHERE menu_id = 2121;

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id
FROM sys_menu
WHERE menu_id = 2121
   OR parent_id = 2121
   OR parent_id IN (SELECT menu_id FROM sys_menu WHERE parent_id = 2121);

-- 恢复 BOM 管理下被重建脚本错误隐藏、停用的「BOM 主数据」菜单。
UPDATE sys_menu
SET visible = '0',
    status = '0',
    update_by = 'admin',
    update_time = NOW()
WHERE menu_id = 2017
  AND parent_id = 2088;

-- 历史基础档案脚本创建了 base_* 表，但当前 Mapper 使用 supplier/warehouse/location。
-- 数据恢复后按既有表结构补齐运行所需表；保留为空表，后续可通过页面维护数据。
CREATE TABLE IF NOT EXISTS supplier LIKE base_supplier;
CREATE TABLE IF NOT EXISTS warehouse LIKE base_warehouse;
CREATE TABLE IF NOT EXISTS location LIKE base_location;

-- 补齐多计量单位功能在 material 表中使用的字段（兼容不支持 ADD COLUMN IF NOT EXISTS 的 MySQL）。
SET @unit_group_code_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'material'
      AND column_name = 'unit_group_code'
);
SET @unit_group_code_sql = IF(
    @unit_group_code_exists = 0,
    'ALTER TABLE material ADD COLUMN unit_group_code VARCHAR(64) NULL COMMENT ''计量单位组编码'' AFTER unit',
    'SELECT 1'
);
PREPARE unit_group_code_statement FROM @unit_group_code_sql;
EXECUTE unit_group_code_statement;
DEALLOCATE PREPARE unit_group_code_statement;
