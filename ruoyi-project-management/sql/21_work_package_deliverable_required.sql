-- 工作包是否需要正式交付物：未启用时，任务完成即可闭环；启用后必须配置必交交付物。
set @has_deliverable_required = (
  select count(*) from information_schema.columns
  where table_schema = database() and table_name = 'pm_project_wbs_node'
    and column_name = 'deliverable_required'
);
set @sql = if(@has_deliverable_required = 0,
  'alter table pm_project_wbs_node add column deliverable_required char(1) not null default ''0'' comment ''是否需要正式交付物：0否 1是'' after definition_of_done',
  'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- 已存在必交交付物的历史工作包保持为“需要正式交付物”。
update pm_project_wbs_node w
set w.deliverable_required = '1'
where w.node_type = 'WORK_PACKAGE'
  and exists (
    select 1 from pm_project_deliverable d
    where d.work_package_id = w.wbs_id and d.required_flag = '1'
  );
