set @project_management_menu_id=(select menu_id from sys_menu where menu_name='项目管理' and menu_type='M' order by menu_id desc limit 1);
insert into sys_menu(menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
values('项目分类',@project_management_menu_id,2,'category','projectManagement/category/index',null,'',1,0,'C','0','0','projectManagement:category:list','tree-table','admin',sysdate(),'项目分类管理');
set @category_menu_id=last_insert_id();
insert into sys_menu(menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) values
('分类查询',@category_menu_id,1,'#','',null,'',1,0,'F','0','0','projectManagement:category:query','#','admin',sysdate(),''),
('分类新增',@category_menu_id,2,'#','',null,'',1,0,'F','0','0','projectManagement:category:add','#','admin',sysdate(),''),
('分类修改',@category_menu_id,3,'#','',null,'',1,0,'F','0','0','projectManagement:category:edit','#','admin',sysdate(),''),
('分类删除',@category_menu_id,4,'#','',null,'',1,0,'F','0','0','projectManagement:category:remove','#','admin',sysdate(),'');
