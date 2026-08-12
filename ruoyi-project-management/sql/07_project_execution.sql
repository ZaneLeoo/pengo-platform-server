create table if not exists pm_project_work_item (
  item_id bigint not null auto_increment, project_id bigint not null, parent_id bigint not null default 0, task_id bigint,
  item_type varchar(20) not null, item_code varchar(32) not null, item_name varchar(200) not null,
  owner_id bigint, status varchar(20) not null, priority varchar(20), start_date date, due_date date,
  progress int not null default 0, file_url varchar(500), description varchar(2000), sort_order int not null default 0,
  create_by varchar(64) default '', create_time datetime, update_by varchar(64) default '', update_time datetime,
  primary key(item_id), unique key uk_pm_work_item_code(item_code), key idx_pm_work_project_type(project_id,item_type), key idx_pm_work_task(task_id), key idx_pm_work_owner(owner_id)
) engine=innodb comment='项目任务交付物问题统一执行项';
