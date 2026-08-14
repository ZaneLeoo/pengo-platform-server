-- 立项申请支撑材料：草稿附件在提交时绑定到对应审批版本，退回后复制为新的草稿附件。
create table if not exists pm_project_initiation_attachment (
  attachment_id bigint not null auto_increment,
  project_id bigint not null,
  approval_id bigint null,
  version_no int not null default 0,
  section_code varchar(32) not null comment 'BASIC_SCHEME/RESOURCE_BUDGET/RISK_ASSESSMENT',
  file_name varchar(255) not null,
  file_url varchar(1000) not null,
  file_size bigint null,
  file_ext varchar(32) null,
  mime_type varchar(128) null,
  description varchar(500) null,
  upload_by varchar(64) not null,
  upload_time datetime not null,
  primary key (attachment_id),
  key idx_pm_initiation_attachment_project (project_id, section_code),
  key idx_pm_initiation_attachment_approval (approval_id),
  constraint fk_pm_initiation_attachment_project foreign key (project_id)
    references pm_project(project_id),
  constraint fk_pm_initiation_attachment_approval foreign key (approval_id)
    references pm_project_initiation_approval(approval_id)
) engine=innodb comment='项目立项申请支撑材料';
