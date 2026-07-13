-- 从现有真实采购订单初始化供应商报价历史。
-- SUP001 的采购订单包含税率，可作为当前有效报价；SUP005 已停用，仅保留为过期历史。

insert into purchase_supplier_quote(
    quote_code, supplier_id, supplier_code, supplier_name, quote_date, currency,
    tax_included, status, effective_date, expire_date, source_type, source_reference,
    remark, create_by, create_time)
select 'Q-HIST-AI-PO-20260712', s.id, s.supplier_code, s.supplier_name,
       '2026-07-12', 'CNY', 'Y', 'APPROVED', '2026-07-12', null,
       'MANUAL', 'AI-PO-20260712-920849B08D0DD94B392DF8CD',
       '从历史采购订单初始化，单价和税率沿用原订单', 'admin', now()
from supplier s
where s.supplier_code = 'SUP001'
  and not exists(select 1 from purchase_supplier_quote where quote_code = 'Q-HIST-AI-PO-20260712');

insert into purchase_supplier_quote_line(
    quote_id, line_no, material_id, material_code, material_name, spec, unit,
    min_order_quantity, min_quantity, max_quantity, unit_price, tax_rate,
    lead_time_days, remark, create_by, create_time)
select q.id, 1, m.material_id, m.material_code, m.material_name, m.spec, m.unit,
       1, 0, null, 20.000000, 13.0000, 0,
       '来源订单 AI-PO-20260712-920849B08D0DD94B392DF8CD', 'admin', now()
from purchase_supplier_quote q
join material m on m.material_code = 'lamppost'
where q.quote_code = 'Q-HIST-AI-PO-20260712'
  and not exists(select 1 from purchase_supplier_quote_line where quote_id = q.id and line_no = 1);

insert into purchase_supplier_quote(
    quote_code, supplier_id, supplier_code, supplier_name, quote_date, currency,
    tax_included, status, effective_date, expire_date, source_type, source_reference,
    remark, create_by, create_time)
select 'Q-HIST-CG002', s.id, s.supplier_code, s.supplier_name,
       '2026-07-12', 'CNY', 'N', 'EXPIRED', '2026-07-12', '2026-07-12',
       'MANUAL', 'CG002',
       '历史采购订单报价；供应商已停用，原订单未记录税率，仅保留历史参考', 'admin', now()
from supplier s
where s.supplier_code = 'SUP005'
  and not exists(select 1 from purchase_supplier_quote where quote_code = 'Q-HIST-CG002');

insert into purchase_supplier_quote_line(
    quote_id, line_no, material_id, material_code, material_name, spec, unit,
    min_order_quantity, min_quantity, max_quantity, unit_price, tax_rate,
    lead_time_days, remark, create_by, create_time)
select q.id, 1, m.material_id, m.material_code, m.material_name, m.spec, m.unit,
       1, 0, null, 23.000000, 0.0000, 0,
       '来源订单 CG002；原订单未记录税率', 'admin', now()
from purchase_supplier_quote q
join material m on m.material_code = 'lamppost'
where q.quote_code = 'Q-HIST-CG002'
  and not exists(select 1 from purchase_supplier_quote_line where quote_id = q.id and line_no = 1);

insert into purchase_supplier_quote_line(
    quote_id, line_no, material_id, material_code, material_name, spec, unit,
    min_order_quantity, min_quantity, max_quantity, unit_price, tax_rate,
    lead_time_days, remark, create_by, create_time)
select q.id, 2, m.material_id, m.material_code, m.material_name, m.spec, m.unit,
       1, 0, null, 50.000000, 0.0000, 0,
       '来源订单 CG002；原订单未记录税率', 'admin', now()
from purchase_supplier_quote q
join material m on m.material_code = 'lamp-base'
where q.quote_code = 'Q-HIST-CG002'
  and not exists(select 1 from purchase_supplier_quote_line where quote_id = q.id and line_no = 2);
