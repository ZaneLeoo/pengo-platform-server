-- 流程管理菜单与按钮权限
set @pm = (select max(menu_id) + 1 from sys_menu);
-- 顶层目录：流程管理
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
                     menu_type, visible, status, perms, icon, create_by, create_time, remark)
values('流程管理', 0, 4, 'flow', null, null, '', 1, 0, 'M', '0', '0', '', 'deployment-unit', 'admin', sysdate(), '流程引擎');

set @flow_menu = last_insert_id();

-- 流程定义
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
                     menu_type, visible, status, perms, icon, create_by, create_time, remark)
values('流程定义', @flow_menu, 1, 'definition', 'flow/definition/index', null, '', 1, 0,
       'C', '0', '0', 'flow:definition:list', 'cluster', 'admin', sysdate(), '审批流程定义与节点配置');
set @def_menu = last_insert_id();

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
                     menu_type, visible, status, perms, icon, create_by, create_time, remark)
values('流程查询', @def_menu, 1, '#', '', null, '', 1, 0, 'F', '0', '0', 'flow:definition:query', '#', 'admin', sysdate(), ''),
       ('流程新增', @def_menu, 2, '#', '', null, '', 1, 0, 'F', '0', '0', 'flow:definition:add', '#', 'admin', sysdate(), ''),
       ('流程修改', @def_menu, 3, '#', '', null, '', 1, 0, 'F', '0', '0', 'flow:definition:edit', '#', 'admin', sysdate(), ''),
       ('流程删除', @def_menu, 4, '#', '', null, '', 1, 0, 'F', '0', '0', 'flow:definition:remove', '#', 'admin', sysdate(), ''),
       ('流程启停', @def_menu, 5, '#', '', null, '', 1, 0, 'F', '0', '0', 'flow:definition:switch', '#', 'admin', sysdate(), '');

-- 审批中心
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
                     menu_type, visible, status, perms, icon, create_by, create_time, remark)
values('审批中心', @flow_menu, 2, 'approval', 'flow/approval/index', null, '', 1, 0,
       'C', '0', '0', 'flow:task:list', 'audit', 'admin', sysdate(), '待办/已办/我发起的审批');
set @task_menu = last_insert_id();

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
                     menu_type, visible, status, perms, icon, create_by, create_time, remark)
values('审批处理', @task_menu, 1, '#', '', null, '', 1, 0, 'F', '0', '0', 'flow:task:handle', '#', 'admin', sysdate(), '');

-- 我的消息
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache,
                     menu_type, visible, status, perms, icon, create_by, create_time, remark)
values('我的消息', @flow_menu, 3, 'message', 'flow/message/index', null, '', 1, 0,
       'C', '0', '0', 'flow:message:list', 'message', 'admin', sysdate(), '流程待办站内消息');
