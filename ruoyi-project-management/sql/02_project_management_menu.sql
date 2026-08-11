-- 项目管理菜单。若已存在同名菜单，请在执行前按部署环境调整 parent_id。
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values('项目管理', 0, 6, 'projectManagement', null, null, '', 1, 0, 'M', '0', '0', '', 'project-management', 'admin', sysdate(), '项目管理目录');
set @project_management_menu_id = last_insert_id();

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
values('人员档案', @project_management_menu_id, 1, 'person', 'projectManagement/person/index', null, '', 1, 0, 'C', '0', '0', 'projectManagement:person:list', 'user', 'admin', sysdate(), '项目人员档案');
set @project_person_menu_id = last_insert_id();

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) values
('人员查询', @project_person_menu_id, 1, '#', '', null, '', 1, 0, 'F', '0', '0', 'projectManagement:person:query', '#', 'admin', sysdate(), ''),
('人员新增', @project_person_menu_id, 2, '#', '', null, '', 1, 0, 'F', '0', '0', 'projectManagement:person:add', '#', 'admin', sysdate(), ''),
('人员修改', @project_person_menu_id, 3, '#', '', null, '', 1, 0, 'F', '0', '0', 'projectManagement:person:edit', '#', 'admin', sysdate(), ''),
('人员删除', @project_person_menu_id, 4, '#', '', null, '', 1, 0, 'F', '0', '0', 'projectManagement:person:remove', '#', 'admin', sysdate(), ''),
('人员导出', @project_person_menu_id, 5, '#', '', null, '', 1, 0, 'F', '0', '0', 'projectManagement:person:export', '#', 'admin', sysdate(), '');

