-- BOM AI 图纸导入动态限制。修改后由后端立即读取生效，前端刷新页面后展示最新值。
insert into sys_config(config_name, config_key, config_value, config_type, create_by, create_time, remark)
select 'BOM AI导入-单次最大图片数', 'mes.bom.ai.import.maxImageCount', '20', 'Y', 'admin', sysdate(), '范围 1~100；仅图片批次生效'
where not exists (select 1 from sys_config where config_key = 'mes.bom.ai.import.maxImageCount');

insert into sys_config(config_name, config_key, config_value, config_type, create_by, create_time, remark)
select 'BOM AI导入-PDF最大页数', 'mes.bom.ai.import.maxPdfPages', '20', 'Y', 'admin', sysdate(), '范围 1~100；仅 PDF 批次生效'
where not exists (select 1 from sys_config where config_key = 'mes.bom.ai.import.maxPdfPages');

insert into sys_config(config_name, config_key, config_value, config_type, create_by, create_time, remark)
select 'BOM AI导入-单文件大小(MB)', 'mes.bom.ai.import.maxFileSizeMb', '10', 'Y', 'admin', sysdate(), '范围 1~50；图片和 PDF 均生效'
where not exists (select 1 from sys_config where config_key = 'mes.bom.ai.import.maxFileSizeMb');

insert into sys_config(config_name, config_key, config_value, config_type, create_by, create_time, remark)
select 'BOM AI导入-单批总大小(MB)', 'mes.bom.ai.import.maxRequestSizeMb', '100', 'Y', 'admin', sysdate(), '范围 1~200；图片和 PDF 均生效'
where not exists (select 1 from sys_config where config_key = 'mes.bom.ai.import.maxRequestSizeMb');
