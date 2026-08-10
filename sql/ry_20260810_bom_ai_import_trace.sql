-- BOM AI 图纸导入追溯：保留原始图纸元数据、Dify 原始输出与最终生成的 BOM 关联。
create table if not exists bom_ai_import_trace (
    id bigint not null auto_increment comment '主键',
    import_no varchar(48) not null comment '导入批次号',
    status varchar(24) not null comment '状态：RECOGNIZING、RECOGNIZED、FAILED、IMPORTED',
    file_count int not null default 0 comment '原始图纸数量',
    source_files longtext not null comment '原始图纸文件元数据 JSON',
    raw_dify_outputs longtext null comment 'Dify 原始工作流输出 JSON',
    preview_payload longtext null comment '识别并匹配后的预览数据 JSON',
    error_message text null comment '识别失败原因',
    imported_bom_master_ids varchar(1000) null comment '生成的 BOM 主数据 ID 列表 JSON',
    imported_bom_version_ids varchar(1000) null comment '生成的 BOM 版本 ID 列表 JSON',
    recognized_time datetime null comment '识别完成时间',
    confirmed_time datetime null comment '确认导入时间',
    create_by varchar(64) default '' comment '创建者',
    create_time datetime null comment '创建时间',
    update_by varchar(64) default '' comment '更新者',
    update_time datetime null comment '更新时间',
    remark varchar(500) default null comment '备注',
    primary key (id),
    unique key uk_bom_ai_import_trace_no (import_no),
    key idx_bom_ai_import_trace_status_time (status, create_time)
) engine=innodb comment='BOM AI 图纸导入追溯记录';
