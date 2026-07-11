-- 送货单、入库单改为明细行级来源参照。
-- 当前开发库尚无业务单据数据，可直接执行；若已有数据，应先完成数据迁移后再执行。

ALTER TABLE purchase_order_line
  ADD COLUMN inbound_quantity DECIMAL(18,6) NOT NULL DEFAULT 0 COMMENT '已入库数量' AFTER qualified_quantity;

ALTER TABLE purchase_receipt
  DROP COLUMN order_id,
  DROP COLUMN order_code;

ALTER TABLE purchase_receipt_line
  CHANGE COLUMN order_line_id source_order_line_id BIGINT NOT NULL COMMENT '来源采购订单明细ID',
  ADD COLUMN source_order_id BIGINT NOT NULL COMMENT '来源采购订单ID' AFTER line_no,
  ADD COLUMN source_order_code VARCHAR(64) NOT NULL COMMENT '来源采购订单编号快照' AFTER source_order_id,
  ADD COLUMN source_order_line_no INT NOT NULL COMMENT '来源采购订单明细行号快照' AFTER source_order_line_id,
  ADD COLUMN inbound_quantity DECIMAL(18,6) NOT NULL DEFAULT 0 COMMENT '已入库数量' AFTER pending_quantity;

ALTER TABLE purchase_inbound
  DROP COLUMN receipt_id,
  DROP COLUMN receipt_code;

ALTER TABLE purchase_inbound_line
  CHANGE COLUMN receipt_line_id source_receipt_line_id BIGINT NOT NULL COMMENT '来源到货单明细ID',
  ADD COLUMN source_receipt_id BIGINT NOT NULL COMMENT '来源到货单ID' AFTER line_no,
  ADD COLUMN source_receipt_code VARCHAR(64) NOT NULL COMMENT '来源到货单编号快照' AFTER source_receipt_id,
  ADD COLUMN source_receipt_line_no INT NOT NULL COMMENT '来源到货单明细行号快照' AFTER source_receipt_line_id,
  ADD COLUMN source_order_id BIGINT NOT NULL COMMENT '来源采购订单ID' AFTER source_receipt_line_no,
  ADD COLUMN source_order_code VARCHAR(64) NOT NULL COMMENT '来源采购订单编号快照' AFTER source_order_id,
  ADD COLUMN source_order_line_id BIGINT NOT NULL COMMENT '来源采购订单明细ID' AFTER source_order_code,
  ADD COLUMN source_order_line_no INT NOT NULL COMMENT '来源采购订单明细行号快照' AFTER source_order_line_id;
