-- “项目管理”更符合用户对项目主数据维护入口的认知；数据库表和权限编码保持稳定。
update sys_menu
set menu_name = '项目管理', update_by = 'admin', update_time = sysdate()
where perms = 'projectManagement:project:list' and menu_type = 'C';
