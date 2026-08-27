-- 类型：结构
-- 适用版本：V2.0 基线与变更专题
-- 前置条件：19_project_plan_domain_refactor.sql、40_project_workflow.sql
-- 可重复执行：否
-- 数据风险：无（仅新增表）
-- 回退方式：确认无 V2 业务数据后删除新增表，或从升级前备份恢复
-- V2.0 项目计划基线与变更。仅用于按 V2 流程启动的新项目。
create table pm_project_plan_baseline (
  baseline_id bigint not null auto_increment, project_id bigint not null, version_no int not null,
  source_change_id bigint null, snapshot_json json not null,
  create_by varchar(64) default '', create_time datetime, primary key(baseline_id),
  unique key uk_pm_plan_baseline_version(project_id,version_no)
) engine=innodb comment='项目计划不可变基线';
create table pm_project_plan_change (
  change_id bigint not null auto_increment, change_code varchar(64) not null, project_id bigint not null,
  base_baseline_id bigint not null, base_version_no int not null, title varchar(200) not null,
  change_reason varchar(2000) not null, impact_description varchar(2000) not null,
  status varchar(30) not null comment 'DRAFT,PENDING,RETURNED,PENDING_APPLY,RECONCILE,APPLIED,WITHDRAWN',
  workflow_instance_id bigint null, applicant_user_id bigint not null,
  create_by varchar(64) default '', create_time datetime, update_by varchar(64) default '', update_time datetime,
  primary key(change_id), unique key uk_pm_plan_change_code(change_code), key idx_pm_plan_change_project(project_id,status)
) engine=innodb comment='项目计划变更单';
create table pm_project_plan_change_item (
  item_id bigint not null auto_increment, change_id bigint not null, module_type varchar(30) not null,
  operation_type varchar(20) not null, target_type varchar(30) not null, target_id bigint null,
  target_name varchar(200) null, before_json json null, after_json json null, item_reason varchar(1000) null,
  sort_order int default 0, primary key(item_id), key idx_pm_plan_change_item(change_id,sort_order)
) engine=innodb comment='项目计划变更项';
create table pm_project_plan_change_attachment (
  attachment_id bigint not null auto_increment, change_id bigint not null, file_name varchar(255) not null,
  file_url varchar(1000) not null, primary key(attachment_id), key idx_pm_plan_change_attachment(change_id)
) engine=innodb comment='项目计划变更附件';
create table pm_project_plan_change_audit (
  audit_id bigint not null auto_increment, change_id bigint not null, action varchar(30) not null,
  operator_user_id bigint null, operator varchar(64) default '', detail varchar(2000) null,
  create_time datetime, primary key(audit_id), key idx_pm_project_plan_change_audit(change_id, audit_id)
) engine=innodb comment='项目计划变更业务审计';
