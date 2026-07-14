-- MES 演示数据一键重置
-- 保留系统、权限、Agent 与文件数据，仅重置 MES 基础档案和业务数据。

set foreign_key_checks = 0;
truncate table inventory_transaction;
truncate table inventory_balance;
truncate table purchase_inbound_line;
truncate table purchase_inbound;
truncate table purchase_receipt_line;
truncate table purchase_receipt;
truncate table purchase_order_line;
truncate table purchase_order;
truncate table purchase_supplier_quote_line;
truncate table purchase_supplier_quote;
truncate table bom_item;
truncate table bom_version;
truncate table bom_master;
truncate table bom_import_item;
truncate table bom_import_task;
truncate table material;
truncate table material_category;
truncate table location;
truncate table warehouse;
truncate table supplier;
set foreign_key_checks = 1;

insert into material_category(category_id,parent_id,ancestors,category_code,category_name,order_num,status,create_by,create_time,remark) values
(1,0,'0','FINISHED','成品',1,'0','admin',now(),'最终可销售产品'),
(2,0,'0','SEMI','半成品',2,'0','admin',now(),'生产过程中的装配件'),
(3,0,'0','ELECTRONIC','电子元器件',3,'0','admin',now(),'控制、电源及光源器件'),
(4,0,'0','STRUCTURAL','结构件',4,'0','admin',now(),'灯臂、底座及紧固件'),
(5,0,'0','CONSUMABLE','生产辅料',5,'0','admin',now(),'焊膏、胶黏剂和涂料等效期物料'),
(6,0,'0','PACKAGE','包装材料',6,'0','admin',now(),'纸箱和缓冲包装');

insert into material(material_id,material_code,material_name,material_type,category_id,spec,model,unit,drawing_no,material_version,source_type,lot_control_flag,shelf_life_control_flag,shelf_life_days,expiry_warning_days,sn_control_flag,inspection_flag,safe_stock,status,create_by,create_time,remark) values
(1,'LAMP-001','智能护眼台灯','FINISHED',1,'整灯 24W','DL-2026','台','DL-ASM-001','V1.0','MAKE','Y','N',null,null,'Y','Y',20,'0','admin',now(),'演示产品'),
(2,'SEMI-LAMP-001','台灯主体组件','SEMI',2,'灯臂与底座总成','DL-BODY','套','DL-BODY-001','V1.0','MAKE','Y','N',null,null,'N','Y',30,'0','admin',now(),null),
(3,'PCB-CTRL-001','智能调光控制板','RAW',3,'蓝牙调光 24W','PCB-C24','块',null,'A','PURCHASE','Y','N',null,null,'N','Y',100,'0','admin',now(),null),
(4,'LED-MOD-001','全光谱LED模组','RAW',3,'Ra98 24W','LED-FS24','件',null,'A','PURCHASE','Y','N',null,null,'N','Y',120,'0','admin',now(),null),
(5,'ARM-AL-001','铝合金灯臂','RAW',4,'阳极银 420mm','ARM-420','件','DL-ARM-001','A','PURCHASE','Y','N',null,null,'N','Y',80,'0','admin',now(),null),
(6,'ADAPTER-24V','24V电源适配器','RAW',3,'24V/1.5A','PA-2415','个',null,'A','PURCHASE','Y','N',null,null,'Y','Y',80,'0','admin',now(),null),
(7,'SCREW-M4-12','M4不锈钢螺钉','RAW',4,'M4×12','M4-12','个',null,null,'PURCHASE','N','N',null,null,'N','N',500,'0','admin',now(),null),
(8,'CARTON-LAMP','台灯彩盒','PACKAGE',6,'450×220×80mm','BOX-DL','个',null,'A','PURCHASE','Y','N',null,null,'N','Y',100,'0','admin',now(),null),
(9,'SOLDER-PASTE-01','无铅焊膏','RAW',5,'SAC305 500g','SP-SAC305','罐',null,null,'PURCHASE','Y','Y',180,30,'N','Y',20,'0','admin',now(),'需冷藏并执行先到期先出'),
(10,'ADHESIVE-STRUCT-01','结构胶','RAW',5,'双组份 400ml','ADH-400','支',null,null,'PURCHASE','Y','Y',365,45,'N','Y',20,'0','admin',now(),'灯臂装配用'),
(11,'COATING-PCB-01','PCB三防漆','RAW',5,'透明 1L','CC-1L','桶',null,null,'PURCHASE','Y','Y',365,60,'N','Y',10,'0','admin',now(),'控制板防护用'),
(12,'BATTERY-CR2032','CR2032纽扣电池','RAW',3,'3V','CR2032','粒',null,null,'PURCHASE','Y','Y',730,90,'N','Y',200,'0','admin',now(),'时钟模块备用电源');

insert into supplier(id,supplier_code,supplier_name,contact_person,contact_phone,address,currency,tax_rate,status,remark,create_by,create_time) values
(1,'SUP-ELEC-001','深圳启明电子有限公司','陈工','13800001001','深圳市宝安区','CNY',13,'NORMAL','控制板与电子器件供应商','admin',now()),
(2,'SUP-METAL-001','东莞精工结构件有限公司','李经理','13800001002','东莞市长安镇','CNY',13,'NORMAL','铝合金结构件供应商','admin',now()),
(3,'SUP-CHEM-001','苏州新材科技有限公司','王女士','13800001003','苏州市工业园区','CNY',13,'NORMAL','焊膏、胶黏剂及涂料供应商','admin',now()),
(4,'SUP-CHEM-002','上海优材化工有限公司','赵经理','13800001004','上海市嘉定区','CNY',13,'NORMAL','生产辅料备选供应商','admin',now());

insert into warehouse(id,warehouse_code,warehouse_name,address,manager,status,remark,create_by,create_time) values
(1,'RM','原材料仓','一号厂房东侧','张仓管','NORMAL','原料和生产辅料','admin',now()),
(2,'FG','成品仓','一号厂房西侧','刘仓管','NORMAL','成品存储','admin',now()),
(3,'QC','待检隔离仓','质量中心','周质检','NORMAL','待检与不合格品隔离','admin',now());
insert into location(id,location_code,location_name,warehouse_id,warehouse_code,warehouse_name,status,create_by,create_time) values
(1,'RM-A01','电子料A01',1,'RM','原材料仓','NORMAL','admin',now()),
(2,'RM-B01','结构件B01',1,'RM','原材料仓','NORMAL','admin',now()),
(3,'RM-COLD-01','辅料冷藏区',1,'RM','原材料仓','NORMAL','admin',now()),
(4,'RM-CHEM-01','化学品区',1,'RM','原材料仓','NORMAL','admin',now()),
(5,'FG-A01','成品A01',2,'FG','成品仓','NORMAL','admin',now()),
(6,'QC-A01','待检区A01',3,'QC','待检隔离仓','NORMAL','admin',now());

insert into bom_master(id,bom_code,parent_item_id,parent_item_code,parent_item_name,parent_item_spec,parent_item_unit,bom_type,status,source_system,create_by,create_time,remark) values
(1,'BOM-LAMP-001',1,'LAMP-001','智能护眼台灯','整灯 24W','台','MANUFACTURING','ENABLED','MANUAL','admin',now(),'整灯生产BOM'),
(2,'BOM-SEMI-LAMP-001',2,'SEMI-LAMP-001','台灯主体组件','灯臂与底座总成','套','MANUFACTURING','ENABLED','MANUAL','admin',now(),'主体组件BOM');
insert into bom_version(id,bom_master_id,version_code,version_name,version_desc,base_qty,usage_type,effective_date,status,approve_status,default_flag,source_system,check_by,check_time,create_by,create_time) values
(1,1,'V1.0','量产V1.0','当前量产版本',1,'MASS',date_sub(curdate(),interval 90 day),'EFFECTIVE','APPROVED',1,'MANUAL','admin',now(),'admin',now()),
(2,1,'V1.1','试制V1.1','替换光源模组的试制版本',1,'TRIAL',curdate(),'DRAFT','PENDING',0,'MANUAL',null,null,'admin',now()),
(3,2,'V1.0','主体量产V1.0','当前量产版本',1,'MASS',date_sub(curdate(),interval 90 day),'EFFECTIVE','APPROVED',1,'MANUAL','admin',now(),'admin',now());
insert into bom_item(bom_version_id,line_no,parent_item_code,component_item_id,component_item_code,component_item_name,component_item_spec,component_item_unit,component_qty,fixed_loss_qty,change_loss_rate,supply_type,is_virtual,mrp_expand_flag,source_system,create_by,create_time,component_bom_version_id) values
(1,10,'LAMP-001',2,'SEMI-LAMP-001','台灯主体组件','灯臂与底座总成','套',1,0,0,'PUSH',0,1,'MANUAL','admin',now(),3),
(1,20,'LAMP-001',3,'PCB-CTRL-001','智能调光控制板','蓝牙调光 24W','块',1,0,1,'PUSH',0,1,'MANUAL','admin',now(),null),
(1,30,'LAMP-001',4,'LED-MOD-001','全光谱LED模组','Ra98 24W','件',1,0,1,'PUSH',0,1,'MANUAL','admin',now(),null),
(1,40,'LAMP-001',6,'ADAPTER-24V','24V电源适配器','24V/1.5A','个',1,0,0,'PUSH',0,1,'MANUAL','admin',now(),null),
(1,50,'LAMP-001',8,'CARTON-LAMP','台灯彩盒','450×220×80mm','个',1,0,0,'PUSH',0,1,'MANUAL','admin',now(),null),
(2,10,'LAMP-001',2,'SEMI-LAMP-001','台灯主体组件','灯臂与底座总成','套',1,0,0,'PUSH',0,1,'MANUAL','admin',now(),3),
(2,20,'LAMP-001',3,'PCB-CTRL-001','智能调光控制板','蓝牙调光 24W','块',1,0,1,'PUSH',0,1,'MANUAL','admin',now(),null),
(2,30,'LAMP-001',4,'LED-MOD-001','全光谱LED模组','Ra98 24W','件',1.1,0,1,'PUSH',0,1,'MANUAL','admin',now(),null),
(3,10,'SEMI-LAMP-001',5,'ARM-AL-001','铝合金灯臂','阳极银 420mm','件',1,0,1,'PUSH',0,1,'MANUAL','admin',now(),null),
(3,20,'SEMI-LAMP-001',7,'SCREW-M4-12','M4不锈钢螺钉','M4×12','个',6,1,0,'PUSH',0,1,'MANUAL','admin',now(),null),
(3,30,'SEMI-LAMP-001',10,'ADHESIVE-STRUCT-01','结构胶','双组份 400ml','支',0.05,0,2,'PUSH',0,1,'MANUAL','admin',now(),null);

insert into purchase_supplier_quote(id,quote_code,supplier_id,supplier_code,supplier_name,quote_date,currency,tax_included,status,effective_date,expire_date,source_type,approved_by,approved_time,create_by,create_time,remark) values
(1,'QT-CHEM-001',3,'SUP-CHEM-001','苏州新材科技有限公司',date_sub(curdate(),interval 30 day),'CNY','Y','APPROVED',date_sub(curdate(),interval 30 day),date_add(curdate(),interval 180 day),'MANUAL','admin',now(),'admin',now(),'当前有效报价'),
(2,'QT-CHEM-002',4,'SUP-CHEM-002','上海优材化工有限公司',date_sub(curdate(),interval 25 day),'CNY','Y','APPROVED',date_sub(curdate(),interval 25 day),date_add(curdate(),interval 180 day),'MANUAL','admin',now(),'admin',now(),'备选供应商报价'),
(3,'QT-ELEC-001',1,'SUP-ELEC-001','深圳启明电子有限公司',date_sub(curdate(),interval 20 day),'CNY','Y','APPROVED',date_sub(curdate(),interval 20 day),date_add(curdate(),interval 120 day),'MANUAL','admin',now(),'admin',now(),'电子料报价');
insert into purchase_supplier_quote_line(id,quote_id,line_no,material_id,material_code,material_name,spec,unit,min_order_quantity,min_quantity,max_quantity,unit_price,tax_rate,lead_time_days,create_by,create_time) values
(1,1,10,9,'SOLDER-PASTE-01','无铅焊膏','SAC305 500g','罐',10,10,null,168,13,5,'admin',now()),
(2,1,20,10,'ADHESIVE-STRUCT-01','结构胶','双组份 400ml','支',20,20,null,42,13,7,'admin',now()),
(3,1,30,11,'COATING-PCB-01','PCB三防漆','透明 1L','桶',5,5,null,126,13,6,'admin',now()),
(4,2,10,9,'SOLDER-PASTE-01','无铅焊膏','SAC305 500g','罐',10,10,null,162,13,8,'admin',now()),
(5,2,20,10,'ADHESIVE-STRUCT-01','结构胶','双组份 400ml','支',20,20,null,45,13,5,'admin',now()),
(6,2,30,11,'COATING-PCB-01','PCB三防漆','透明 1L','桶',5,5,null,119,13,9,'admin',now()),
(7,3,10,3,'PCB-CTRL-001','智能调光控制板','蓝牙调光 24W','块',50,50,null,58,13,12,'admin',now()),
(8,3,20,12,'BATTERY-CR2032','CR2032纽扣电池','3V','粒',500,500,null,1.2,13,10,'admin',now());

insert into purchase_order(id,order_code,supplier_code,supplier_name,order_date,expected_date,status,approved_by,approved_time,total_quantity,total_amount,currency,bill_type,create_by,create_time,remark) values
(1,'PO-DEMO-001','SUP-CHEM-001','苏州新材科技有限公司',date_sub(curdate(),interval 45 day),date_sub(curdate(),interval 30 day),'APPROVED','admin',date_sub(now(),interval 44 day),115,13830,'CNY','NORMAL','admin',date_sub(now(),interval 45 day),'已完成到货入库的效期物料采购'),
(2,'PO-DEMO-DRAFT-001','SUP-ELEC-001','深圳启明电子有限公司',curdate(),date_add(curdate(),interval 14 day),'DRAFT',null,null,100,5800,'CNY','NORMAL','admin',now(),'用于测试AI补充和审核采购草稿');
insert into purchase_order_line(id,order_id,line_no,material_id,material_code,material_name,spec,model,unit,order_quantity,received_quantity,qualified_quantity,inbound_quantity,unit_price,tax_rate,amount,planned_date,quote_id,quote_line_id,price_source,create_by,create_time) values
(1,1,10,9,'SOLDER-PASTE-01','无铅焊膏','SAC305 500g','SP-SAC305','罐',50,50,50,50,168,13,8400,date_sub(curdate(),interval 30 day),1,1,'QUOTE','admin',date_sub(now(),interval 45 day)),
(2,1,20,10,'ADHESIVE-STRUCT-01','结构胶','双组份 400ml','ADH-400','支',40,40,40,40,42,13,1680,date_sub(curdate(),interval 30 day),1,2,'QUOTE','admin',date_sub(now(),interval 45 day)),
(3,1,30,11,'COATING-PCB-01','PCB三防漆','透明 1L','CC-1L','桶',25,25,25,25,126,13,3150,date_sub(curdate(),interval 30 day),1,3,'QUOTE','admin',date_sub(now(),interval 45 day)),
(4,2,10,3,'PCB-CTRL-001','智能调光控制板','蓝牙调光 24W','PCB-C24','块',100,0,0,0,58,13,5800,date_add(curdate(),interval 14 day),3,7,'QUOTE','admin',now());

insert into purchase_receipt(id,receipt_code,supplier_code,supplier_name,receipt_date,status,inspection_status,approved_by,approved_time,inspection_by,inspection_time,total_quantity,bill_type,create_by,create_time,remark) values
(1,'RCV-DEMO-001','SUP-CHEM-001','苏州新材科技有限公司',date_sub(curdate(),interval 30 day),'APPROVED','PASSED','admin',date_sub(now(),interval 30 day),'admin',date_sub(now(),interval 29 day),115,'PURCHASE_ORDER','admin',date_sub(now(),interval 30 day),'效期物料到货并质检合格');
insert into purchase_receipt_line(id,receipt_id,line_no,source_order_id,source_order_code,source_order_line_id,source_order_line_no,material_id,material_code,material_name,spec,unit,received_quantity,qualified_quantity,rejected_quantity,pending_quantity,inbound_quantity,lot_no,production_date,expiry_date,warehouse_code,warehouse_name,location_code,location_name,create_by,create_time) values
(1,1,10,1,'PO-DEMO-001',1,10,9,'SOLDER-PASTE-01','无铅焊膏','SAC305 500g','罐',50,50,0,0,50,'SP-DEMO-001',date_sub(curdate(),interval 165 day),date_add(curdate(),interval 15 day),'RM','原材料仓','RM-COLD-01','辅料冷藏区','admin',date_sub(now(),interval 30 day)),
(2,1,20,1,'PO-DEMO-001',2,20,10,'ADHESIVE-STRUCT-01','结构胶','双组份 400ml','支',40,40,0,0,40,'ADH-DEMO-001',date_sub(curdate(),interval 245 day),date_add(curdate(),interval 120 day),'RM','原材料仓','RM-CHEM-01','化学品区','admin',date_sub(now(),interval 30 day)),
(3,1,30,1,'PO-DEMO-001',3,30,11,'COATING-PCB-01','PCB三防漆','透明 1L','桶',25,25,0,0,25,'COAT-DEMO-001',date_sub(curdate(),interval 373 day),date_sub(curdate(),interval 8 day),'RM','原材料仓','RM-CHEM-01','化学品区','admin',date_sub(now(),interval 30 day));

insert into purchase_inbound(id,inbound_code,inbound_date,status,approved_by,approved_time,total_quantity,bill_type,create_by,create_time,remark) values
(1,'IN-DEMO-001',date_sub(curdate(),interval 28 day),'APPROVED','admin',date_sub(now(),interval 28 day),115,'RECEIPT','admin',date_sub(now(),interval 28 day),'采购质检合格入库');
insert into purchase_inbound_line(id,inbound_id,line_no,source_receipt_id,source_receipt_code,source_receipt_line_id,source_receipt_line_no,source_order_id,source_order_code,source_order_line_id,source_order_line_no,material_id,material_code,material_name,spec,unit,inbound_quantity,lot_no,production_date,expiry_date,warehouse_code,warehouse_name,location_code,location_name,create_by,create_time) values
(1,1,10,1,'RCV-DEMO-001',1,10,1,'PO-DEMO-001',1,10,9,'SOLDER-PASTE-01','无铅焊膏','SAC305 500g','罐',50,'SP-DEMO-001',date_sub(curdate(),interval 165 day),date_add(curdate(),interval 15 day),'RM','原材料仓','RM-COLD-01','辅料冷藏区','admin',date_sub(now(),interval 28 day)),
(2,1,20,1,'RCV-DEMO-001',2,20,1,'PO-DEMO-001',2,20,10,'ADHESIVE-STRUCT-01','结构胶','双组份 400ml','支',40,'ADH-DEMO-001',date_sub(curdate(),interval 245 day),date_add(curdate(),interval 120 day),'RM','原材料仓','RM-CHEM-01','化学品区','admin',date_sub(now(),interval 28 day)),
(3,1,30,1,'RCV-DEMO-001',3,30,1,'PO-DEMO-001',3,30,11,'COATING-PCB-01','PCB三防漆','透明 1L','桶',25,'COAT-DEMO-001',date_sub(curdate(),interval 373 day),date_sub(curdate(),interval 8 day),'RM','原材料仓','RM-CHEM-01','化学品区','admin',date_sub(now(),interval 28 day));

insert into inventory_balance(id,material_id,material_code,material_name,warehouse_code,warehouse_name,location_code,location_name,lot_no,production_date,expiry_date,unit,quantity,available_quantity,locked_quantity,status,last_inbound_date,last_update_time,create_by,create_time,remark) values
(1,9,'SOLDER-PASTE-01','无铅焊膏','RM','原材料仓','RM-COLD-01','辅料冷藏区','SP-DEMO-001',date_sub(curdate(),interval 165 day),date_add(curdate(),interval 15 day),'罐',50,50,0,'NORMAL',date_sub(curdate(),interval 28 day),now(),'admin',date_sub(now(),interval 28 day),'未来30天内到期'),
(2,10,'ADHESIVE-STRUCT-01','结构胶','RM','原材料仓','RM-CHEM-01','化学品区','ADH-DEMO-001',date_sub(curdate(),interval 245 day),date_add(curdate(),interval 120 day),'支',40,35,5,'NORMAL',date_sub(curdate(),interval 28 day),now(),'admin',date_sub(now(),interval 28 day),'正常效期，部分锁定'),
(3,11,'COATING-PCB-01','PCB三防漆','RM','原材料仓','RM-CHEM-01','化学品区','COAT-DEMO-001',date_sub(curdate(),interval 373 day),date_sub(curdate(),interval 8 day),'桶',25,25,0,'NORMAL',date_sub(curdate(),interval 28 day),now(),'admin',date_sub(now(),interval 28 day),'已过期演示批次'),
(4,3,'PCB-CTRL-001','智能调光控制板','RM','原材料仓','RM-A01','电子料A01','PCB-OPENING-001',null,null,'块',180,180,0,'NORMAL',date_sub(curdate(),interval 60 day),now(),'admin',date_sub(now(),interval 60 day),'无保质期管理的期初库存');
insert into inventory_transaction(id,transaction_type,business_id,business_code,business_line_id,material_id,material_code,material_name,warehouse_code,location_code,lot_no,unit,quantity,source_receipt_line_id,source_order_line_id,create_by,create_time,remark) values
(1,'INBOUND',1,'IN-DEMO-001',1,9,'SOLDER-PASTE-01','无铅焊膏','RM','RM-COLD-01','SP-DEMO-001','罐',50,1,1,'admin',date_sub(now(),interval 28 day),'采购入库'),
(2,'INBOUND',1,'IN-DEMO-001',2,10,'ADHESIVE-STRUCT-01','结构胶','RM','RM-CHEM-01','ADH-DEMO-001','支',40,2,2,'admin',date_sub(now(),interval 28 day),'采购入库'),
(3,'INBOUND',1,'IN-DEMO-001',3,11,'COATING-PCB-01','PCB三防漆','RM','RM-CHEM-01','COAT-DEMO-001','桶',25,3,3,'admin',date_sub(now(),interval 28 day),'采购入库');

alter table material_category auto_increment = 7;
alter table material auto_increment = 13;
alter table supplier auto_increment = 5;
alter table warehouse auto_increment = 4;
alter table location auto_increment = 7;
