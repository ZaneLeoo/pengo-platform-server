-- 类型：权限
-- 适用版本：V2.0 基线与变更专题
-- 前置条件：41_project_plan_change.sql、项目管理菜单已存在
-- 可重复执行：是
-- 数据风险：低（仅新增菜单按钮和角色授权）

set @project_menu = (
  select menu_id from sys_menu where perms = 'projectManagement:project:list' limit 1
);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type,
                     visible, status, perms, icon, create_by, create_time, remark)
select action_name, @project_menu, sort_no, '', '', null, '', 1, 0, 'F', '0', '0', permission, '#', 'admin', sysdate(),
       '项目计划基线与变更按钮权限'
from (
  select '变更查询' action_name, 61 sort_no, 'projectManagement:planChange:query' permission union all
  select '变更维护', 62, 'projectManagement:planChange:edit' union all
  select '确认应用变更', 63, 'projectManagement:planChange:apply'
) actions
where @project_menu is not null
  and not exists(select 1 from sys_menu existing where existing.perms = actions.permission);

-- 与原项目查询/修改权限保持同一角色授权范围；不赋予管理员业务越权能力。
insert ignore into sys_role_menu(role_id, menu_id)
select grant_role.role_id, change_menu.menu_id
from sys_role_menu grant_role
join sys_menu origin_menu on origin_menu.menu_id = grant_role.menu_id
join sys_menu change_menu on change_menu.perms = 'projectManagement:planChange:query'
where origin_menu.perms = 'projectManagement:project:query';

insert ignore into sys_role_menu(role_id, menu_id)
select grant_role.role_id, change_menu.menu_id
from sys_role_menu grant_role
join sys_menu origin_menu on origin_menu.menu_id = grant_role.menu_id
join sys_menu change_menu on change_menu.perms in ('projectManagement:planChange:edit', 'projectManagement:planChange:apply')
where origin_menu.perms = 'projectManagement:project:edit';
