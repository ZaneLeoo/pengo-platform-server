-- 类型：结构 / 菜单与授权
-- 适用版本：V2 预算与成本阶段 5（项目工时）
-- 前置条件：44_project_budget.sql、45_work_package_budget.sql、46_project_actual_cost.sql、40_project_workflow.sql
-- 可重复执行：是

set names utf8mb4;

create table if not exists pm_project_labor_rate (
  rate_id bigint not null auto_increment comment '人员小时单价ID',
  user_id bigint not null comment '系统用户ID',
  user_name varchar(64) not null comment '用户账号快照',
  nick_name varchar(64) not null comment '用户姓名快照',
  effective_start_date date not null comment '生效开始日期',
  effective_end_date date null comment '生效结束日期',
  hourly_rate decimal(16,2) not null comment '人民币含税小时单价',
  status char(1) not null default '0' comment '0启用 1停用',
  create_by varchar(64) default '', create_time datetime default current_timestamp,
  update_by varchar(64) default '', update_time datetime default null,
  primary key(rate_id), key idx_pm_labor_rate_user_date(user_id,effective_start_date)
) engine=InnoDB comment='项目人员小时单价';

create table if not exists pm_project_work_hours_sheet (
  sheet_id bigint not null auto_increment comment '周工时单ID',
  user_id bigint not null comment '填报人用户ID',
  user_name varchar(64) not null comment '填报人账号快照',
  nick_name varchar(64) not null comment '填报人姓名快照',
  week_start_date date not null comment '自然周周一',
  week_end_date date not null comment '自然周周日',
  project_id bigint null comment '审批上下文项目ID',
  sheet_type varchar(20) not null default 'NORMAL' comment 'NORMAL常规周单 CORRECTION更正周单',
  status varchar(20) not null default 'DRAFT' comment 'DRAFT/IN_APPROVAL/RETURNED/ARCHIVED',
  late_report_reason varchar(500) null comment '补报原因',
  workflow_instance_id bigint null comment '审批实例ID',
  submit_time datetime null, archive_time datetime null,
  create_by varchar(64) default '', create_time datetime default current_timestamp,
  update_by varchar(64) default '', update_time datetime default null,
  primary key(sheet_id), unique key uk_pm_work_hours_user_week_type(user_id,week_start_date,sheet_type),
  key idx_pm_work_hours_sheet_status(status)
) engine=InnoDB comment='项目自然周工时单';

-- 兼容已先执行过本脚本的环境：允许同一人员同一周保留一张常规单和一张更正单。
set @has_sheet_type := (select count(*) from information_schema.columns where table_schema=database() and table_name='pm_project_work_hours_sheet' and column_name='sheet_type');
set @add_sheet_type_sql := if(@has_sheet_type=0, 'alter table pm_project_work_hours_sheet add column sheet_type varchar(20) not null default ''NORMAL'' comment ''NORMAL常规周单 CORRECTION更正周单'' after project_id', 'select 1');
prepare stmt from @add_sheet_type_sql; execute stmt; deallocate prepare stmt;
set @old_week_unique := (select index_name from information_schema.statistics where table_schema=database() and table_name='pm_project_work_hours_sheet' and index_name='uk_pm_work_hours_user_week' limit 1);
set @drop_week_unique_sql := if(@old_week_unique is null, 'select 1', 'alter table pm_project_work_hours_sheet drop index uk_pm_work_hours_user_week');
prepare stmt from @drop_week_unique_sql; execute stmt; deallocate prepare stmt;
set @has_new_week_unique := (select count(*) from information_schema.statistics where table_schema=database() and table_name='pm_project_work_hours_sheet' and index_name='uk_pm_work_hours_user_week_type');
set @add_new_week_unique_sql := if(@has_new_week_unique=0, 'create unique index uk_pm_work_hours_user_week_type on pm_project_work_hours_sheet(user_id,week_start_date,sheet_type)', 'select 1');
prepare stmt from @add_new_week_unique_sql; execute stmt; deallocate prepare stmt;

create table if not exists pm_project_work_hours_entry (
  entry_id bigint not null auto_increment comment '工时明细ID',
  sheet_id bigint not null comment '周工时单ID',
  project_id bigint not null, work_package_id bigint not null, task_id bigint not null,
  report_user_id bigint not null comment '填报人用户ID',
  project_name varchar(200) not null, work_package_name varchar(200) not null, task_name varchar(200) not null,
  report_user_name varchar(64) not null, report_nick_name varchar(64) not null,
  work_date date not null, hours decimal(6,1) not null, overtime_flag char(1) not null default '0',
  work_description varchar(1000) not null, achievement_description varchar(1000) not null,
  source_entry_id bigint null comment '被更正的原工时明细', correction_reason varchar(500) null,
  entry_status varchar(20) not null default 'DRAFT', cost_status varchar(20) null,
  rate_id_snapshot bigint null, rate_amount_snapshot decimal(16,2) null,
  actual_cost_id bigint null,
  create_by varchar(64) default '', create_time datetime default current_timestamp,
  update_by varchar(64) default '', update_time datetime default null,
  primary key(entry_id), key idx_pm_work_hours_entry_sheet(sheet_id),
  key idx_pm_work_hours_entry_task_date(task_id,work_date),
  key idx_pm_work_hours_entry_user_date(report_user_id,work_date),
  key idx_pm_work_hours_entry_source(source_entry_id),
  constraint fk_pm_work_hours_entry_sheet foreign key(sheet_id) references pm_project_work_hours_sheet(sheet_id) on delete cascade
) engine=InnoDB comment='项目工时日明细';

alter table pm_project_actual_cost modify source_type varchar(30) not null default 'MANUAL' comment '来源类型：MANUAL/PURCHASE_INBOUND/WORK_HOURS';

set @pm_root := (select menu_id from sys_menu where menu_type='M' and path='projectManagement' order by menu_id limit 1);
insert into sys_menu(menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select '我的工时',@pm_root,8,'work-hours','projectManagement/workHours/my',null,'ProjectMyWorkHours',1,0,'C','0','0','projectManagement:workHours:query','clock-circle','admin',sysdate(),'个人工时填报'
where @pm_root is not null and not exists(select 1 from sys_menu where parent_id=@pm_root and path='work-hours');
set @my_hours_menu := (select menu_id from sys_menu where parent_id=@pm_root and path='work-hours' order by menu_id limit 1);
update sys_menu set menu_name='我的工时',component='projectManagement/workHours/my',route_name='ProjectMyWorkHours',perms='projectManagement:workHours:query',icon='clock-circle',visible='0',status='0' where menu_id=@my_hours_menu;
insert into sys_menu(menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select '工时管理',@pm_root,9,'work-hours-manage','projectManagement/workHours/manage',null,'ProjectWorkHoursManage',1,0,'C','0','0','projectManagement:workHours:manage','profile','admin',sysdate(),'项目工时查询管理'
where @pm_root is not null and not exists(select 1 from sys_menu where parent_id=@pm_root and path='work-hours-manage');
set @hours_manage_menu := (select menu_id from sys_menu where parent_id=@pm_root and path='work-hours-manage' order by menu_id limit 1);
insert into sys_menu(menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select '人工单价',@pm_root,10,'labor-rate','projectManagement/workHours/laborRate',null,'ProjectLaborRate',1,0,'C','0','0','projectManagement:laborRate:query','money-collect','admin',sysdate(),'人员小时单价维护'
where @pm_root is not null and not exists(select 1 from sys_menu where parent_id=@pm_root and path='labor-rate');
set @labor_rate_menu := (select menu_id from sys_menu where parent_id=@pm_root and path='labor-rate' order by menu_id limit 1);

insert into sys_menu(menu_name,parent_id,order_num,path,component,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time)
select a.name,a.parent_id,a.order_num,'','',1,0,'F','0','0',a.perms,'#','admin',sysdate()
from (select '工时维护' name,@my_hours_menu parent_id,1 order_num,'projectManagement:workHours:edit' perms union all select '工时管理查询',@hours_manage_menu,1,'projectManagement:workHours:manage' union all select '单价维护',@labor_rate_menu,1,'projectManagement:laborRate:edit') a
where a.parent_id is not null and not exists(select 1 from sys_menu m where m.perms=a.perms);

-- 部分 Windows/Docker MySQL 客户端会错误传递中文参数；以 UTF-8 字节回写名称，保证菜单不出现乱码。
update sys_menu set menu_name=convert(0xE68891E79A84E5B7A5E697B6 using utf8mb4) where menu_id=@my_hours_menu;
update sys_menu set menu_name=convert(0xE5B7A5E697B6E7AEA1E79086 using utf8mb4) where menu_id=@hours_manage_menu;
update sys_menu set menu_name=convert(0xE4BABAE5B7A5E58D95E4BBB7 using utf8mb4) where menu_id=@labor_rate_menu;
update sys_menu set menu_name=convert(0xE5B7A5E697B6E7BBB4E68AA4 using utf8mb4) where perms='projectManagement:workHours:edit';
update sys_menu set menu_name=convert(0xE5B7A5E697B6E7AEA1E79086E69FA5E8AFA2 using utf8mb4) where perms='projectManagement:workHours:manage' and menu_type='F';
update sys_menu set menu_name=convert(0xE58D95E4BBB7E7BBB4E68AA4 using utf8mb4) where perms='projectManagement:laborRate:edit';

-- 明确授予管理员角色；业务服务层仍限制只能填报本人任务，管理员没有代填或越权能力。
insert ignore into sys_role_menu(role_id,menu_id)
select 1,menu_id from sys_menu where perms in ('projectManagement:workHours:query','projectManagement:workHours:edit','projectManagement:workHours:manage','projectManagement:laborRate:query','projectManagement:laborRate:edit');
