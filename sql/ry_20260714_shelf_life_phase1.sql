-- 保质期管理第一阶段：物料配置、到货/入库继承、批次库存效期

alter table material
    add column shelf_life_control_flag char(1) not null default 'N'
        comment '是否启用保质期管理（Y是 N否）' after lot_control_flag,
    add column shelf_life_days int null
        comment '默认保质期天数' after shelf_life_control_flag,
    add column expiry_warning_days int null
        comment '到期预警天数' after shelf_life_days;

alter table purchase_inbound_line
    add column production_date date null
        comment '生产日期' after lot_no,
    add column expiry_date date null
        comment '有效期' after production_date;

alter table inventory_balance
    add column production_date date null
        comment '生产日期' after lot_no,
    add column expiry_date date null
        comment '有效期' after production_date;

create index idx_inventory_balance_expiry_date on inventory_balance (expiry_date);
