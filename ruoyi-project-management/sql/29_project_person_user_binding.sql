-- 项目人员档案与系统登录账号绑定。
-- 业务人员档案和登录身份继续分表；本字段只保存可选的一对一关联。
set @pm_person_user_id_exists := (
    select count(*)
    from information_schema.columns
    where table_schema = database()
      and table_name = 'pm_person'
      and column_name = 'user_id'
);
set @pm_person_user_id_sql := if(
    @pm_person_user_id_exists = 0,
    'alter table pm_person add column user_id bigint default null comment ''关联系统用户ID'' after dept_id',
    'select 1'
);
prepare pm_person_user_id_stmt from @pm_person_user_id_sql;
execute pm_person_user_id_stmt;
deallocate prepare pm_person_user_id_stmt;

set @pm_person_user_key_exists := (
    select count(*)
    from information_schema.statistics
    where table_schema = database()
      and table_name = 'pm_person'
      and index_name = 'uk_pm_person_user_id'
);
set @pm_person_user_key_sql := if(
    @pm_person_user_key_exists = 0,
    'alter table pm_person add unique key uk_pm_person_user_id (user_id)',
    'select 1'
);
prepare pm_person_user_key_stmt from @pm_person_user_key_sql;
execute pm_person_user_key_stmt;
deallocate prepare pm_person_user_key_stmt;
