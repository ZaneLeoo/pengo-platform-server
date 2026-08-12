-- 项目与WBS任务生命周期：实际时间、暂停原因及动作审计。
alter table pm_project add column actual_start_date date null comment '实际启动日期' after end_date;
alter table pm_project add column actual_end_date date null comment '实际完成日期' after actual_start_date;
alter table pm_project add column pause_reason varchar(500) null comment '最近一次暂停原因' after progress;

alter table pm_project_work_item add column actual_start_date date null comment '实际开始日期' after due_date;
alter table pm_project_work_item add column actual_end_date date null comment '实际完成日期' after actual_start_date;
alter table pm_project_work_item add column pause_reason varchar(500) null comment '最近一次暂停原因' after progress;

create table if not exists pm_project_lifecycle_log (
  log_id bigint not null auto_increment,
  project_id bigint not null,
  action varchar(20) not null,
  from_status varchar(20) not null,
  to_status varchar(20) not null,
  reason varchar(500) null,
  operator varchar(64) not null,
  operate_time datetime not null,
  primary key (log_id),
  key idx_pm_project_lifecycle(project_id, operate_time)
) engine=innodb comment='项目生命周期操作记录';

create table if not exists pm_project_work_item_lifecycle_log (
  log_id bigint not null auto_increment,
  item_id bigint not null,
  project_id bigint not null,
  action varchar(20) not null,
  from_status varchar(20) not null,
  to_status varchar(20) not null,
  reason varchar(500) null,
  operator varchar(64) not null,
  operate_time datetime not null,
  primary key (log_id),
  key idx_pm_work_item_lifecycle(item_id, operate_time),
  key idx_pm_work_item_lifecycle_project(project_id, operate_time)
) engine=innodb comment='WBS任务生命周期操作记录';
