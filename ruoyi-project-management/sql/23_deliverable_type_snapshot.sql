-- 将类型配置快照写入具体交付要求，后续类型配置变更不影响历史项目。
set @has_type_id=(select count(*) from information_schema.columns where table_schema=database() and table_name='pm_project_deliverable' and column_name='deliverable_type_id');
set @sql=if(@has_type_id=0,'alter table pm_project_deliverable add column deliverable_type_id bigint null after deliverable_type, add column submission_mode varchar(20) null after deliverable_type_id, add column allowed_extensions varchar(1000) null after submission_mode','select 1'); prepare stmt from @sql; execute stmt; deallocate prepare stmt;
update pm_project_deliverable d join pm_project_deliverable_type t on t.type_code=d.deliverable_type
set d.deliverable_type_id=t.type_id, d.submission_mode=t.submission_mode,
    d.allowed_extensions=(select group_concat(f.file_extension order by f.file_extension separator ',') from pm_project_deliverable_type_format f where f.type_id=t.type_id)
where d.deliverable_type_id is null;
