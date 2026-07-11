-- 采购管理、库存管理菜单及权限
-- 依赖 MES 基础资料菜单（perms = mes:base:list）已存在。
set @mes_base_id := coalesce((select menu_id from sys_menu where perms='mes:base:list' limit 1), 0);
update sys_menu set menu_name='采购管理', remark='采购订单、到货单、入库单' where perms='mes:purchase:list';

insert into sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select '采购管理',@mes_base_id,20,'/purchase','mes/purchase/index',0,0,'C','0','0','mes:purchase:list','shopping-cart','admin',sysdate(),'采购订单、到货单、入库单'
where not exists(select 1 from sys_menu where perms='mes:purchase:list');
set @purchase_id := (select menu_id from sys_menu where perms='mes:purchase:list' limit 1);

insert into sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select '库存管理',@mes_base_id,21,'/inventory','mes/purchase/index',0,0,'C','0','0','mes:inventoryBalance:list','database','admin',sysdate(),'库存余额管理'
where not exists(select 1 from sys_menu where perms='mes:inventoryBalance:list');
set @inventory_id := (select menu_id from sys_menu where perms='mes:inventoryBalance:list' limit 1);

insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '采购订单查询',@purchase_id,1,'F','0','0','mes:purchaseOrder:list','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:purchaseOrder:list');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '采购订单详情',@purchase_id,2,'F','0','0','mes:purchaseOrder:query','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:purchaseOrder:query');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '采购订单新增',@purchase_id,3,'F','0','0','mes:purchaseOrder:add','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:purchaseOrder:add');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '采购订单修改',@purchase_id,4,'F','0','0','mes:purchaseOrder:edit','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:purchaseOrder:edit');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '采购订单删除',@purchase_id,5,'F','0','0','mes:purchaseOrder:remove','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:purchaseOrder:remove');

insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '到货单查询',@purchase_id,6,'F','0','0','mes:purchaseReceipt:list','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:purchaseReceipt:list');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '到货单详情',@purchase_id,7,'F','0','0','mes:purchaseReceipt:query','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:purchaseReceipt:query');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '到货单新增',@purchase_id,8,'F','0','0','mes:purchaseReceipt:add','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:purchaseReceipt:add');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '到货单修改',@purchase_id,9,'F','0','0','mes:purchaseReceipt:edit','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:purchaseReceipt:edit');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '到货单删除',@purchase_id,10,'F','0','0','mes:purchaseReceipt:remove','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:purchaseReceipt:remove');

insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '入库单查询',@purchase_id,11,'F','0','0','mes:purchaseInbound:list','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:purchaseInbound:list');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '入库单详情',@purchase_id,12,'F','0','0','mes:purchaseInbound:query','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:purchaseInbound:query');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '入库单新增',@purchase_id,13,'F','0','0','mes:purchaseInbound:add','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:purchaseInbound:add');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '入库单修改',@purchase_id,14,'F','0','0','mes:purchaseInbound:edit','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:purchaseInbound:edit');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '入库单删除',@purchase_id,15,'F','0','0','mes:purchaseInbound:remove','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:purchaseInbound:remove');

insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '库存查询',@inventory_id,1,'F','0','0','mes:inventoryBalance:list','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:inventoryBalance:list');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '库存详情',@inventory_id,2,'F','0','0','mes:inventoryBalance:query','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:inventoryBalance:query');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '库存新增',@inventory_id,3,'F','0','0','mes:inventoryBalance:add','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:inventoryBalance:add');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '库存修改',@inventory_id,4,'F','0','0','mes:inventoryBalance:edit','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:inventoryBalance:edit');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time) select '库存删除',@inventory_id,5,'F','0','0','mes:inventoryBalance:remove','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:inventoryBalance:remove');

-- 超级管理员默认拥有本次新增菜单权限；其他角色请在角色管理中按需授权。
insert into sys_role_menu(role_id,menu_id)
select 1,m.menu_id from sys_menu m
where (m.perms like 'mes:purchase%' or m.perms like 'mes:inventoryBalance%')
  and not exists(select 1 from sys_role_menu r where r.role_id=1 and r.menu_id=m.menu_id);
