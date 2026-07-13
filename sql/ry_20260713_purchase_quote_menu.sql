-- 供应商报价菜单与权限，依赖采购管理目录已存在。
set @purchase_menu_id := (select menu_id from sys_menu where menu_type='M' and menu_name='采购管理' limit 1);

insert into sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select '供应商报价',@purchase_menu_id,16,'purchaseQuote','mes/purchase/quote',0,0,'C','0','0','mes:purchaseQuote:list','dollar','admin',sysdate(),'供应商报价维护与 AI 选价'
where @purchase_menu_id is not null
  and not exists(select 1 from sys_menu where perms='mes:purchaseQuote:list');

set @quote_menu_id := (select menu_id from sys_menu where menu_type='C' and perms='mes:purchaseQuote:list' limit 1);

insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select '供应商报价查询',@quote_menu_id,1,'F','0','0','mes:purchaseQuote:query','#','admin',sysdate(),'查看报价详情'
where @quote_menu_id is not null and not exists(select 1 from sys_menu where perms='mes:purchaseQuote:query');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select '供应商报价新增',@quote_menu_id,2,'F','0','0','mes:purchaseQuote:add','#','admin',sysdate(),'新增报价'
where @quote_menu_id is not null and not exists(select 1 from sys_menu where perms='mes:purchaseQuote:add');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select '供应商报价修改',@quote_menu_id,3,'F','0','0','mes:purchaseQuote:edit','#','admin',sysdate(),'修改草稿报价'
where @quote_menu_id is not null and not exists(select 1 from sys_menu where perms='mes:purchaseQuote:edit');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select '供应商报价删除',@quote_menu_id,4,'F','0','0','mes:purchaseQuote:remove','#','admin',sysdate(),'删除草稿报价'
where @quote_menu_id is not null and not exists(select 1 from sys_menu where perms='mes:purchaseQuote:remove');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select '供应商报价审核',@quote_menu_id,5,'F','0','0','mes:purchaseQuote:approve','#','admin',sysdate(),'审核报价'
where @quote_menu_id is not null and not exists(select 1 from sys_menu where perms='mes:purchaseQuote:approve');
insert into sys_menu(menu_name,parent_id,order_num,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select '供应商报价弃审',@quote_menu_id,6,'F','0','0','mes:purchaseQuote:unapprove','#','admin',sysdate(),'弃审报价'
where @quote_menu_id is not null and not exists(select 1 from sys_menu where perms='mes:purchaseQuote:unapprove');

insert into sys_role_menu(role_id,menu_id)
select 1,m.menu_id from sys_menu m
where m.perms like 'mes:purchaseQuote:%'
  and not exists(select 1 from sys_role_menu r where r.role_id=1 and r.menu_id=m.menu_id);
