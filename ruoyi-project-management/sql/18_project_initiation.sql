-- 项目申请与立项审批。
alter table pm_project
  add column applicant varchar(64) null comment '申请人账号' after manager_id,
  add column applicant_dept_id bigint null comment '申请部门' after applicant,
  add column project_background text null comment '项目背景' after project_goal,
  add column project_scope text null comment '项目范围' after project_background,
  add column out_of_scope text null comment '范围外事项' after project_scope,
  add column expected_outcome text null comment '预期成果' after out_of_scope,
  add column resource_requirement text null comment '资源需求' after expected_outcome,
  add column budget_required char(1) not null default '0' comment '是否需要预算' after resource_requirement,
  add column budget_amount decimal(16,2) null comment '预算总额' after budget_required,
  add column budget_description varchar(1000) null comment '预算说明' after budget_amount,
  add column major_risk text null comment '主要风险' after budget_description,
  add column technical_feasibility text null comment '技术可行性' after major_risk,
  add column resource_feasibility text null comment '资源可行性' after technical_feasibility,
  add column feasibility_conclusion text null comment '综合结论' after resource_feasibility,
  add column initiation_version int not null default 0 comment '立项提交版本' after feasibility_conclusion,
  add column initiation_time datetime null comment '正式立项时间' after initiation_version;

create table if not exists pm_project_preliminary_plan (
  plan_id bigint not null auto_increment,
  project_id bigint not null,
  phase_name varchar(100) not null,
  start_date date not null,
  end_date date not null,
  milestone_name varchar(200) not null,
  phase_goal varchar(1000) null,
  sort_order int not null default 0,
  converted_phase_id bigint null,
  create_by varchar(64) default '', create_time datetime,
  update_by varchar(64) default '', update_time datetime,
  primary key(plan_id), key idx_pm_pre_plan_project(project_id,sort_order)
) engine=innodb comment='项目立项初步计划';

create table if not exists pm_project_initiation_approval (
  approval_id bigint not null auto_increment,
  project_id bigint not null,
  version_no int not null,
  snapshot_json longtext not null,
  submit_by varchar(64) not null,
  submit_time datetime not null,
  status varchar(20) not null default 'PENDING',
  review_by varchar(64) null,
  review_time datetime null,
  review_comment varchar(1000) null,
  primary key(approval_id), unique key uk_pm_initiation_version(project_id,version_no),
  key idx_pm_initiation_project(project_id,submit_time)
) engine=innodb comment='项目立项审批记录';

update pm_project set status='APPROVED' where status='PLANNED';
