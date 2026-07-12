-- ============================================
-- 基础档案：供应商、仓库、库位
-- ============================================

-- 1. 供应商档案
DROP TABLE IF EXISTS base_supplier;
CREATE TABLE base_supplier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    supplier_code VARCHAR(50) NOT NULL COMMENT '供应商编码',
    supplier_name VARCHAR(100) NOT NULL COMMENT '供应商名称',
    contact_person VARCHAR(50) DEFAULT NULL COMMENT '联系人',
    contact_phone VARCHAR(30) DEFAULT NULL COMMENT '联系电话',
    address VARCHAR(200) DEFAULT NULL COMMENT '地址',
    currency VARCHAR(10) DEFAULT 'CNY' COMMENT '币种',
    tax_rate DECIMAL(5,2) DEFAULT NULL COMMENT '税率(%)',
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '状态：NORMAL正常/DISABLED停用',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    UNIQUE KEY uk_supplier_code (supplier_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商档案';

-- 2. 仓库档案
DROP TABLE IF EXISTS base_warehouse;
CREATE TABLE base_warehouse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    warehouse_code VARCHAR(50) NOT NULL COMMENT '仓库编码',
    warehouse_name VARCHAR(100) NOT NULL COMMENT '仓库名称',
    address VARCHAR(200) DEFAULT NULL COMMENT '仓库地址',
    manager VARCHAR(50) DEFAULT NULL COMMENT '负责人',
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '状态：NORMAL正常/DISABLED停用',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    UNIQUE KEY uk_warehouse_code (warehouse_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库档案';

-- 3. 库位档案
DROP TABLE IF EXISTS base_location;
CREATE TABLE base_location (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    location_code VARCHAR(50) NOT NULL COMMENT '库位编码',
    location_name VARCHAR(100) NOT NULL COMMENT '库位名称',
    warehouse_id BIGINT NOT NULL COMMENT '所属仓库ID',
    warehouse_code VARCHAR(50) NOT NULL COMMENT '所属仓库编码',
    warehouse_name VARCHAR(100) NOT NULL COMMENT '所属仓库名称',
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '状态：NORMAL正常/DISABLED停用',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建人',
    create_time DATETIME DEFAULT NULL COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新人',
    update_time DATETIME DEFAULT NULL COMMENT '更新时间',
    UNIQUE KEY uk_location_code (location_code),
    KEY idx_warehouse_id (warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库位档案';

-- 4. 字典：档案状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('档案状态', 'mes_archive_status', '0', 'admin', NOW(), '供应商/仓库/库位共用');

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, list_class, create_by, create_time) VALUES
('mes_archive_status', '正常', 'NORMAL', 1, 'success', 'admin', NOW()),
('mes_archive_status', '停用', 'DISABLED', 2, 'danger', 'admin', NOW());

-- 5. 菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
('供应商档案', 2004, 10, 'supplier', 'mes/base/supplier/index', 'C', '0', '0', 'base:supplier:list', 'user', 'admin', NOW()),
('仓库档案',   2004, 11, 'warehouse', 'mes/base/warehouse/index', 'C', '0', '0', 'base:warehouse:list', 'home', 'admin', NOW()),
('库位档案',   2004, 12, 'location', 'mes/base/location/index', 'C', '0', '0', 'base:location:list', 'environment', 'admin', NOW());

SET @supplier_menu_id  = (SELECT menu_id FROM sys_menu WHERE perms = 'base:supplier:list');
SET @warehouse_menu_id = (SELECT menu_id FROM sys_menu WHERE perms = 'base:warehouse:list');
SET @location_menu_id  = (SELECT menu_id FROM sys_menu WHERE perms = 'base:location:list');

INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, perms, create_by, create_time) VALUES
-- 供应商
('供应商查询', @supplier_menu_id, 1, 'F', '0', '0', 'base:supplier:query',  'admin', NOW()),
('供应商新增', @supplier_menu_id, 2, 'F', '0', '0', 'base:supplier:add',    'admin', NOW()),
('供应商修改', @supplier_menu_id, 3, 'F', '0', '0', 'base:supplier:edit',   'admin', NOW()),
('供应商删除', @supplier_menu_id, 4, 'F', '0', '0', 'base:supplier:remove', 'admin', NOW()),
-- 仓库
('仓库查询', @warehouse_menu_id, 1, 'F', '0', '0', 'base:warehouse:query',  'admin', NOW()),
('仓库新增', @warehouse_menu_id, 2, 'F', '0', '0', 'base:warehouse:add',    'admin', NOW()),
('仓库修改', @warehouse_menu_id, 3, 'F', '0', '0', 'base:warehouse:edit',   'admin', NOW()),
('仓库删除', @warehouse_menu_id, 4, 'F', '0', '0', 'base:warehouse:remove', 'admin', NOW()),
-- 库位
('库位查询', @location_menu_id, 1, 'F', '0', '0', 'base:location:query',  'admin', NOW()),
('库位新增', @location_menu_id, 2, 'F', '0', '0', 'base:location:add',    'admin', NOW()),
('库位修改', @location_menu_id, 3, 'F', '0', '0', 'base:location:edit',   'admin', NOW()),
('库位删除', @location_menu_id, 4, 'F', '0', '0', 'base:location:remove', 'admin', NOW());
