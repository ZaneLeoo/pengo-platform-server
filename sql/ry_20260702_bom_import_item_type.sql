-- BOM OCR 导入草稿明细增加子件类型/属性字段

alter table bom_import_item
    add column item_type varchar(64) default null comment '子件类型/属性' after unit;
