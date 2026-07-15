alter table agent_file
    add column direction varchar(10) not null default 'OUTPUT' comment 'INPUT用户上传、OUTPUT Agent生成' after status,
    add column extraction_status varchar(20) default null comment 'NOT_REQUIRED无需提取、READY已提取、FAILED失败' after direction,
    add column extracted_text mediumtext default null comment '文档提取正文' after extraction_status,
    add column extracted_characters int default null comment '提取正文字符数' after extracted_text,
    add column extraction_error varchar(500) default null comment '提取失败原因' after extracted_characters;
