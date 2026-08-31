-- V2 预算与成本阶段 2：项目分类预算
create table if not exists pm_project_budget_line (
  budget_line_id bigint not null auto_increment comment '预算明细ID',
  project_id bigint not null comment '项目ID',
  cost_category_id bigint not null comment '成本类别ID',
  category_code varchar(50) not null comment '类别编码快照',
  category_name varchar(100) not null comment '类别名称快照',
  category_path varchar(500) not null comment '类别路径快照',
  budget_amount decimal(16,2) not null comment '人民币含税预算金额',
  estimation_basis varchar(1000) not null comment '测算依据',
  sort_order int not null default 0 comment '排序',
  create_by varchar(64) default '' comment '创建者', create_time datetime default current_timestamp,
  update_by varchar(64) default '', update_time datetime default null,
  primary key (budget_line_id),
  unique key uk_pm_project_budget_category (project_id,cost_category_id),
  key idx_pm_project_budget_category (cost_category_id),
  constraint fk_pm_project_budget_project foreign key (project_id) references pm_project(project_id) on delete cascade,
  constraint fk_pm_project_budget_category foreign key (cost_category_id) references pm_cost_category(cost_category_id)
) engine=InnoDB comment='项目分类预算明细';

insert into pm_cost_category(parent_id,ancestors,category_code,category_name,level_no,
  allow_manual_entry,system_flag,status,sort_order,description,create_by,create_time)
select 0,'0','CONTINGENCY','预备费',1,'1','1','0',90,'尚未明确用途的项目风险储备预算','admin',sysdate()
where not exists(select 1 from pm_cost_category where category_code='CONTINGENCY');

