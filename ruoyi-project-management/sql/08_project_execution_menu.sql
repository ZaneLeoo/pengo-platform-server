set @pm=(select menu_id from sys_menu where menu_name='项目管理' and menu_type='M' order by menu_id desc limit 1);
insert into sys_menu(menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) values
('项目工作台',@pm,1,'workbench','projectManagement/workbench/index',null,'',1,0,'C','0','0','projectManagement:workItem:list','dashboard','admin',sysdate(),'项目执行概览'),
('WBS任务',@pm,4,'task','projectManagement/task/index',null,'',1,0,'C','0','0','projectManagement:workItem:list','tree-table','admin',sysdate(),'任务和里程碑'),
('交付物',@pm,5,'deliverable','projectManagement/deliverable/index',null,'',1,0,'C','0','0','projectManagement:workItem:list','documentation','admin',sysdate(),'项目交付物'),
('问题跟踪',@pm,6,'issue','projectManagement/issue/index',null,'',1,0,'C','0','0','projectManagement:workItem:list','bug','admin',sysdate(),'项目问题闭环');
set @task=(select menu_id from sys_menu where parent_id=@pm and path='task' order by menu_id desc limit 1);
insert into sys_menu(menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark) values
('执行项查询',@task,1,'#','',null,'',1,0,'F','0','0','projectManagement:workItem:query','#','admin',sysdate(),''),
('执行项新增',@task,2,'#','',null,'',1,0,'F','0','0','projectManagement:workItem:add','#','admin',sysdate(),''),
('执行项修改',@task,3,'#','',null,'',1,0,'F','0','0','projectManagement:workItem:edit','#','admin',sysdate(),''),
('执行项删除',@task,4,'#','',null,'',1,0,'F','0','0','projectManagement:workItem:remove','#','admin',sysdate(),'');
