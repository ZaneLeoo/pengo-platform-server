-- BOM OCR 导入草稿表

drop table if exists bom_import_item;
drop table if exists bom_import_task;

create table bom_import_task (
    id                         bigint(20)      not null auto_increment comment '导入任务ID',
    file_name                  varchar(255)    default null            comment '原始文件名称',
    file_url                   varchar(500)    default null            comment '原始文件访问地址',
    file_type                  varchar(32)     default null            comment '原始文件类型',
    status                     varchar(32)     not null                comment '任务状态',
    title                      varchar(255)    default null            comment '图纸标题',
    parent_name_candidate      varchar(255)    default null            comment '母件名称候选',
    parent_code_candidate      varchar(128)    default null            comment '母件编码候选',
    product_model              varchar(128)    default null            comment '产品型号/规格型号',
    drawing_no                 varchar(128)    default null            comment '图纸编号/部件图号/装配图号',
    revision                   varchar(64)     default null            comment '版本/版次',
    base_qty_candidate         decimal(18,6)   default null            comment '母件基准数量/底数候选',
    total_rows                 int(11)         default null            comment '图纸声明的总明细行数',
    unit_weight                decimal(18,6)   default null            comment '单件重量',
    raw_result_json            longtext                                comment 'Dify原始输出JSON',
    extra_fields_json          text                                    comment '标题区扩展字段JSON',
    issues_json                text                                    comment '问题列表JSON',
    error_message              varchar(1000)   default null            comment '错误信息',
    create_by                  varchar(64)     default ''              comment '创建者',
    create_time                datetime                                comment '创建时间',
    update_by                  varchar(64)     default ''              comment '更新者',
    update_time                datetime                                comment '更新时间',
    remark                     varchar(500)    default null            comment '备注',
    primary key (id),
    key idx_bom_import_status_time (status, create_time),
    key idx_bom_import_parent_code (parent_code_candidate),
    key idx_bom_import_drawing_no (drawing_no)
) engine=innodb comment='BOM OCR导入任务表';

create table bom_import_item (
    id                         bigint(20)      not null auto_increment comment '草稿明细ID',
    import_id                  bigint(20)      not null                comment '导入任务ID',
    line_no                    int(11)         default null            comment '明细行序号',
    component_code_candidate   varchar(128)    default null            comment '子件编码候选',
    drawing_no                 varchar(128)    default null            comment '子件图号',
    item_name                  varchar(255)    default null            comment '子件名称',
    quantity                   decimal(18,6)   default null            comment '子件数量候选',
    spec                       varchar(1000)   default null            comment '规格型号/材料/标准号',
    unit                       varchar(64)     default null            comment '单位',
    item_type                  varchar(64)     default null            comment '子件类型/属性',
    unit_weight                decimal(18,6)   default null            comment '单件重量',
    total_weight               decimal(18,6)   default null            comment '总重量',
    remark                     varchar(500)    default null            comment '备注',
    raw_text                   varchar(2000)   default null            comment '原始行文本',
    confidence                 decimal(8,4)    default null            comment '模型置信度',
    matched_material_id        bigint(20)      default null            comment '匹配物料ID',
    matched_material_code      varchar(64)     default null            comment '匹配物料编码',
    matched_material_name      varchar(100)    default null            comment '匹配物料名称',
    match_status               varchar(32)     default 'missing'       comment '物料匹配状态',
    risk_level                 varchar(32)     default 'ok'            comment '行风险等级',
    issue_message              varchar(1000)   default null            comment '行级问题描述',
    create_by                  varchar(64)     default ''              comment '创建者',
    create_time                datetime                                comment '创建时间',
    update_by                  varchar(64)     default ''              comment '更新者',
    update_time                datetime                                comment '更新时间',
    primary key (id),
    key idx_bom_import_item_import (import_id),
    key idx_bom_import_item_line (import_id, line_no),
    key idx_bom_import_item_component (component_code_candidate),
    key idx_bom_import_item_risk (risk_level)
) engine=innodb comment='BOM OCR导入草稿明细表';

select @mes_base_id := menu_id from sys_menu where menu_name = '基础资料' limit 1;

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM图纸导入', @mes_base_id, 9, 'bomImport', 'mes/base/bomImport/index', '', '', 1, 0, 'C', '0', '0', 'mes:bomImport:list', 'upload', 'admin', sysdate(), 'BOM OCR导入菜单'
where @mes_base_id is not null and not exists (select 1 from sys_menu where perms = 'mes:bomImport:list');

select @bom_import_menu_id := menu_id from sys_menu where perms = 'mes:bomImport:list' limit 1;
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM图纸导入查询', @bom_import_menu_id, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:bomImport:query', '#', 'admin', sysdate(), '' where @bom_import_menu_id is not null and not exists (select 1 from sys_menu where perms = 'mes:bomImport:query');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM图纸导入新增', @bom_import_menu_id, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:bomImport:add', '#', 'admin', sysdate(), '' where @bom_import_menu_id is not null and not exists (select 1 from sys_menu where perms = 'mes:bomImport:add');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM图纸导入修改', @bom_import_menu_id, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:bomImport:edit', '#', 'admin', sysdate(), '' where @bom_import_menu_id is not null and not exists (select 1 from sys_menu where perms = 'mes:bomImport:edit');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM图纸导入删除', @bom_import_menu_id, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:bomImport:remove', '#', 'admin', sysdate(), '' where @bom_import_menu_id is not null and not exists (select 1 from sys_menu where perms = 'mes:bomImport:remove');
