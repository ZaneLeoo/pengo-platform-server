-- 采购、到货、入库、库存菜单；执行前请先执行采购库存建表脚本
set @mes_base_id := (select menu_id from sys_menu where perms='mes:base:list' limit 1);
insert into sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select '采购与库存',@mes_base_id,20,'purchase','mes/purchase/index',0,0,'C','0','0','mes:purchase:list','shopping-cart','admin',sysdate(),'采购到货入库及库存余额';
set @purchase_menu_id := (select menu_id from sys_menu where perms='mes:purchase:list' limit 1);
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time)
select '采购与库存查询',@purchase_menu_id,1,'F','0','0','mes:purchase:query','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:purchase:query');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time)
select '采购与库存新增',@purchase_menu_id,2,'F','0','0','mes:purchase:add','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:purchase:add');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time)
select '采购与库存修改',@purchase_menu_id,3,'F','0','0','mes:purchase:edit','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:purchase:edit');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time)
select '采购与库存删除',@purchase_menu_id,4,'F','0','0','mes:purchase:remove','#','admin',sysdate() where not exists(select 1 from sys_menu where perms='mes:purchase:remove');
