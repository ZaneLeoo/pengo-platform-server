-- 为已部署的项目执行项表增加“关联WBS任务”，交付物和问题均可追溯到具体任务。
alter table pm_project_work_item add column task_id bigint null comment '关联WBS任务ID' after parent_id;
alter table pm_project_work_item add key idx_pm_work_task(task_id);
