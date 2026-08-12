create table if not exists pm_project_deliverable (
  deliverable_id bigint not null auto_increment, project_id bigint not null, task_id bigint not null,
  deliverable_name varchar(200) not null, deliverable_type varchar(32) not null,
  required_flag char(1) not null default '1', approval_required char(1) not null default '0', reviewer varchar(64) not null default 'admin',
  status varchar(32) not null default 'PENDING', description varchar(500) default null,
  submit_by varchar(64) default null, latest_file_url varchar(1000) default null, latest_external_url varchar(1000) default null,
  create_by varchar(64) default '', create_time datetime default null, update_by varchar(64) default '', update_time datetime default null,
  primary key(deliverable_id), key idx_pm_deliverable_task(task_id), key idx_pm_deliverable_project(project_id)
) engine=innodb comment='WBS任务应交付项';
create table if not exists pm_project_deliverable_submission (
  submission_id bigint not null auto_increment, deliverable_id bigint not null, version_no int not null,
  file_url varchar(1000) default null, external_url varchar(1000) default null, submit_by varchar(64) not null, submit_time datetime not null,
  review_result varchar(32) not null, review_comment varchar(1000) default null, review_by varchar(64) default null, review_time datetime default null,
  primary key(submission_id), key idx_pm_deliverable_submission(deliverable_id)
) engine=innodb comment='交付物提交与审核历史';
