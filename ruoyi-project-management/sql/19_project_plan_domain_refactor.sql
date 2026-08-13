-- 项目计划核心领域模型：WBS范围、Task执行、正式交付物和问题分离。
create table if not exists pm_project_wbs_node (
  wbs_id bigint not null auto_increment,
  project_id bigint not null,
  parent_id bigint not null default 0,
  wbs_code varchar(64) not null,
  node_type varchar(20) not null comment 'SUMMARY/WORK_PACKAGE',
  wbs_name varchar(200) not null,
  scope_description varchar(2000) null,
  owner_id bigint null,
  plan_start_date date null,
  plan_end_date date null,
  target_start_date date null,
  target_end_date date null,
  target_milestone varchar(200) null,
  acceptance_criteria varchar(2000) null,
  definition_of_done varchar(2000) null,
  priority varchar(20) null,
  estimated_hours decimal(12,2) null,
  budget_amount decimal(16,2) null,
  status varchar(32) not null default 'NOT_STARTED',
  progress int not null default 0,
  sort_order int not null default 0,
  create_by varchar(64) default '', create_time datetime,
  update_by varchar(64) default '', update_time datetime,
  primary key (wbs_id),
  unique key uk_pm_wbs_code (project_id,wbs_code),
  key idx_pm_wbs_parent (project_id,parent_id,sort_order),
  key idx_pm_wbs_owner (owner_id)
) engine=innodb comment='项目WBS范围节点';

create table if not exists pm_project_task (
  task_id bigint not null auto_increment,
  project_id bigint not null,
  work_package_id bigint not null,
  parent_task_id bigint not null default 0,
  task_code varchar(96) not null,
  task_type varchar(20) not null comment 'SUMMARY/EXECUTION',
  task_name varchar(200) not null,
  description varchar(2000) null,
  assignee_id bigint null,
  plan_start_date date null,
  plan_end_date date null,
  actual_start_date date null,
  actual_end_date date null,
  estimated_hours decimal(12,2) null,
  actual_hours decimal(12,2) null,
  priority varchar(20) null,
  status varchar(20) not null default 'NOT_STARTED',
  progress int not null default 0,
  pause_reason varchar(500) null,
  sort_order int not null default 0,
  create_by varchar(64) default '', create_time datetime,
  update_by varchar(64) default '', update_time datetime,
  primary key (task_id),
  unique key uk_pm_task_code (project_id,task_code),
  key idx_pm_task_package (work_package_id,parent_task_id,sort_order),
  key idx_pm_task_assignee (assignee_id)
) engine=innodb comment='工作包执行任务';

create table if not exists pm_project_task_output (
  output_id bigint not null auto_increment,
  task_id bigint not null,
  output_name varchar(200) not null,
  file_url varchar(1000) not null,
  description varchar(1000) null,
  create_by varchar(64) not null, create_time datetime not null,
  primary key (output_id), key idx_pm_task_output_task (task_id,create_time)
) engine=innodb comment='执行任务过程成果';

create table if not exists pm_project_issue (
  issue_id bigint not null auto_increment,
  project_id bigint not null,
  work_package_id bigint null,
  task_id bigint null,
  issue_code varchar(64) not null,
  issue_name varchar(200) not null,
  description varchar(2000) null,
  owner_id bigint null,
  severity varchar(20) not null default 'MEDIUM',
  status varchar(20) not null default 'OPEN',
  due_date date null,
  resolution varchar(2000) null,
  create_by varchar(64) default '', create_time datetime,
  update_by varchar(64) default '', update_time datetime,
  primary key (issue_id), unique key uk_pm_issue_code (project_id,issue_code),
  key idx_pm_issue_project (project_id,status), key idx_pm_issue_package (work_package_id), key idx_pm_issue_task (task_id)
) engine=innodb comment='项目问题跟踪';

set @has_task_id=(select count(*) from information_schema.columns where table_schema=database() and table_name='pm_project_deliverable' and column_name='task_id');
set @sql=if(@has_task_id=1,'alter table pm_project_deliverable change column task_id work_package_id bigint not null comment ''所属工作包''','select 1'); prepare stmt from @sql; execute stmt; deallocate prepare stmt;
set @has_planned=(select count(*) from information_schema.columns where table_schema=database() and table_name='pm_project_deliverable' and column_name='planned_date');
set @sql=if(@has_planned=0,'alter table pm_project_deliverable add column planned_date date null after description','select 1'); prepare stmt from @sql; execute stmt; deallocate prepare stmt;
set @has_acceptance=(select count(*) from information_schema.columns where table_schema=database() and table_name='pm_project_deliverable' and column_name='acceptance_criteria');
set @sql=if(@has_acceptance=0,'alter table pm_project_deliverable add column acceptance_criteria varchar(2000) null after planned_date','select 1'); prepare stmt from @sql; execute stmt; deallocate prepare stmt;
set @has_business_type=(select count(*) from information_schema.columns where table_schema=database() and table_name='pm_project_deliverable' and column_name='business_type');
set @sql=if(@has_business_type=0,'alter table pm_project_deliverable add column business_type varchar(32) null after acceptance_criteria, add column business_id varchar(128) null after business_type','select 1'); prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @has_outline_name=(select count(*) from information_schema.columns where table_schema=database() and table_name='pm_project_preliminary_plan' and column_name='outline_name');
set @has_phase_name=(select count(*) from information_schema.columns where table_schema=database() and table_name='pm_project_preliminary_plan' and column_name='phase_name');
set @sql=if(@has_outline_name=0 and @has_phase_name=1,'alter table pm_project_preliminary_plan change column phase_name outline_name varchar(100) not null','select 1'); prepare stmt from @sql; execute stmt; deallocate prepare stmt;
set @has_outline_description=(select count(*) from information_schema.columns where table_schema=database() and table_name='pm_project_preliminary_plan' and column_name='outline_description');
set @has_phase_goal=(select count(*) from information_schema.columns where table_schema=database() and table_name='pm_project_preliminary_plan' and column_name='phase_goal');
set @sql=if(@has_outline_description=0 and @has_phase_goal=1,'alter table pm_project_preliminary_plan change column phase_goal outline_description varchar(1000) null','select 1'); prepare stmt from @sql; execute stmt; deallocate prepare stmt;
set @has_converted_wbs=(select count(*) from information_schema.columns where table_schema=database() and table_name='pm_project_preliminary_plan' and column_name='converted_wbs_id');
set @has_converted_phase=(select count(*) from information_schema.columns where table_schema=database() and table_name='pm_project_preliminary_plan' and column_name='converted_phase_id');
set @sql=if(@has_converted_wbs=0 and @has_converted_phase=1,'alter table pm_project_preliminary_plan change column converted_phase_id converted_wbs_id bigint null','select 1'); prepare stmt from @sql; execute stmt; deallocate prepare stmt;

drop table if exists pm_project_phase_lifecycle_log;
drop table if exists pm_project_phase;
drop table if exists pm_project_work_item_lifecycle_log;
drop table if exists pm_project_work_item;
