-- 供应商报价与 AI 采购选价基础表
-- 报价单使用 BaseEntity 审计字段，不使用软删除；已审核报价通过状态和有效期参与推荐。

create table if not exists purchase_supplier_quote (
    id bigint not null auto_increment comment '主键',
    quote_code varchar(64) not null comment '报价单号',
    supplier_id bigint not null comment '供应商ID',
    supplier_code varchar(64) not null comment '供应商编码快照',
    supplier_name varchar(255) not null comment '供应商名称快照',
    quote_date date not null comment '报价日期',
    currency varchar(16) not null default 'CNY' comment '币种',
    tax_included char(1) not null default 'Y' comment '是否含税 Y是 N否',
    status varchar(32) not null default 'DRAFT' comment '状态 DRAFT草稿 APPROVED已审核 EXPIRED已过期 CANCELLED已作废',
    effective_date date not null comment '生效日期',
    expire_date date null comment '失效日期',
    source_type varchar(32) not null default 'MANUAL' comment '来源 MANUAL手工 EXCEL导入 API同步',
    source_reference varchar(255) null comment '外部报价单号或来源说明',
    approved_by varchar(64) null comment '审核人',
    approved_time datetime null comment '审核时间',
    remark varchar(500) null comment '备注',
    create_by varchar(64) null comment '创建人',
    create_time datetime null comment '创建时间',
    update_by varchar(64) null comment '更新人',
    update_time datetime null comment '更新时间',
    primary key (id),
    unique key uk_purchase_supplier_quote_code (quote_code),
    key idx_purchase_supplier_quote_supplier (supplier_id),
    key idx_purchase_supplier_quote_status_date (status, effective_date, expire_date)
) engine=InnoDB default charset=utf8mb4 comment='供应商报价主表';

create table if not exists purchase_supplier_quote_line (
    id bigint not null auto_increment comment '主键',
    quote_id bigint not null comment '报价主表ID',
    line_no int not null comment '行号',
    material_id bigint not null comment '物料ID',
    material_code varchar(64) not null comment '物料编码快照',
    material_name varchar(255) not null comment '物料名称快照',
    spec varchar(500) null comment '规格快照',
    unit varchar(64) not null comment '单位快照',
    min_order_quantity decimal(18,6) not null comment '最小起订量',
    min_quantity decimal(18,6) null comment '阶梯价格起始数量',
    max_quantity decimal(18,6) null comment '阶梯价格结束数量，空表示无上限',
    unit_price decimal(18,6) not null comment '报价单价',
    tax_rate decimal(8,4) not null default 0 comment '税率百分比',
    lead_time_days int not null default 0 comment '交货周期天数',
    remark varchar(500) null comment '备注',
    create_by varchar(64) null comment '创建人',
    create_time datetime null comment '创建时间',
    update_by varchar(64) null comment '更新人',
    update_time datetime null comment '更新时间',
    primary key (id),
    unique key uk_purchase_supplier_quote_line (quote_id, line_no),
    key idx_purchase_supplier_quote_line_material (material_code, quote_id),
    constraint fk_purchase_supplier_quote_line_quote foreign key (quote_id)
        references purchase_supplier_quote (id)
) engine=InnoDB default charset=utf8mb4 comment='供应商报价明细';

-- 采购订单明细记录报价来源，便于审计和价格追溯。
set @quote_source_column_exists := (
    select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'purchase_order_line' and column_name = 'quote_id'
);
set @quote_source_sql := if(@quote_source_column_exists = 0,
    'alter table purchase_order_line add column quote_id bigint null comment ''报价主表ID'' after planned_date',
    'select 1');
prepare quote_source_stmt from @quote_source_sql;
execute quote_source_stmt;
deallocate prepare quote_source_stmt;

set @quote_line_source_column_exists := (
    select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'purchase_order_line' and column_name = 'quote_line_id'
);
set @quote_line_source_sql := if(@quote_line_source_column_exists = 0,
    'alter table purchase_order_line add column quote_line_id bigint null comment ''报价明细ID'' after quote_id',
    'select 1');
prepare quote_line_source_stmt from @quote_line_source_sql;
execute quote_line_source_stmt;
deallocate prepare quote_line_source_stmt;

set @price_source_column_exists := (
    select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'purchase_order_line' and column_name = 'price_source'
);
set @price_source_sql := if(@price_source_column_exists = 0,
    'alter table purchase_order_line add column price_source varchar(32) not null default ''MANUAL'' comment ''价格来源 MANUAL手工 QUOTE报价'' after quote_line_id',
    'select 1');
prepare price_source_stmt from @price_source_sql;
execute price_source_stmt;
deallocate prepare price_source_stmt;
