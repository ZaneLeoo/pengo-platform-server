-- 智能装配视觉检测工作站研发项目（project_id = 3）团队账号。
-- 初始密码：123456，仅用于本地测试；请登录后在系统用户管理中修改。
-- 账号采用人员姓名拼音首字母：lps、lbh、lh、zxl、sn。
set @pm_team_password := '$2a$10$Cuc0psSVHpYe790n/y/S5.cm299MRw9iLx96AA8DNsUXZIYmqwxCO';

-- 复用前一轮已经创建的测试账号，改成正式的拼音缩写；目标账号不存在时才改名，避免覆盖其他账号。
set @pm_lps_exists := (select count(*) from sys_user where user_name = 'lps');
update sys_user u
set u.user_name = 'lps', u.nick_name = '林派生', u.password = @pm_team_password,
    u.status = '0', u.update_by = 'admin', u.update_time = sysdate()
where u.user_name = 'pm_lead'
  and @pm_lps_exists = 0;

set @pm_zxl_exists := (select count(*) from sys_user where user_name = 'zxl');
update sys_user u
set u.user_name = 'zxl', u.nick_name = '周晓岚', u.password = @pm_team_password,
    u.status = '0', u.update_by = 'admin', u.update_time = sysdate()
where u.user_name = 'pm_test'
  and @pm_zxl_exists = 0;

-- 陈思远不是当前项目成员，但保留其已存在的测试账号并统一为拼音账号，避免留下 pm_* 命名。
set @pm_csy_exists := (select count(*) from sys_user where user_name = 'csy');
update sys_user u
set u.user_name = 'csy', u.nick_name = '陈思远', u.password = @pm_team_password,
    u.status = '0', u.update_by = 'admin', u.update_time = sysdate()
where u.user_name = 'pm_design'
  and @pm_csy_exists = 0;

insert into sys_user(dept_id, user_name, nick_name, user_type, email, phonenumber, sex,
                     avatar, password, status, del_flag, create_by, create_time, remark)
select p.dept_id, x.user_name, p.person_name, '00', coalesce(p.email, ''), coalesce(p.mobile, ''),
       '2', '', @pm_team_password, '0', '0', 'admin', sysdate(),
       '智能装配视觉检测工作站研发项目团队测试账号'
from pm_person p
join (
    select '002' as person_code, 'lbh' as user_name
    union all select '008', 'lh'
    union all select '007', 'sn'
) x on x.person_code = p.person_code
where not exists (select 1 from sys_user u where u.user_name = x.user_name);

-- 已存在的团队账号也重置为统一测试密码，便于切换账号验证。
update sys_user
set password = @pm_team_password, status = '0', update_by = 'admin', update_time = sysdate()
where user_name in ('lps', 'lbh', 'lh', 'zxl', 'sn');

-- 普通管理员角色拥有项目管理菜单和接口权限，不授予超级管理员角色。
insert into sys_user_role(user_id, role_id)
select u.user_id, 100
from sys_user u
where u.user_name in ('lps', 'lbh', 'lh', 'zxl', 'sn')
  and not exists (select 1 from sys_user_role ur where ur.user_id = u.user_id and ur.role_id = 100);

-- 动态路由树需要同时授权项目管理顶层目录，工作台才会出现在登录后的路由中。
insert into sys_role_menu(role_id, menu_id)
select 100, m.menu_id
from sys_menu m
where m.parent_id = 0 and m.path = 'projectManagement'
  and not exists (select 1 from sys_role_menu rm where rm.role_id = 100 and rm.menu_id = m.menu_id);

-- 将项目团队成员绑定到对应系统账号；人员档案仍保留独立的业务字段。
update pm_person p
join sys_user u on u.user_name = case p.person_code
    when '001' then 'lps'
    when '002' then 'lbh'
    when '004' then 'zxl'
    when '007' then 'sn'
    when '008' then 'lh'
end
set p.user_id = u.user_id, p.update_by = 'admin', p.update_time = sysdate()
where p.person_code in ('001', '002', '004', '007', '008');
