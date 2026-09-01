-- 类型：结构 / 预置
-- 适用版本：V2 预算与成本阶段 2 → 阶段 3
-- 前置条件：43_project_cost_category.sql、44_project_budget.sql、pm_project_wbs_node
-- 可重复执行：是
-- 数据风险：无
-- 回退方式：确认无业务引用后删除 pm_work_package_budget_line

create table if not exists pm_work_package_budget_line (
  work_package_budget_line_id bigint not null auto_increment comment '工作包预算明细ID',
  project_id bigint not null comment '项目ID',
  work_package_id bigint not null comment '工作包ID',
  cost_category_id bigint not null comment '成本类别ID',
  category_code varchar(50) not null comment '类别编码快照',
  category_name varchar(100) not null comment '类别名称快照',
  category_path varchar(500) not null comment '类别路径快照',
  budget_amount decimal(16,2) not null comment '人民币含税预算金额',
  estimation_basis varchar(1000) not null comment '测算依据',
  sort_order int not null default 0 comment '排序',
  create_by varchar(64) default '' comment '创建者', create_time datetime default current_timestamp,
  update_by varchar(64) default '', update_time datetime default null,
  primary key (work_package_budget_line_id),
  unique key uk_pm_wp_budget_category (work_package_id,cost_category_id),
  key idx_pm_wp_budget_project_category (project_id,cost_category_id),
  key idx_pm_wp_budget_package (work_package_id),
  constraint fk_pm_wp_budget_project foreign key (project_id) references pm_project(project_id) on delete cascade,
  constraint fk_pm_wp_budget_package foreign key (work_package_id) references pm_project_wbs_node(wbs_id) on delete cascade,
  constraint fk_pm_wp_budget_category foreign key (cost_category_id) references pm_cost_category(cost_category_id)
) engine=InnoDB comment='工作包分类预算明细';
