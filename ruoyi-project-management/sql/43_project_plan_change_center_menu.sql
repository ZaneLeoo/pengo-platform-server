-- 类型：菜单与授权
-- 适用版本：V2.0 项目变更中心
-- 前置条件：42_project_plan_change_permissions.sql
-- 可重复执行：是
-- 数据风险：无（仅新增菜单、调整既有变更按钮归属）

set names utf8mb4;

set @project_root := (
  select menu_id from sys_menu
  where menu_type = 'M' and path = 'projectManagement'
  order by menu_id limit 1
);

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, is_frame,
                     is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
select '项目变更', @project_root, 6, 'planChange', 'projectManagement/planChange/index', '', 1,
       0, 'C', '0', '0', 'projectManagement:planChange:query', 'file-search', 'admin', sysdate()
where @project_root is not null
  and not exists (
    select 1 from sys_menu where parent_id = @project_root and path = 'planChange'
  );

set @change_menu := (
  select menu_id from sys_menu
  where parent_id = @project_root and path = 'planChange'
  order by menu_id limit 1
);

-- 兼容曾以非 UTF-8 客户端执行过本脚本的环境，校正菜单展示名。
update sys_menu set menu_name = '项目变更' where menu_id = @change_menu;

update sys_menu
set parent_id = @change_menu
where perms in ('projectManagement:planChange:query', 'projectManagement:planChange:edit', 'projectManagement:planChange:apply')
  and menu_type = 'F';

insert ignore into sys_role_menu(role_id, menu_id)
select distinct rm.role_id, @change_menu
from sys_role_menu rm
join sys_menu m on m.menu_id = rm.menu_id
where @change_menu is not null and m.perms = 'projectManagement:planChange:query';
