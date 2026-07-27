-- Dify BOM_OCR 应用配置（种子数据）
-- 项目使用 agent_dify_app_config 表维护 Dify 多应用配置。
-- API Key 不写入脚本，请通过系统管理 > Dify 应用配置 页面填写后启用。

insert into agent_dify_app_config(app_code, app_name, app_type, api_base_url, enabled, remark, create_by, create_time)
select 'BOM_OCR', 'BOM图纸OCR识别', 'workflow', 'https://api.dify.ai/v1', 'N',
       'BOM 图纸 OCR/结构化识别应用，请填写 Dify API Key 后启用。', 'admin', sysdate()
where not exists (select 1 from agent_dify_app_config where app_code = 'BOM_OCR');
