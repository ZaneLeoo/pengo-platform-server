-- BOM子件：增加引用BOM版本字段
ALTER TABLE bom_item ADD COLUMN component_bom_version_id bigint(20) DEFAULT NULL COMMENT '子件引用的BOM版本ID，为空则取默认版本';
