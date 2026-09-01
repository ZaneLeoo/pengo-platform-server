-- 数据库恢复后的菜单树整理（可重复执行，不删除菜单或既有角色授权）。
-- 请以 UTF-8 客户端执行；Windows 下不要用 Get-Content 管道转发到 mysql，以免中文菜单名称被控制台编码损坏。
-- 覆盖制造运营的基础数据、采购管理、库存管理，避免历史脚本硬编码 menu_id 导致的错挂。

SET @mes_id := (SELECT menu_id FROM sys_menu WHERE parent_id = 0 AND path = 'mes' AND menu_type = 'M' LIMIT 1);
UPDATE sys_menu SET menu_name='制造运营', visible='0', status='0', update_by='admin', update_time=NOW() WHERE menu_id=@mes_id;

SET @base_id := (SELECT menu_id FROM sys_menu WHERE parent_id=@mes_id AND path='base' AND menu_type='M' LIMIT 1);
SET @purchase_id := (SELECT menu_id FROM sys_menu WHERE parent_id=@mes_id AND path='purchaseManagement' AND menu_type='M' LIMIT 1);
SET @inventory_id := (SELECT menu_id FROM sys_menu WHERE parent_id=@mes_id AND path='inventoryManagement' AND menu_type='M' LIMIT 1);
UPDATE sys_menu SET menu_name='基础数据', visible='0', status='0', update_by='admin', update_time=NOW() WHERE menu_id=@base_id;
UPDATE sys_menu SET menu_name='采购管理', visible='0', status='0', update_by='admin', update_time=NOW() WHERE menu_id=@purchase_id;
UPDATE sys_menu SET menu_name='库存管理', visible='0', status='0', update_by='admin', update_time=NOW() WHERE menu_id=@inventory_id;

-- 基础数据业务菜单与按钮全部回到自己的页面下。
UPDATE sys_menu SET parent_id=@base_id, menu_type='C', component='mes/base/supplier/index', path='supplier', visible='0', status='0', update_by='admin', update_time=NOW() WHERE perms='base:supplier:list';
SET @supplier_id := (SELECT menu_id FROM sys_menu WHERE perms='base:supplier:list' LIMIT 1);
UPDATE sys_menu SET parent_id=@supplier_id, update_by='admin', update_time=NOW() WHERE perms IN ('base:supplier:query','base:supplier:add','base:supplier:edit','base:supplier:remove');

UPDATE sys_menu SET parent_id=@base_id, menu_type='C', component='mes/base/warehouse/index', path='warehouse', visible='0', status='0', update_by='admin', update_time=NOW() WHERE perms='base:warehouse:list';
SET @warehouse_id := (SELECT menu_id FROM sys_menu WHERE perms='base:warehouse:list' LIMIT 1);
UPDATE sys_menu SET parent_id=@warehouse_id, update_by='admin', update_time=NOW() WHERE perms IN ('base:warehouse:query','base:warehouse:add','base:warehouse:edit','base:warehouse:remove');

UPDATE sys_menu SET parent_id=@base_id, menu_type='C', component='mes/base/location/index', path='location', visible='0', status='0', update_by='admin', update_time=NOW() WHERE perms='base:location:list';
SET @location_id := (SELECT menu_id FROM sys_menu WHERE perms='base:location:list' LIMIT 1);
UPDATE sys_menu SET parent_id=@location_id, update_by='admin', update_time=NOW() WHERE perms IN ('base:location:query','base:location:add','base:location:edit','base:location:remove');

-- 采购链路独立页面：订单 → 到货 → 入库；不再使用已废弃的聚合页面。
UPDATE sys_menu SET parent_id=@purchase_id, menu_name='采购订单', menu_type='C', path='purchaseOrder', component='mes/purchase/order', visible='0', status='0', update_by='admin', update_time=NOW() WHERE perms='mes:purchaseOrder:list';
SET @order_id := (SELECT menu_id FROM sys_menu WHERE perms='mes:purchaseOrder:list' LIMIT 1);
UPDATE sys_menu SET parent_id=@order_id, update_by='admin', update_time=NOW() WHERE perms IN ('mes:purchaseOrder:query','mes:purchaseOrder:add','mes:purchaseOrder:edit','mes:purchaseOrder:remove','mes:purchaseOrder:approve','mes:purchaseOrder:unapprove');

SET @receipt_id := (SELECT menu_id FROM sys_menu WHERE perms='mes:purchaseReceipt:menu' LIMIT 1);
UPDATE sys_menu SET parent_id=@purchase_id, menu_name='采购到货', menu_type='C', path='purchaseReceipt', component='mes/purchase/receipt', visible='0', status='0', update_by='admin', update_time=NOW() WHERE menu_id=@receipt_id;
UPDATE sys_menu SET parent_id=@receipt_id, update_by='admin', update_time=NOW() WHERE perms IN ('mes:purchaseReceipt:list','mes:purchaseReceipt:query','mes:purchaseReceipt:add','mes:purchaseReceipt:edit','mes:purchaseReceipt:remove','mes:purchaseReceipt:approve','mes:purchaseReceipt:unapprove','mes:purchaseReceipt:inspect','mes:purchaseReceipt:uninspect','mes:purchaseReceipt:reference');

SET @inbound_id := (SELECT menu_id FROM sys_menu WHERE perms='mes:purchaseInbound:menu' LIMIT 1);
UPDATE sys_menu SET parent_id=@purchase_id, menu_name='采购入库', menu_type='C', path='purchaseInbound', component='mes/purchase/inbound', visible='0', status='0', update_by='admin', update_time=NOW() WHERE menu_id=@inbound_id;
UPDATE sys_menu SET parent_id=@inbound_id, update_by='admin', update_time=NOW() WHERE perms IN ('mes:purchaseInbound:list','mes:purchaseInbound:query','mes:purchaseInbound:add','mes:purchaseInbound:edit','mes:purchaseInbound:remove','mes:purchaseInbound:approve','mes:purchaseInbound:unapprove','mes:purchaseInbound:reference');

UPDATE sys_menu SET parent_id=@purchase_id, menu_type='C', path='purchaseQuote', component='mes/purchase/quote', visible='0', status='0', update_by='admin', update_time=NOW() WHERE perms='mes:purchaseQuote:list';
SET @quote_id := (SELECT menu_id FROM sys_menu WHERE perms='mes:purchaseQuote:list' LIMIT 1);
UPDATE sys_menu SET parent_id=@quote_id, update_by='admin', update_time=NOW() WHERE perms IN ('mes:purchaseQuote:query','mes:purchaseQuote:add','mes:purchaseQuote:edit','mes:purchaseQuote:remove','mes:purchaseQuote:approve','mes:purchaseQuote:unapprove');

UPDATE sys_menu SET parent_id=@inventory_id, menu_type='C', path='inventoryBalance', component='mes/purchase/inventory', visible='0', status='0', update_by='admin', update_time=NOW() WHERE perms='mes:inventoryBalance:list';
SET @balance_id := (SELECT menu_id FROM sys_menu WHERE perms='mes:inventoryBalance:list' LIMIT 1);
UPDATE sys_menu SET parent_id=@balance_id, update_by='admin', update_time=NOW() WHERE perms IN ('mes:inventoryBalance:query','mes:inventoryBalance:add','mes:inventoryBalance:edit','mes:inventoryBalance:remove');

-- 已废弃聚合菜单及其旧权限保留审计，但不再显示或路由。
UPDATE sys_menu SET visible='1', status='1', update_by='admin', update_time=NOW() WHERE perms IN ('mes:purchase:list','mes:purchase:query','mes:purchase:add','mes:purchase:edit','mes:purchase:remove');

-- 管理员保有完整可见菜单访问；其他角色原授权不覆盖。
INSERT IGNORE INTO sys_role_menu(role_id,menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id IN (@mes_id,@base_id,@purchase_id,@inventory_id,@supplier_id,@warehouse_id,@location_id,@order_id,@receipt_id,@inbound_id,@quote_id,@balance_id);
