-- 历史版本的流程编辑页面未回传 status，曾将流程定义状态更新为 NULL。
-- 发布版本存在但定义未启用时，业务提交无法选中该流程。
update pm_workflow_definition
set status = 'ENABLED'
where status is null or trim(status) = '';
