-- ============================================
-- 菜单重组：MES → 制造运营
-- 日期：2026-07-12
-- ============================================

-- 1. 重命名
UPDATE sys_menu SET menu_name = '制造运营' WHERE menu_id = 2003;
UPDATE sys_menu SET menu_name = '基础数据' WHERE menu_id = 2004;

-- 2. 创建采购管理目录
INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, icon, create_by, create_time)
VALUES ('采购管理', 2003, 2, 'M', '0', '0', '#', 'admin', NOW());

-- 3. 创建库存管理目录
INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, icon, create_by, create_time)
VALUES ('库存管理', 2003, 3, 'M', '0', '0', '#', 'admin', NOW());

-- 4. 采购订单 → 采购管理
UPDATE sys_menu SET parent_id = 2083, order_num = 1 WHERE menu_id = 2046;

-- 5. 到货单 → 采购管理
UPDATE sys_menu SET parent_id = 2083, order_num = 2 WHERE menu_id = 2067;

-- 6. 库存余额 → 库存管理
UPDATE sys_menu SET parent_id = 2084, order_num = 1 WHERE menu_id = 2047;

-- 7. 入库单 → 库存管理
UPDATE sys_menu SET parent_id = 2084, order_num = 2 WHERE menu_id = 2068;

-- 注: 子权限按钮 (F类型) 随父节点自动迁移，无需单独处理
