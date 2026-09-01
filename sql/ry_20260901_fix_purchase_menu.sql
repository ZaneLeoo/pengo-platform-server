-- 修复采购菜单的历史父子关系和类型：采购管理下使用独立订单、到货、入库页面。
SET @mes_id := (SELECT menu_id FROM sys_menu WHERE parent_id = 0 AND path = 'mes' LIMIT 1);
SET @purchase_root := (SELECT menu_id FROM sys_menu WHERE parent_id = @mes_id AND path = 'purchaseManagement' AND menu_type = 'M' LIMIT 1);

UPDATE sys_menu
SET parent_id = @purchase_root, menu_type = 'C', menu_name = '采购订单', path = 'purchaseOrder',
    component = 'mes/purchase/order', visible = '0', status = '0', update_by = 'admin', update_time = NOW()
WHERE perms = 'mes:purchaseOrder:list';
SET @order_menu := (SELECT menu_id FROM sys_menu WHERE perms = 'mes:purchaseOrder:list' LIMIT 1);

UPDATE sys_menu
SET parent_id = @order_menu, update_by = 'admin', update_time = NOW()
WHERE perms IN ('mes:purchaseOrder:query', 'mes:purchaseOrder:add', 'mes:purchaseOrder:edit',
                'mes:purchaseOrder:remove', 'mes:purchaseOrder:approve', 'mes:purchaseOrder:unapprove');

INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,query,route_name,menu_type,visible,status,perms,icon,create_by,create_time,remark)
SELECT '采购到货', @purchase_root, 2, 'purchaseReceipt', 'mes/purchase/receipt', '', 'PurchaseReceipt', 'C', '0', '0', 'mes:purchaseReceipt:menu', 'truck', 'admin', NOW(), '采购订单参照到货、质检'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mes:purchaseReceipt:menu');
SET @receipt_menu := (SELECT menu_id FROM sys_menu WHERE perms = 'mes:purchaseReceipt:menu' LIMIT 1);
UPDATE sys_menu SET parent_id = @receipt_menu, update_by = 'admin', update_time = NOW()
WHERE perms IN ('mes:purchaseReceipt:list', 'mes:purchaseReceipt:query', 'mes:purchaseReceipt:add',
                'mes:purchaseReceipt:edit', 'mes:purchaseReceipt:remove', 'mes:purchaseReceipt:approve',
                'mes:purchaseReceipt:unapprove', 'mes:purchaseReceipt:inspect', 'mes:purchaseReceipt:uninspect',
                'mes:purchaseReceipt:reference');

INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,query,route_name,menu_type,visible,status,perms,icon,create_by,create_time,remark)
SELECT '采购入库', @purchase_root, 3, 'purchaseInbound', 'mes/purchase/inbound', '', 'PurchaseInbound', 'C', '0', '0', 'mes:purchaseInbound:menu', 'database', 'admin', NOW(), '采购到货参照入库'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'mes:purchaseInbound:menu');
SET @inbound_menu := (SELECT menu_id FROM sys_menu WHERE perms = 'mes:purchaseInbound:menu' LIMIT 1);
UPDATE sys_menu SET parent_id = @inbound_menu, update_by = 'admin', update_time = NOW()
WHERE perms IN ('mes:purchaseInbound:list', 'mes:purchaseInbound:query', 'mes:purchaseInbound:add',
                'mes:purchaseInbound:edit', 'mes:purchaseInbound:remove', 'mes:purchaseInbound:approve',
                'mes:purchaseInbound:unapprove', 'mes:purchaseInbound:reference');

-- 停用旧的聚合菜单，避免继续进入 mes/purchase/index。
UPDATE sys_menu SET visible = '1', status = '1', update_by = 'admin', update_time = NOW()
WHERE perms = 'mes:purchase:list' AND component = 'mes/purchase/index';

-- 将原聚合菜单已授予的角色同步授权给新的可见菜单。
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT rm.role_id, menu.menu_id
FROM sys_role_menu rm
JOIN sys_menu old_menu ON old_menu.menu_id = rm.menu_id AND old_menu.perms = 'mes:purchase:list'
JOIN sys_menu menu ON menu.perms IN ('mes:purchaseOrder:list', 'mes:purchaseReceipt:menu', 'mes:purchaseInbound:menu');
