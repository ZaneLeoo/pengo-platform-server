-- Dify 知识库服务 API 配置；密钥请通过系统参数管理维护，不要提交真实密钥。
insert into sys_config(config_name, config_key, config_value, config_type, create_by, create_time, remark)
select 'Dify知识库API地址', 'agent.dify.knowledge.api_base_url', 'http://localhost/v1', 'Y', 'admin', sysdate(),
       'Dify知识库 Service API 基础地址'
where not exists (select 1 from sys_config where config_key = 'agent.dify.knowledge.api_base_url');

insert into sys_config(config_name, config_key, config_value, config_type, create_by, create_time, remark)
select 'Dify知识库API密钥', 'agent.dify.knowledge.api_key', '', 'Y', 'admin', sysdate(),
       'Dify知识库 Service API Key，仅保存在后端'
where not exists (select 1 from sys_config where config_key = 'agent.dify.knowledge.api_key');
