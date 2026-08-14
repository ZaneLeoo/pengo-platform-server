-- 本地联调测试账号，统一初始密码：Test@123456。
-- 仅用于开发环境；进入系统后请在“系统管理 → 用户管理”中修改密码或停用账号。
-- 使用普通管理员角色，不授予超级管理员权限。
set @pm_test_password := '$2a$10$hTTO2fBxyJCYgaO1J0viZusNeIXewFhNN3r2fMTYRd7q3SqZCwvny';

insert into sys_user(dept_id, user_name, nick_name, user_type, email, phonenumber, sex,
                     avatar, password, status, del_flag, create_by, create_time, remark)
select 103, 'pm_lead', '林派生（项目负责人测试）', '00', 'pm_lead@example.com', '', '2',
       '', @pm_test_password, '0', '0', 'admin', sysdate(), '项目管理本地测试账号'
where not exists (select 1 from sys_user where user_name = 'pm_lead');

insert into sys_user(dept_id, user_name, nick_name, user_type, email, phonenumber, sex,
                     avatar, password, status, del_flag, create_by, create_time, remark)
select 103, 'pm_design', '陈思远（设计测试）', '00', 'pm_design@example.com', '', '2',
       '', @pm_test_password, '0', '0', 'admin', sysdate(), '项目管理本地测试账号'
where not exists (select 1 from sys_user where user_name = 'pm_design');

insert into sys_user(dept_id, user_name, nick_name, user_type, email, phonenumber, sex,
                     avatar, password, status, del_flag, create_by, create_time, remark)
select 105, 'pm_test', '周晓岚（测试工程师）', '00', 'pm_test@example.com', '', '2',
       '', @pm_test_password, '0', '0', 'admin', sysdate(), '项目管理本地测试账号'
where not exists (select 1 from sys_user where user_name = 'pm_test');

-- 测试账号使用普通管理员角色，便于联调项目管理菜单；不改变超级管理员权限。
insert into sys_user_role(user_id, role_id)
select u.user_id, 100
from sys_user u
where u.user_name in ('pm_lead', 'pm_design', 'pm_test')
  and not exists (select 1 from sys_user_role ur where ur.user_id = u.user_id and ur.role_id = 100);

-- 普通管理员角色补齐项目管理模块权限，便于用多个账号验证项目成员、任务和交付物操作。
insert into sys_role_menu(role_id, menu_id)
select 100, m.menu_id
from sys_menu m
where m.perms like 'projectManagement:%'
  and not exists (select 1 from sys_role_menu rm where rm.role_id = 100 and rm.menu_id = m.menu_id);

-- 动态路由树需要同时拥有项目管理顶层目录，否则子菜单虽有权限也无法组装出来。
insert into sys_role_menu(role_id, menu_id)
select 100, m.menu_id
from sys_menu m
where m.parent_id = 0 and m.path = 'projectManagement'
  and not exists (select 1 from sys_role_menu rm where rm.role_id = 100 and rm.menu_id = m.menu_id);

-- 将三份已有业务人员档案绑定到对应测试账号；重复执行不会改变其他绑定。
update pm_person p
join sys_user u on u.user_name = case p.person_code
    when '001' then 'pm_lead'
    when '003' then 'pm_design'
    when '004' then 'pm_test'
end
set p.user_id = u.user_id,
    p.update_by = 'admin',
    p.update_time = sysdate()
where p.person_code in ('001', '003', '004')
  and p.user_id is null;
