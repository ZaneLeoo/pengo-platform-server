-- BOM AI 导入生命周期、重复文件识别与过期信息。
alter table bom_ai_import_trace
    add column source_fingerprint char(64) null comment '本批原始文件哈希集合指纹' after file_count,
    add column cancelled_time datetime null comment '主动放弃时间' after confirmed_time,
    add column expired_time datetime null comment '过期时间' after cancelled_time,
    add key idx_bom_ai_import_trace_fingerprint_status (source_fingerprint, status);
