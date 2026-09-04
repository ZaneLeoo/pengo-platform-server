-- BOM 图纸附件。附件文件由通用上传接口保存，本表维护 BOM 与文件的业务关系。
create table if not exists bom_drawing_attachment (
    attachment_id bigint not null auto_increment comment '附件主键ID',
    bom_master_id bigint not null comment '所属BOM主表ID',
    file_name varchar(255) not null comment '用户上传时的原始文件名',
    file_url varchar(1000) not null comment '通用上传接口返回的文件资源地址',
    file_size bigint null comment '文件大小（字节）',
    file_ext varchar(32) null comment '小写文件扩展名',
    mime_type varchar(128) null comment '文件MIME类型',
    description varchar(500) null comment '图纸附件说明',
    upload_by varchar(64) not null default '' comment '上传人账号',
    upload_time datetime not null default current_timestamp comment '上传时间',
    primary key (attachment_id),
    key idx_bom_drawing_attachment_master (bom_master_id),
    constraint fk_bom_drawing_attachment_master
        foreign key (bom_master_id) references bom_master(id) on delete cascade
) engine=InnoDB comment='BOM图纸附件';
