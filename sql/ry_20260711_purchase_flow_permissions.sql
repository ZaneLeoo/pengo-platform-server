-- 单据流转权限；默认授予超级管理员。
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time)
select '采购订单审核',menu_id,10,'F','0','0','mes:purchaseOrder:approve','#','admin',sysdate() from sys_menu where perms='mes:purchaseOrder:list' and menu_type='C' and not exists(select 1 from sys_menu where perms='mes:purchaseOrder:approve');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time)
select '采购订单弃审',menu_id,11,'F','0','0','mes:purchaseOrder:unapprove','#','admin',sysdate() from sys_menu where perms='mes:purchaseOrder:list' and menu_type='C' and not exists(select 1 from sys_menu where perms='mes:purchaseOrder:unapprove');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time)
select '送货单审核',menu_id,10,'F','0','0','mes:purchaseReceipt:approve','#','admin',sysdate() from sys_menu where perms='mes:purchaseReceipt:menu' and not exists(select 1 from sys_menu where perms='mes:purchaseReceipt:approve');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time)
select '送货单弃审',menu_id,11,'F','0','0','mes:purchaseReceipt:unapprove','#','admin',sysdate() from sys_menu where perms='mes:purchaseReceipt:menu' and not exists(select 1 from sys_menu where perms='mes:purchaseReceipt:unapprove');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time)
select '送货单质检',menu_id,12,'F','0','0','mes:purchaseReceipt:inspect','#','admin',sysdate() from sys_menu where perms='mes:purchaseReceipt:menu' and not exists(select 1 from sys_menu where perms='mes:purchaseReceipt:inspect');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time)
select '送货单参照',menu_id,13,'F','0','0','mes:purchaseReceipt:reference','#','admin',sysdate() from sys_menu where perms='mes:purchaseReceipt:menu' and not exists(select 1 from sys_menu where perms='mes:purchaseReceipt:reference');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time)
select '入库单审核',menu_id,10,'F','0','0','mes:purchaseInbound:approve','#','admin',sysdate() from sys_menu where perms='mes:purchaseInbound:menu' and not exists(select 1 from sys_menu where perms='mes:purchaseInbound:approve');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time)
select '入库单弃审',menu_id,11,'F','0','0','mes:purchaseInbound:unapprove','#','admin',sysdate() from sys_menu where perms='mes:purchaseInbound:menu' and not exists(select 1 from sys_menu where perms='mes:purchaseInbound:unapprove');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time)
select '入库单参照',menu_id,12,'F','0','0','mes:purchaseInbound:reference','#','admin',sysdate() from sys_menu where perms='mes:purchaseInbound:menu' and not exists(select 1 from sys_menu where perms='mes:purchaseInbound:reference');

insert into sys_role_menu(role_id,menu_id)
select 1,m.menu_id from sys_menu m where m.perms in ('mes:purchaseOrder:approve','mes:purchaseOrder:unapprove','mes:purchaseReceipt:approve','mes:purchaseReceipt:unapprove','mes:purchaseReceipt:inspect','mes:purchaseReceipt:reference','mes:purchaseInbound:approve','mes:purchaseInbound:unapprove','mes:purchaseInbound:reference')
and not exists(select 1 from sys_role_menu r where r.role_id=1 and r.menu_id=m.menu_id);
