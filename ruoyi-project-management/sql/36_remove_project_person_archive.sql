-- 项目管理改用 sys_user 作为唯一人员身份来源。
-- 清理此前测试项目业务数据及“人员档案”菜单；不删除 pm_person 表，保留为历史物理表，应用不再读取。

set foreign_key_checks = 0;
delete from pm_project_deliverable_submission;
delete from pm_project_deliverable;
delete from pm_project_task_operation_log;
delete from pm_project_task_output;
delete from pm_project_task;
delete from pm_project_issue;
delete from pm_project_wbs_node;
delete from pm_project_member;
delete from pm_project_initiation_attachment;
delete from pm_project_initiation_approval;
delete from pm_project_preliminary_plan;
delete from pm_project_lifecycle_log;
delete from pm_project;
set foreign_key_checks = 1;

delete rm from sys_role_menu rm
join sys_menu m on m.menu_id = rm.menu_id
where m.perms like 'projectManagement:person%';
delete from sys_menu where perms like 'projectManagement:person%'
   or (parent_id in (select menu_id from (select menu_id from sys_menu where perms = 'projectManagement:person:list') x));
