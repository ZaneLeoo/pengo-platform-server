-- 类型：结构 / 预置
-- 适用版本：V2 预算与成本阶段 3 → 阶段 4
-- 前置条件：43_project_cost_category.sql、44_project_budget.sql、45_work_package_budget.sql、pm_project_wbs_node
-- 可重复执行：是
-- 数据风险：无
-- 回退方式：确认无业务引用后删除 pm_project_actual_cost

create table if not exists pm_project_actual_cost (
  actual_cost_id bigint not null auto_increment comment '实际成本记录ID',
  project_id bigint not null comment '项目ID',
  work_package_id bigint null comment '工作包ID，为空表示项目级成本',
  cost_category_id bigint not null comment '成本类别ID',
  category_code varchar(50) not null comment '类别编码快照',
  category_name varchar(100) not null comment '类别名称快照',
  category_path varchar(500) not null comment '类别路径快照',
  actual_amount decimal(16,2) not null comment '人民币含税实际成本金额',
  occur_date date not null comment '实际发生日期',
  description varchar(500) not null comment '成本事由说明',
  correction_no int not null default 0 comment '更正次数，仅供展示',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime default current_timestamp,
  update_by varchar(64) default '',
  update_time datetime default null,
  primary key (actual_cost_id),
  key idx_pm_ac_project_category (project_id,cost_category_id),
  key idx_pm_ac_category (cost_category_id),
  key idx_pm_ac_work_package (work_package_id),
  key idx_pm_ac_occur_date (occur_date),
  constraint fk_pm_ac_project foreign key (project_id) references pm_project(project_id) on delete cascade,
  constraint fk_pm_ac_package foreign key (work_package_id) references pm_project_wbs_node(wbs_id) on delete cascade,
  constraint fk_pm_ac_category foreign key (cost_category_id) references pm_cost_category(cost_category_id)
) engine=InnoDB comment='项目实际成本登记表';
