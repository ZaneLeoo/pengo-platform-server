-- AI 图纸导入追溯：记录上传与 Dify 工作流的总识别耗时（毫秒）。
alter table bom_ai_import_trace
    add column recognition_duration_ms bigint null comment '识别总耗时（毫秒）' after recognized_bom_count;
