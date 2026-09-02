-- 类型：初始化数据 / 项目工时业务角色与测试账号
-- 适用版本：V2 预算与成本阶段 5（项目工时）
-- 可重复执行：是
--
-- 说明：密码不在脚本内明文或固定散列，执行时复用现有 csy 测试账号的
-- BCrypt 密码（123456）。生产环境应通过系统“重置密码”维护账号密码。

set names utf8mb4;

-- 角色：项目经理、成本管理员、工时审批人。
insert into sys_role(role_name, role_key, role_sort, data_scope, menu_check_strictly,
                     dept_check_strictly, status, del_flag, create_by, create_time, remark)
select convert(0xE9A1B9E79BAEE7BB8FE79086 using utf8mb4), 'project_manager', 30, '1', 1, 1,
       '0', '0', 'admin', now(), convert(0xE9A1B9E79BAEE7AEA1E79086E9A1B9E79BAEE4B88EE5B7A5E697B6E6B187E6B1BEE8A7BEE69D83 using utf8mb4)
where not exists (select 1 from sys_role where role_key = 'project_manager' and del_flag = '0');

insert into sys_role(role_name, role_key, role_sort, data_scope, menu_check_strictly,
                     dept_check_strictly, status, del_flag, create_by, create_time, remark)
select convert(0xE68890E69CACE7AEA1E79086E59198 using utf8mb4), 'cost_manager', 40, '1', 1, 1,
       '0', '0', 'admin', now(), convert(0xE7B4A7E6B8B8E4BABAE5B7A5E697B6E68890E69CACE4B88EE4BABAE5B7A5E58D95E4BBB7E7BBAFE68AA4E7AEA1E79086 using utf8mb4)
where not exists (select 1 from sys_role where role_key = 'cost_manager' and del_flag = '0');

insert into sys_role(role_name, role_key, role_sort, data_scope, menu_check_strictly,
                     dept_check_strictly, status, del_flag, create_by, create_time, remark)
select convert(0xE5B7A5E697B6E5AEA1E689B9E4BABA using utf8mb4), 'work_hours_approver', 50, '1', 1, 1,
       '0', '0', 'admin', now(), convert(0xE58FAFE4BDA0E4BD9CE4B8BAE591A8E5B7A5E697B6E5AEA1E689B9E6B58EE7A88BE79A84E7B3BBE7BB9FE8A792E889B2 using utf8mb4)
where not exists (select 1 from sys_role where role_key = 'work_hours_approver' and del_flag = '0');

-- 测试账号：均复用 csy 的 BCrypt 密码（当前为 123456）。
set @default_dept := (select dept_id from sys_dept where status = '0' and del_flag = '0' order by dept_id limit 1);
insert into sys_user(dept_id,user_name,nick_name,user_type,email,phonenumber,sex,avatar,password,status,del_flag,create_by,create_time,remark)
select @default_dept, 'pm_manager', convert(0xE9A1B9E79BAEE7BB8FE79086 using utf8mb4), '00', '', '', '0', '', source.password, '0', '0', 'admin', now(), convert(0xE9A1B9E79BAEE7BAA7E6B58BE8AF95E8B4A6E58FB7 using utf8mb4)
from sys_user source where source.user_name = 'csy'
  and not exists (select 1 from sys_user where user_name = 'pm_manager');
insert into sys_user(dept_id,user_name,nick_name,user_type,email,phonenumber,sex,avatar,password,status,del_flag,create_by,create_time,remark)
select @default_dept, 'cost_manager', convert(0xE68890E69CACE7AEA1E79086E59198 using utf8mb4), '00', '', '', '0', '', source.password, '0', '0', 'admin', now(), convert(0xE9A1B9E79BAEE7BAA7E6B58BE8AF95E8B4A6E58FB7 using utf8mb4)
from sys_user source where source.user_name = 'csy'
  and not exists (select 1 from sys_user where user_name = 'cost_manager');
insert into sys_user(dept_id,user_name,nick_name,user_type,email,phonenumber,sex,avatar,password,status,del_flag,create_by,create_time,remark)
select @default_dept, 'hours_approver', convert(0xE5B7A5E697B6E5AEA1E689B9E4BABA using utf8mb4), '00', '', '', '0', '', source.password, '0', '0', 'admin', now(), convert(0xE9A1B9E79BAEE7BAA7E6B58BE8AF95E8B4A6E58FB7 using utf8mb4)
from sys_user source where source.user_name = 'csy'
  and not exists (select 1 from sys_user where user_name = 'hours_approver');

-- 账号与角色绑定。
insert ignore into sys_user_role(user_id, role_id)
select u.user_id, r.role_id from sys_user u join sys_role r
where u.user_name = 'pm_manager' and r.role_key = 'project_manager';
insert ignore into sys_user_role(user_id, role_id)
select u.user_id, r.role_id from sys_user u join sys_role r
where u.user_name = 'cost_manager' and r.role_key = 'cost_manager';
insert ignore into sys_user_role(user_id, role_id)
select u.user_id, r.role_id from sys_user u join sys_role r
where u.user_name = 'hours_approver' and r.role_key = 'work_hours_approver';

-- 菜单及按钮权限：系统管理员仅保留系统权限；这些角色是实际业务授权载体。
set @pm_root := (select menu_id from sys_menu where menu_type='M' and path='projectManagement' order by menu_id limit 1);
set @project_manager_role := (select role_id from sys_role where role_key='project_manager' and del_flag='0' limit 1);
set @cost_manager_role := (select role_id from sys_role where role_key='cost_manager' and del_flag='0' limit 1);
set @hours_approver_role := (select role_id from sys_role where role_key='work_hours_approver' and del_flag='0' limit 1);

-- 项目经理：项目工作区、变更、任务/交付物/问题与工时台账；具体项目范围仍由服务层判定。
insert ignore into sys_role_menu(role_id,menu_id)
select @project_manager_role, menu_id from sys_menu
where menu_id=@pm_root or perms in ('projectManagement:project:list','projectManagement:workItem:list',
  'projectManagement:issue:list','projectManagement:planChange:query','projectManagement:workHours:query',
  'projectManagement:workHours:edit','projectManagement:workHours:manage');

-- 成本管理员：成本类别、工时台账和人工单价维护。
insert ignore into sys_role_menu(role_id,menu_id)
select @cost_manager_role, menu_id from sys_menu
where menu_id=@pm_root or perms in ('projectManagement:project:list','projectManagement:costCategory:list',
  'projectManagement:workHours:manage','projectManagement:laborRate:query','projectManagement:laborRate:edit');

-- 工时审批人：个人审批页由个人中心承载；保留工时查询以便查看原业务。
insert ignore into sys_role_menu(role_id,menu_id)
select @hours_approver_role, menu_id from sys_menu
where menu_id=@pm_root or perms in ('projectManagement:workHours:query','projectManagement:workHours:manage');
