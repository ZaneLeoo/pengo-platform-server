-- MES基础资料：物料分类、物料主数据

drop table if exists material;
drop table if exists material_category;

create table material_category (
    category_id     bigint(20)      not null auto_increment    comment '分类ID',
    parent_id       bigint(20)      default 0                  comment '父分类ID',
    ancestors       varchar(500)    default ''                 comment '祖级列表',
    category_code   varchar(64)     not null                   comment '分类编码',
    category_name   varchar(100)    not null                   comment '分类名称',
    order_num       int(4)          default 0                  comment '显示顺序',
    status          char(1)         default '0'                comment '状态（0正常 1停用）',
    create_by       varchar(64)     default ''                 comment '创建者',
    create_time     datetime                                   comment '创建时间',
    update_by       varchar(64)     default ''                 comment '更新者',
    update_time     datetime                                   comment '更新时间',
    remark          varchar(500)    default null               comment '备注',
    primary key (category_id),
    unique key uk_material_category_code (category_code),
    key idx_material_category_parent (parent_id)
) engine=innodb auto_increment=100 comment = '物料分类表';

create table material (
    material_id        bigint(20)      not null auto_increment    comment '物料ID',
    material_code      varchar(64)     not null                   comment '物料编码',
    material_name      varchar(100)    not null                   comment '物料名称',
    material_type      varchar(32)     not null                   comment '物料类型（RAW/SEMI_FINISHED/FINISHED/AUXILIARY/PACKAGE）',
    category_id        bigint(20)      default null               comment '分类ID',
    spec               varchar(120)    default null               comment '规格',
    model              varchar(120)    default null               comment '型号',
    unit               varchar(32)     not null                   comment '主单位',
    drawing_no         varchar(64)     default null               comment '图号',
    material_version   varchar(32)     default null               comment '物料版本',
    source_type        varchar(32)     not null                   comment '来源类型（MAKE/PURCHASE/OUTSOURCE）',
    lot_control_flag   char(1)         default 'N'                comment '是否批次管理（Y是 N否）',
    sn_control_flag    char(1)         default 'N'                comment '是否序列号管理（Y是 N否）',
    inspection_flag    char(1)         default 'N'                comment '是否需要检验（Y是 N否）',
    safe_stock         decimal(18,6)   default 0.000000           comment '安全库存',
    default_bom_id     bigint(20)      default null               comment '默认BOM ID',
    default_route_id   bigint(20)      default null               comment '默认工艺路线ID',
    status             char(1)         default '0'                comment '状态（0正常 1停用）',
    create_by          varchar(64)     default ''                 comment '创建者',
    create_time        datetime                                   comment '创建时间',
    update_by          varchar(64)     default ''                 comment '更新者',
    update_time        datetime                                   comment '更新时间',
    remark             varchar(500)    default null               comment '备注',
    primary key (material_id),
    unique key uk_material_code (material_code),
    key idx_material_category (category_id),
    key idx_material_type (material_type)
) engine=innodb auto_increment=100 comment = '物料主数据表';

-- 菜单：MES / 基础资料 / 物料分类、物料主数据
select @mes_parent_id := menu_id from sys_menu where menu_name = 'MES' limit 1;
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'MES', 0, 6, 'mes', null, '', '', 1, 0, 'M', '0', '0', '', 'tree', 'admin', sysdate(), 'MES目录'
where @mes_parent_id is null;
select @mes_parent_id := menu_id from sys_menu where menu_name = 'MES' limit 1;

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '基础资料', @mes_parent_id, 1, 'base', null, '', '', 1, 0, 'M', '0', '0', '', 'component', 'admin', sysdate(), 'MES基础资料目录'
where not exists (select 1 from sys_menu where menu_name = '基础资料' and parent_id = @mes_parent_id);
select @mes_base_id := menu_id from sys_menu where menu_name = '基础资料' and parent_id = @mes_parent_id limit 1;

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '物料分类', @mes_base_id, 1, 'materialCategory', 'mes/base/materialCategory/index', '', '', 1, 0, 'C', '0', '0', 'mes:materialCategory:list', 'tree-table', 'admin', sysdate(), '物料分类菜单'
where not exists (select 1 from sys_menu where perms = 'mes:materialCategory:list');
select @material_category_menu_id := menu_id from sys_menu where perms = 'mes:materialCategory:list' limit 1;
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '物料分类查询', @material_category_menu_id, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:materialCategory:query', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:materialCategory:query');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '物料分类新增', @material_category_menu_id, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:materialCategory:add', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:materialCategory:add');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '物料分类修改', @material_category_menu_id, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:materialCategory:edit', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:materialCategory:edit');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '物料分类删除', @material_category_menu_id, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:materialCategory:remove', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:materialCategory:remove');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '物料分类导出', @material_category_menu_id, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:materialCategory:export', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:materialCategory:export');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '物料主数据', @mes_base_id, 2, 'material', 'mes/base/material/index', '', '', 1, 0, 'C', '0', '0', 'mes:material:list', 'dict', 'admin', sysdate(), '物料主数据菜单'
where not exists (select 1 from sys_menu where perms = 'mes:material:list');
select @material_menu_id := menu_id from sys_menu where perms = 'mes:material:list' limit 1;
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '物料查询', @material_menu_id, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:material:query', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:material:query');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '物料新增', @material_menu_id, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:material:add', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:material:add');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '物料修改', @material_menu_id, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:material:edit', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:material:edit');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '物料删除', @material_menu_id, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:material:remove', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:material:remove');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select '物料导出', @material_menu_id, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:material:export', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:material:export');

drop table if exists bom_item;
drop table if exists bom_version;
drop table if exists bom_master;

create table bom_master (
    id                 bigint(20)    not null auto_increment  comment '主键',
    bom_code           varchar(64)   not null                 comment 'BOM编码',
    parent_item_id     bigint(20)    default null             comment '母件物料ID',
    parent_item_code   varchar(64)   not null                 comment '母件编码',
    parent_item_name   varchar(255)  not null                 comment '母件名称',
    parent_item_spec   varchar(500)  default null             comment '母件规格',
    parent_item_unit   varchar(64)   default null             comment '母件单位',
    bom_type           varchar(32)   not null                 comment 'BOM类型',
    status             varchar(32)   not null                 comment '状态',
    source_system      varchar(32)   default 'MANUAL'         comment '来源系统',
    source_id          varchar(255)  default null             comment '外部ID',
    create_by          varchar(64)   default ''               comment '创建者',
    create_time        datetime                               comment '创建时间',
    update_by          varchar(64)   default ''               comment '更新者',
    update_time        datetime                               comment '更新时间',
    remark             varchar(500)  default null             comment '备注',
    primary key (id),
    unique key uk_bom_master_code (bom_code),
    key idx_bom_master_parent_code (parent_item_code)
) engine=innodb comment='BOM主表';

create table bom_version (
    id                         bigint(20)     not null auto_increment  comment '主键',
    bom_master_id              bigint(20)     not null                 comment 'BOM主表ID',
    version_code               varchar(64)    not null                 comment '版本号',
    version_name               varchar(255)   default null             comment '版本名称',
    version_desc               varchar(500)   default null             comment '版本说明',
    base_qty                   decimal(18,6)  not null default 1       comment 'BOM基准数量',
    usage_type                 varchar(32)    not null default 'GENERAL' comment '用途',
    effective_date             date           default null             comment '生效日期',
    expire_date                date           default null             comment '失效日期',
    status                     varchar(32)    not null default 'DRAFT' comment '版本状态',
    approve_status             varchar(32)    not null default 'PENDING' comment '审批状态',
    default_flag               tinyint        not null default 0       comment '是否默认版本',
    default_routing_code       varchar(64)    default null             comment '默认工艺路线编码',
    default_routing_name       varchar(255)   default null             comment '默认工艺路线名称',
    default_routing_version_id bigint(20)     default null             comment '默认工艺路线版本ID',
    source_system              varchar(32)    default 'MANUAL'         comment '来源系统',
    source_id                  varchar(255)   default null             comment '外部版本ID',
    check_by                   varchar(64)    default null             comment '审核人',
    check_time                 datetime                                comment '审核时间',
    close_by                   varchar(64)    default null             comment '停用人',
    close_time                 datetime                                comment '停用时间',
    create_by                  varchar(64)    default ''               comment '创建者',
    create_time                datetime                                comment '创建时间',
    update_by                  varchar(64)    default ''               comment '更新者',
    update_time                datetime                                comment '更新时间',
    remark                     varchar(500)   default null             comment '备注',
    primary key (id),
    unique key uk_bom_version_code (bom_master_id, version_code),
    key idx_bom_version_master (bom_master_id)
) engine=innodb comment='BOM版本表';

create table bom_item (
    id                    bigint(20)     not null auto_increment comment '主键',
    bom_version_id        bigint(20)     not null                comment 'BOM版本ID',
    line_no               int            not null                comment '行号',
    parent_item_code      varchar(64)    default null            comment '母件编码',
    component_item_id     bigint(20)     default null            comment '子件物料ID',
    component_item_code   varchar(64)    not null                comment '子件编码',
    component_item_name   varchar(255)   not null                comment '子件名称',
    component_item_spec   varchar(500)   default null            comment '子件规格',
    component_item_unit   varchar(64)    default null            comment '子件单位',
    component_attribute   varchar(64)    default null            comment '子件属性',
    component_qty         decimal(18,6)  not null                comment '子件用量',
    fixed_loss_qty        decimal(18,6)  default 0               comment '固定损耗数量',
    change_loss_rate      decimal(18,6)  default 0               comment '变动损耗率%',
    length                decimal(18,6)  default null            comment '长度',
    width                 decimal(18,6)  default null            comment '宽度',
    supply_type           varchar(32)    not null default 'PUSH' comment '发料/扣料方式',
    is_virtual            tinyint        not null default 0      comment '是否虚拟件',
    mrp_expand_flag       tinyint        not null default 1      comment '是否参与MRP展开',
    source_system         varchar(32)    default 'MANUAL'        comment '来源系统',
    create_by             varchar(64)    default ''              comment '创建者',
    create_time           datetime                               comment '创建时间',
    update_by             varchar(64)    default ''              comment '更新者',
    update_time           datetime                               comment '更新时间',
    remark                varchar(500)   default null            comment '备注',
    primary key (id),
    unique key uk_bom_item_version_line (bom_version_id, line_no),
    key idx_bom_item_version (bom_version_id),
    key idx_bom_item_component (component_item_code)
) engine=innodb comment='BOM子件明细表';

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM管理', @mes_base_id, 3, 'bom', 'mes/base/bom/index', '', 'BomMaster', 1, 0, 'C', '0', '0', 'mes:bomMaster:list', 'tree', 'admin', sysdate(), 'BOM管理菜单'
where not exists (select 1 from sys_menu where perms = 'mes:bomMaster:list');
select @bom_menu_id := menu_id from sys_menu where perms = 'mes:bomMaster:list' limit 1;

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM详情', @mes_base_id, 4, 'bom/:id', 'mes/base/bom/detail', '', 'BomDetail', 1, 0, 'C', '1', '0', 'mes:bomMaster:detail', '#', 'admin', sysdate(), 'BOM详情隐藏路由'
where not exists (select 1 from sys_menu where route_name = 'BomDetail');

insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM查询', @bom_menu_id, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:bomMaster:query', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:bomMaster:query');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM新增', @bom_menu_id, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:bomMaster:add', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:bomMaster:add');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM修改', @bom_menu_id, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:bomMaster:edit', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:bomMaster:edit');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM删除', @bom_menu_id, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:bomMaster:remove', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:bomMaster:remove');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM导出', @bom_menu_id, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:bomMaster:export', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:bomMaster:export');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM版本查询', @bom_menu_id, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:bomVersion:list', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:bomVersion:list');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM版本详情', @bom_menu_id, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:bomVersion:query', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:bomVersion:query');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM版本新增', @bom_menu_id, 8, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:bomVersion:add', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:bomVersion:add');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM版本修改', @bom_menu_id, 9, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:bomVersion:edit', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:bomVersion:edit');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM版本删除', @bom_menu_id, 10, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:bomVersion:remove', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:bomVersion:remove');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM明细查询', @bom_menu_id, 11, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:bomItem:list', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:bomItem:list');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM明细详情', @bom_menu_id, 12, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:bomItem:query', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:bomItem:query');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM明细新增', @bom_menu_id, 13, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:bomItem:add', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:bomItem:add');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM明细修改', @bom_menu_id, 14, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:bomItem:edit', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:bomItem:edit');
insert into sys_menu(menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
select 'BOM明细删除', @bom_menu_id, 15, '', '', '', '', 1, 0, 'F', '0', '0', 'mes:bomItem:remove', '#', 'admin', sysdate(), '' where not exists (select 1 from sys_menu where perms = 'mes:bomItem:remove');
