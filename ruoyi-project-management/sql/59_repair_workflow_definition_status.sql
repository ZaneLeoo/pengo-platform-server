-- 修复历史流程定义状态为空，导致已发布流程无法被业务提交选中的问题。
update pm_workflow_definition
set status = 'ENABLED'
where status is null or trim(status) = '';
