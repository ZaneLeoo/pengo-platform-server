-- 类型：结构 / 字典注释补齐
-- 适用版本：V2 完整项目测试回归
-- 可重复执行：是（仅 MODIFY COLUMN，不新增列或索引）
-- 说明：补齐采购实际成本、人员单价和工时表的字段注释，避免数据字典出现空白字段。

set names utf8mb4;

alter table pm_project_actual_cost modify column source_line_id bigint null comment '来源单据明细ID快照';
alter table pm_project_actual_cost modify column source_document_no varchar(64) null comment '来源单号快照';
alter table pm_project_actual_cost modify column source_line_no varchar(32) null comment '来源单据行号快照';
alter table pm_project_actual_cost modify column cost_status varchar(16) not null default 'EFFECTIVE' comment '成本状态：EFFECTIVE有效/REVERSED已冲销';
alter table pm_project_actual_cost modify column reverse_reason varchar(500) null comment '冲销原因';
alter table pm_project_actual_cost modify column reversed_by varchar(64) null comment '冲销人账号';
alter table pm_project_actual_cost modify column reversed_time datetime null comment '冲销时间';

alter table pm_project_labor_rate modify column create_by varchar(64) default '' comment '创建人账号';
alter table pm_project_labor_rate modify column create_time datetime default current_timestamp comment '创建时间';
alter table pm_project_labor_rate modify column update_by varchar(64) default '' comment '最后修改人账号';
alter table pm_project_labor_rate modify column update_time datetime null comment '最后修改时间';

alter table pm_project_work_hours_entry modify column project_id bigint not null comment '项目ID';
alter table pm_project_work_hours_entry modify column work_package_id bigint not null comment '工作包ID';
alter table pm_project_work_hours_entry modify column task_id bigint not null comment '执行任务ID';
alter table pm_project_work_hours_entry modify column project_name varchar(200) not null comment '项目名称快照';
alter table pm_project_work_hours_entry modify column work_package_name varchar(200) not null comment '工作包名称快照';
alter table pm_project_work_hours_entry modify column task_name varchar(200) not null comment '任务名称快照';
alter table pm_project_work_hours_entry modify column report_user_name varchar(64) not null comment '填报人账号快照';
alter table pm_project_work_hours_entry modify column report_nick_name varchar(64) not null comment '填报人姓名快照';
alter table pm_project_work_hours_entry modify column work_date date not null comment '实际工作日期';
alter table pm_project_work_hours_entry modify column hours decimal(6,1) not null comment '投入工时，按0.5小时粒度';
alter table pm_project_work_hours_entry modify column overtime_flag char(1) not null default '0' comment '是否加班：0否1是';
alter table pm_project_work_hours_entry modify column work_description varchar(1000) not null comment '工作内容';
alter table pm_project_work_hours_entry modify column achievement_description varchar(1000) not null comment '工作成果';
alter table pm_project_work_hours_entry modify column correction_reason varchar(500) null comment '更正原因';
alter table pm_project_work_hours_entry modify column entry_status varchar(20) not null default 'DRAFT' comment '明细状态：DRAFT/IN_APPROVAL/ARCHIVED/VOID';
alter table pm_project_work_hours_entry modify column cost_status varchar(20) null comment '人工成本状态：待处理/已归集等';
alter table pm_project_work_hours_entry modify column rate_id_snapshot bigint null comment '人员单价记录快照ID';
alter table pm_project_work_hours_entry modify column rate_amount_snapshot decimal(16,2) null comment '人员小时单价快照';
alter table pm_project_work_hours_entry modify column actual_cost_id bigint null comment '关联人工实际成本记录ID';
alter table pm_project_work_hours_entry modify column create_by varchar(64) default '' comment '创建人账号';
alter table pm_project_work_hours_entry modify column create_time datetime default current_timestamp comment '创建时间';
alter table pm_project_work_hours_entry modify column update_by varchar(64) default '' comment '最后修改人账号';
alter table pm_project_work_hours_entry modify column update_time datetime null comment '最后修改时间';

alter table pm_project_work_hours_sheet modify column submit_time datetime null comment '提交审批时间';
alter table pm_project_work_hours_sheet modify column archive_time datetime null comment '审批归档时间';
alter table pm_project_work_hours_sheet modify column create_by varchar(64) default '' comment '创建人账号';
alter table pm_project_work_hours_sheet modify column create_time datetime default current_timestamp comment '创建时间';
alter table pm_project_work_hours_sheet modify column update_by varchar(64) default '' comment '最后修改人账号';
alter table pm_project_work_hours_sheet modify column update_time datetime null comment '最后修改时间';
