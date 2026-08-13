-- 项目主档表
create table if not exists pm_project (
  project_id bigint not null auto_increment,
  project_code varchar(32) not null,
  project_name varchar(100) not null,
  category_id bigint not null,
  manager_id bigint not null,
  start_date date not null,
  end_date date not null,
  status varchar(20) not null default 'DRAFT',
  progress int not null default 0,
  project_goal varchar(1000) not null,
  create_by varchar(64) default '',
  create_time datetime,
  update_by varchar(64) default '',
  update_time datetime,
  remark varchar(500),
  primary key (project_id),
  unique key uk_pm_project_code (project_code),
  key idx_pm_project_category (category_id),
  key idx_pm_project_manager (manager_id)
) engine=innodb comment='项目主档';
