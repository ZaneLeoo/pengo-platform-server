-- 任务可明确声明是否必须产出交付物；默认否，避免阶段汇总节点被误拦截。
alter table pm_project_work_item add column deliverable_required char(1) not null default '0' comment '是否要求交付物：0否1是' after task_id;
