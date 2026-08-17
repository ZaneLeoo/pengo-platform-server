-- 项目管理以系统用户为唯一人员身份来源。
-- 本脚本只新增并回填 user_id 字段；旧 person_id 字段在全部应用代码切换并验收后再移除。

delimiter $$
drop procedure if exists pm_add_column_if_missing $$
create procedure pm_add_column_if_missing(in p_table_name varchar(64), in p_column_name varchar(64), in p_definition_sql varchar(512))
begin
  if not exists (select 1 from information_schema.columns where table_schema = database() and table_name = p_table_name and column_name = p_column_name) then
    set @statement = concat('alter table ', p_table_name, ' add column ', p_column_name, ' ', p_definition_sql);
    prepare statement_handle from @statement;
    execute statement_handle;
    deallocate prepare statement_handle;
  end if;
end $$
delimiter ;

call pm_add_column_if_missing('pm_project', 'manager_user_id', 'bigint null after manager_id');
call pm_add_column_if_missing('pm_project_member', 'user_id', 'bigint null after person_id');
call pm_add_column_if_missing('pm_project_wbs_node', 'owner_user_id', 'bigint null after owner_id');
call pm_add_column_if_missing('pm_project_task', 'assignee_user_id', 'bigint null after assignee_id');
call pm_add_column_if_missing('pm_project_issue', 'owner_user_id', 'bigint null after owner_id');

update pm_project p left join pm_person person on person.person_id = p.manager_id
set p.manager_user_id = person.user_id
where p.manager_user_id is null;

update pm_project_member member left join pm_person person on person.person_id = member.person_id
set member.user_id = person.user_id
where member.user_id is null;

update pm_project_wbs_node wbs left join pm_person person on person.person_id = wbs.owner_id
set wbs.owner_user_id = person.user_id
where wbs.owner_user_id is null;

update pm_project_task task left join pm_person person on person.person_id = task.assignee_id
set task.assignee_user_id = person.user_id
where task.assignee_user_id is null;

update pm_project_issue issue_item left join pm_person person on person.person_id = issue_item.owner_id
set issue_item.owner_user_id = person.user_id
where issue_item.owner_user_id is null;

set @index_sql = if(
  exists(select 1 from information_schema.statistics where table_schema = database() and table_name = 'pm_project_member' and index_name = 'uk_pm_project_member_user'),
  'select 1',
  'alter table pm_project_member add unique key uk_pm_project_member_user (project_id, user_id)'
);
prepare index_statement from @index_sql;
execute index_statement;
deallocate prepare index_statement;

drop procedure if exists pm_add_column_if_missing;
