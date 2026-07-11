-- 审核、质检、库存事务字段。
ALTER TABLE purchase_order ADD COLUMN approved_by VARCHAR(64) DEFAULT NULL COMMENT '审核人' AFTER status, ADD COLUMN approved_time DATETIME DEFAULT NULL COMMENT '审核时间' AFTER approved_by;
ALTER TABLE purchase_receipt ADD COLUMN approved_by VARCHAR(64) DEFAULT NULL COMMENT '审核人' AFTER inspection_status, ADD COLUMN approved_time DATETIME DEFAULT NULL COMMENT '审核时间' AFTER approved_by, ADD COLUMN inspection_by VARCHAR(64) DEFAULT NULL COMMENT '质检人' AFTER approved_time, ADD COLUMN inspection_time DATETIME DEFAULT NULL COMMENT '质检时间' AFTER inspection_by;
ALTER TABLE purchase_inbound ADD COLUMN approved_by VARCHAR(64) DEFAULT NULL COMMENT '审核人' AFTER status, ADD COLUMN approved_time DATETIME DEFAULT NULL COMMENT '审核时间' AFTER approved_by;

ALTER TABLE inventory_balance MODIFY location_code VARCHAR(64) NOT NULL DEFAULT '' COMMENT '库位编码', MODIFY lot_no VARCHAR(128) NOT NULL DEFAULT '' COMMENT '批次号';

CREATE TABLE IF NOT EXISTS inventory_transaction (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  transaction_type VARCHAR(32) NOT NULL COMMENT '事务类型：INBOUND/INBOUND_REVERSE',
  business_id BIGINT NOT NULL COMMENT '业务单据ID',
  business_code VARCHAR(64) NOT NULL COMMENT '业务单据编号快照',
  business_line_id BIGINT NOT NULL COMMENT '业务单据明细ID',
  material_id BIGINT NOT NULL COMMENT '物料ID',
  material_code VARCHAR(64) NOT NULL COMMENT '物料编码快照',
  material_name VARCHAR(255) NOT NULL COMMENT '物料名称快照',
  warehouse_code VARCHAR(64) NOT NULL COMMENT '仓库编码',
  location_code VARCHAR(64) NOT NULL DEFAULT '' COMMENT '库位编码',
  lot_no VARCHAR(128) NOT NULL DEFAULT '' COMMENT '批次号',
  unit VARCHAR(64) NOT NULL COMMENT '计量单位',
  quantity DECIMAL(18,6) NOT NULL COMMENT '变动数量，入库为正、弃审为负',
  source_receipt_line_id BIGINT DEFAULT NULL COMMENT '来源送货单明细ID',
  source_order_line_id BIGINT DEFAULT NULL COMMENT '来源采购订单明细ID',
  create_by VARCHAR(64) DEFAULT NULL COMMENT '创建人',
  create_time DATETIME DEFAULT NULL COMMENT '创建时间',
  remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_inventory_transaction_business (business_id, business_line_id),
  KEY idx_inventory_transaction_material (material_id, warehouse_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存事务流水';
