-- 类型：测试环境数据重置 / 基础数据初始化
-- 场景：工业边缘数据采集终端研发项目全流程测试
-- 警告：会清空项目、工作流运行记录、采购、库存、物料和 BOM 业务数据。
-- 保留：系统用户、角色、菜单、字典、系统配置及工作流定义/版本。
-- 仅允许在开发或测试环境执行。

set names utf8mb4;
set foreign_key_checks = 0;

-- 项目工作流运行数据
truncate table pm_workflow_task_candidate;
truncate table pm_workflow_task;
truncate table pm_workflow_event_log;
truncate table pm_workflow_instance;

-- 项目工时、成本与预算
truncate table pm_project_work_hours_entry;
truncate table pm_project_work_hours_sheet;
truncate table pm_project_actual_cost;
truncate table pm_work_package_budget_line;
truncate table pm_project_budget_line;
truncate table pm_project_labor_rate;

-- 项目计划变更、问题、交付物、任务与 WBS
truncate table pm_project_plan_change_attachment;
truncate table pm_project_plan_change_audit;
truncate table pm_project_plan_change_item;
truncate table pm_project_plan_change;
truncate table pm_project_plan_baseline;
truncate table pm_project_issue_activity;
truncate table pm_project_issue;
truncate table pm_project_deliverable_submission;
truncate table pm_project_deliverable;
truncate table pm_project_task_operation_log;
truncate table pm_project_task_output;
truncate table pm_project_task;
truncate table pm_project_wbs_node;

-- 立项、团队与项目主数据
truncate table pm_project_initiation_attachment;
truncate table pm_project_initiation_approval;
truncate table pm_project_preliminary_plan;
truncate table pm_project_lifecycle_log;
truncate table pm_project_member;
truncate table pm_project;

-- 采购与库存业务数据
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

-- 产品、物料和 BOM 基础数据
truncate table bom_ai_import_trace;
truncate table bom_item;
truncate table bom_version;
truncate table bom_master;
truncate table material;
truncate table material_category;
truncate table location;
truncate table warehouse;
truncate table supplier;

-- 清理未启用的历史同名基础表，避免恢复数据造成误判
truncate table base_location;
truncate table base_warehouse;
truncate table base_supplier;

-- 项目配置基础数据
truncate table pm_project_deliverable_type_format;
truncate table pm_project_deliverable_type;
truncate table pm_project_role;
truncate table pm_professional_role;
truncate table pm_cost_category;
truncate table pm_project_category;

-- 项目分类
insert into pm_project_category
    (category_id, parent_id, ancestors, category_code, category_name, order_num, status,
     create_by, create_time, remark)
values
    (1, 0, '0', 'PRODUCT_RND', '产品研发', 10, '0', 'system', now(), '产品设计、开发和验证类项目'),
    (2, 1, '0,1', 'INDUSTRIAL_IOT', '工业物联网产品', 10, '0', 'system', now(), '工业通信、边缘计算和数据采集产品'),
    (3, 1, '0,1', 'SOFTWARE_PRODUCT', '软件产品', 20, '0', 'system', now(), '平台、应用和工具软件产品'),
    (4, 0, '0', 'PROCESS_IMPROVEMENT', '工艺改进', 20, '0', 'system', now(), '制造工艺、质量和效率改进项目');

-- 项目团队角色
insert into pm_project_role
    (role_id, project_id, role_code, role_name, system_flag, status, sort_order, create_by, create_time)
values
    (1, 0, 'PROJECT_MANAGER', '项目负责人', '1', '0', 10, 'system', now()),
    (2, 0, 'CORE_MEMBER', '核心成员', '1', '0', 20, 'system', now()),
    (3, 0, 'MEMBER', '普通成员', '1', '0', 30, 'system', now());

-- 专业角色
insert into pm_professional_role
    (professional_role_id, role_code, role_name, role_description, system_flag, status,
     sort_order, create_by, create_time)
values
    (1, 'PROJECT_MANAGER', '项目经理', '负责目标、范围、计划、风险和跨部门协调', '1', '0', 10, 'system', now()),
    (2, 'PRODUCT_MANAGER', '产品经理', '负责需求分析、产品定义和验收场景', '1', '0', 20, 'system', now()),
    (3, 'HARDWARE_ENGINEER', '硬件工程师', '负责原理图、PCB、器件选型和硬件调试', '1', '0', 30, 'system', now()),
    (4, 'EMBEDDED_ENGINEER', '嵌入式工程师', '负责驱动、协议、固件和设备侧联调', '1', '0', 40, 'system', now()),
    (5, 'TEST_ENGINEER', '测试工程师', '负责功能、可靠性、EMC和现场验证', '1', '0', 50, 'system', now()),
    (6, 'MECHANICAL_ENGINEER', '结构工程师', '负责外壳、散热、安装和结构图纸', '1', '0', 60, 'system', now()),
    (7, 'PROCESS_ENGINEER', '工艺工程师', '负责试制工艺、装配指导和量产可制造性', '1', '0', 70, 'system', now()),
    (8, 'PROCUREMENT_ENGINEER', '采购工程师', '负责供应商、询报价、采购和交期协调', '1', '0', 80, 'system', now()),
    (9, 'QUALITY_ENGINEER', '质量工程师', '负责质量策划、问题闭环和供应商质量', '1', '0', 90, 'system', now());

-- 成本类别：仅末级类别可用于项目分类预算
insert into pm_cost_category
    (cost_category_id, parent_id, ancestors, category_code, category_name, level_no,
     allow_manual_entry, system_flag, status, sort_order, description, create_by, create_time)
values
    (1, 0, '0', 'LABOR', '人工成本', 1, '0', '1', '0', 10, '人工类成本汇总', 'system', now()),
    (2, 0, '0', 'MATERIAL', '材料成本', 1, '0', '1', '0', 20, '材料类成本汇总', 'system', now()),
    (3, 0, '0', 'OUTSOURCING', '外协成本', 1, '0', '1', '0', 30, '外部设计、加工和测试', 'system', now()),
    (11, 1, '0,1', 'INTERNAL_LABOR', '内部人工', 2, '0', '1', '0', 10, '由审批归档工时自动归集', 'system', now()),
    (12, 1, '0,1', 'EXTERNAL_LABOR', '外部人工', 2, '1', '0', '0', 20, '临时专家与外部技术人员费用', 'system', now()),
    (21, 2, '0,2', 'PROTOTYPE_MATERIAL', '样机材料', 2, '0', '1', '0', 10, '由项目采购入库自动归集', 'system', now()),
    (22, 2, '0,2', 'TRIAL_MATERIAL', '试生产材料', 2, '0', '1', '0', 20, '小批试制材料', 'system', now()),
    (31, 3, '0,3', 'OUTSOURCED_DESIGN', '外协设计', 2, '1', '0', '0', 10, '外部设计服务', 'system', now()),
    (32, 3, '0,3', 'OUTSOURCED_PROCESSING', '外协加工', 2, '1', '0', '0', 20, 'PCB、结构件等委外加工', 'system', now()),
    (33, 3, '0,3', 'OUTSOURCED_TESTING', '外协测试', 2, '1', '0', '0', 30, 'EMC和可靠性第三方测试', 'system', now()),
    (4, 0, '0', 'TEST_CERTIFICATION', '测试与认证', 1, '1', '0', '0', 40, '认证、实验室和检测费用', 'system', now()),
    (5, 0, '0', 'EQUIPMENT_TOOL', '设备及工具', 1, '1', '0', '0', 50, '专用设备、工装和仪器', 'system', now()),
    (6, 0, '0', 'TRAVEL', '差旅', 1, '1', '0', '0', 60, '现场调研、安装和试点差旅', 'system', now()),
    (7, 0, '0', 'SOFTWARE_SERVICE', '软件与技术服务', 1, '1', '0', '0', 70, '开发工具、云服务和技术许可', 'system', now()),
    (8, 0, '0', 'OTHER', '其他成本', 1, '1', '0', '0', 80, '无法归入标准类别的必要成本', 'system', now()),
    (9, 0, '0', 'CONTINGENCY', '预备费', 1, '1', '1', '0', 90, '用于已识别风险和合理不确定性', 'system', now());

-- 交付物类型和允许格式
insert into pm_project_deliverable_type
    (type_id, type_code, type_name, submission_mode, default_approval_required,
     status, sort_order, create_by, create_time)
values
    (1, 'DOCUMENT', '文档', 'FILE', '0', '0', 10, 'system', now()),
    (2, 'DRAWING', '图纸/设计数据', 'FILE', '1', '0', 20, 'system', now()),
    (3, 'BOM', 'BOM', 'FILE', '1', '0', 30, 'system', now()),
    (4, 'PROCESS', '工艺资料', 'FILE', '1', '0', 40, 'system', now()),
    (5, 'REPORT', '测试/验收报告', 'FILE', '1', '0', 50, 'system', now()),
    (6, 'FORM', '表单记录', 'FILE', '0', '0', 60, 'system', now()),
    (7, 'EXTERNAL_LINK', '外部链接', 'LINK', '0', '0', 70, 'system', now()),
    (8, 'OTHER', '其他', 'FILE', '0', '0', 80, 'system', now());

insert into pm_project_deliverable_type_format (type_id, file_extension) values
    (1, 'doc'), (1, 'docx'), (1, 'pdf'), (1, 'xls'), (1, 'xlsx'), (1, 'ppt'), (1, 'pptx'),
    (2, 'pdf'), (2, 'dwg'), (2, 'dxf'), (2, 'step'), (2, 'stp'),
    (3, 'xlsx'), (3, 'csv'), (3, 'pdf'),
    (4, 'docx'), (4, 'xlsx'), (4, 'pdf'),
    (5, 'docx'), (5, 'xlsx'), (5, 'pdf'),
    (6, 'pdf');

-- 人员全局内部成本单价，账号由现有系统用户提供
insert into pm_project_labor_rate
    (user_id, user_name, nick_name, effective_start_date, effective_end_date,
     hourly_rate, status, create_by, create_time)
select user_id, user_name, nick_name, '2026-01-01', null,
       case user_name
           when 'lps' then 260.00
           when 'csy' then 220.00
           when 'zxl' then 210.00
           when 'qa_engineer' then 180.00
           when 'procurement_officer' then 150.00
       end,
       '0', 'system', now()
from sys_user
where user_name in ('lps', 'csy', 'zxl', 'qa_engineer', 'procurement_officer')
  and status = '0' and del_flag = '0';

-- 供应商
insert into supplier
    (id, supplier_code, supplier_name, contact_person, contact_phone, address,
     currency, tax_rate, status, remark, create_by, create_time)
values
    (1, 'SUP-ELEC-001', '华东工业电子供应链有限公司', '王志强', '13800001234',
     '上海市浦东新区科苑路88号', 'CNY', 13.00, 'NORMAL', '芯片、模块和连接器供应商', 'system', now()),
    (2, 'SUP-PCB-001', '深圳精工电路科技有限公司', '刘敏', '13800002345',
     '深圳市宝安区新桥工业园', 'CNY', 13.00, 'NORMAL', 'PCB打样及小批制造供应商', 'system', now()),
    (3, 'SUP-MECH-001', '苏州恒锐精密制造有限公司', '陈伟', '13800003456',
     '苏州市工业园区星湖街168号', 'CNY', 13.00, 'NORMAL', '铝合金外壳和安装件供应商', 'system', now());

-- 仓库与库位
insert into warehouse
    (id, warehouse_code, warehouse_name, address, manager, status, remark, create_by, create_time)
values
    (1, 'WH-RD', '研发样机库', '研发中心一楼', '质量工程师', 'NORMAL', '研发样机及试制物料', 'system', now()),
    (2, 'WH-MAT', '电子物料库', '制造中心A区', '采购专员', 'NORMAL', '电子元器件和结构件', 'system', now());

insert into location
    (id, location_code, location_name, warehouse_id, warehouse_code, warehouse_name,
     status, remark, create_by, create_time)
values
    (1, 'RD-A01', '研发待检区', 1, 'WH-RD', '研发样机库', 'NORMAL', '到货待检物料', 'system', now()),
    (2, 'RD-A02', '研发合格品区', 1, 'WH-RD', '研发样机库', 'NORMAL', '检验合格的样机物料', 'system', now()),
    (3, 'MAT-E01', '电子元器件区', 2, 'WH-MAT', '电子物料库', 'NORMAL', '芯片、模块和连接器', 'system', now()),
    (4, 'MAT-S01', '结构件区', 2, 'WH-MAT', '电子物料库', 'NORMAL', '外壳、导轨卡扣和包装', 'system', now());

-- 物料分类
insert into material_category
    (category_id, parent_id, ancestors, category_code, category_name, order_num,
     status, create_by, create_time, remark)
values
    (1, 0, '0', 'FINISHED_PRODUCT', '成品', 10, '0', 'system', now(), '可独立交付的整机产品'),
    (2, 0, '0', 'SEMI_FINISHED', '半成品', 20, '0', 'system', now(), '自制装配组件'),
    (3, 0, '0', 'ELECTRONIC_COMPONENT', '电子元器件', 30, '0', 'system', now(), 'PCB、芯片、模块和连接器'),
    (4, 0, '0', 'STRUCTURAL_PART', '结构件', 40, '0', 'system', now(), '外壳和安装件'),
    (5, 0, '0', 'WIRE_HARNESS', '线束', 50, '0', 'system', now(), '内部连接线束'),
    (6, 0, '0', 'PACKAGING', '包装材料', 60, '0', 'system', now(), '标签、说明书和包装箱');

-- 工业边缘数据采集终端物料主数据
insert into material
    (material_id, material_code, material_name, material_type, category_id, spec, model,
     unit, drawing_no, material_version, source_type, lot_control_flag,
     shelf_life_control_flag, sn_control_flag, inspection_flag, safe_stock,
     status, create_by, create_time, remark)
values
    (1, 'EDG-1000', '工业边缘数据采集终端', 'FINISHED', 1, '24VDC；双网口；4路隔离RS485', 'EDG-1000',
     '台', 'DWG-EDG-1000', 'A', 'MAKE', 'Y', 'N', 'Y', 'Y', 0, '0', 'system', now(), '本次研发项目目标产品'),
    (2, 'PCBA-1000', '边缘采集主控板组件', 'SEMI_FINISHED', 2, 'STM32H743；双以太网；4路RS485', 'PCBA-1000',
     '件', 'PCB-ASSY-1000', 'A', 'MAKE', 'Y', 'N', 'Y', 'Y', 0, '0', 'system', now(), '整机核心自制组件'),
    (3, 'PWR-1000', '隔离电源模块组件', 'SEMI_FINISHED', 2, '24VDC输入；5V/3A输出；2kV隔离', 'PWR-1000',
     '件', 'PWR-ASSY-1000', 'A', 'MAKE', 'Y', 'N', 'N', 'Y', 0, '0', 'system', now(), '整机电源自制组件'),
    (4, 'PCB-1000', '主控板裸板', 'RAW', 3, '8层沉金；1.6mm；工控级', 'PCB-1000-A',
     '片', 'PCB-1000', 'A', 'PURCHASE', 'Y', 'N', 'N', 'Y', 20, '0', 'system', now(), '主控板PCB'),
    (5, 'MCU-H743', '主控制器芯片', 'RAW', 3, 'Cortex-M7；2MB Flash；LQFP144', 'STM32H743ZIT6',
     '颗', null, null, 'PURCHASE', 'Y', 'N', 'N', 'Y', 50, '0', 'system', now(), '核心控制器'),
    (6, 'ETH-W5500', '工业以太网控制器', 'RAW', 3, '硬件TCP/IP；SPI接口', 'W5500',
     '颗', null, null, 'PURCHASE', 'Y', 'N', 'N', 'Y', 50, '0', 'system', now(), '以太网通信器件'),
    (7, 'RS485-ISO', '隔离RS485收发模块', 'RAW', 3, '2.5kV隔离；15kV ESD', 'ISO485-MOD',
     '只', null, null, 'PURCHASE', 'Y', 'N', 'N', 'Y', 100, '0', 'system', now(), '现场总线接口模块'),
    (8, 'EMMC-8G', '工业级eMMC存储器', 'RAW', 3, '8GB；工业温度', 'EMMC-8G-I',
     '颗', null, null, 'PURCHASE', 'Y', 'N', 'N', 'Y', 50, '0', 'system', now(), '本地数据缓存'),
    (9, 'DCDC-24V5V', '隔离DC-DC模块', 'RAW', 3, '24V转5V；15W；2kV隔离', 'DCDC-24S05-15W',
     '只', null, null, 'PURCHASE', 'Y', 'N', 'N', 'Y', 50, '0', 'system', now(), '隔离电源核心器件'),
    (10, 'TERM-12P', '可插拔接线端子', 'RAW', 3, '12位；5.08mm；绿色', 'TERM-12P-508',
     '只', null, null, 'PURCHASE', 'N', 'N', 'N', 'Y', 100, '0', 'system', now(), '现场接线端子'),
    (11, 'ENC-1000', '铝合金整机外壳', 'RAW', 4, 'IP30；导轨安装；阳极氧化', 'ENC-EDG-1000',
     '套', 'DWG-ENC-1000', 'A', 'PURCHASE', 'Y', 'N', 'N', 'Y', 20, '0', 'system', now(), '整机结构件'),
    (12, 'DIN-CLIP', 'DIN导轨卡扣', 'RAW', 4, '35mm标准导轨', 'DIN-35',
     '只', null, null, 'PURCHASE', 'N', 'N', 'N', 'Y', 50, '0', 'system', now(), '整机安装件'),
    (13, 'HARNESS-1000', '整机内部线束', 'RAW', 5, '电源及通信连接线束', 'HARNESS-EDG-1000',
     '套', 'DWG-HARNESS-1000', 'A', 'PURCHASE', 'Y', 'N', 'N', 'Y', 20, '0', 'system', now(), '整机线束'),
    (14, 'LABEL-1000', '产品铭牌标签', 'PACKAGE', 6, '耐高温PET；二维码', 'LABEL-EDG-1000',
     '张', null, 'A', 'PURCHASE', 'Y', 'N', 'N', 'Y', 100, '0', 'system', now(), '序列号和产品信息标签'),
    (15, 'MANUAL-1000', '快速安装说明书', 'PACKAGE', 6, 'A5；彩色双面', 'MANUAL-EDG-1000',
     '本', null, 'A', 'PURCHASE', 'Y', 'N', 'N', 'N', 50, '0', 'system', now(), '随箱资料'),
    (16, 'BOX-1000', '整机包装箱', 'PACKAGE', 6, '五层瓦楞；含EPE内衬', 'BOX-EDG-1000',
     '套', null, 'A', 'PURCHASE', 'Y', 'N', 'N', 'Y', 30, '0', 'system', now(), '整机运输包装');

-- 多层 BOM：整机、主控板组件、隔离电源组件
insert into bom_master
    (id, bom_code, parent_item_id, parent_item_code, parent_item_name, parent_item_spec,
     parent_item_unit, bom_type, status, source_system, create_by, create_time, remark)
values
    (1, 'BOM-EDG-1000', 1, 'EDG-1000', '工业边缘数据采集终端', '24VDC；双网口；4路隔离RS485',
     '台', 'MANUFACTURING', 'ENABLED', 'MANUAL', 'system', now(), '整机制造BOM'),
    (2, 'BOM-PCBA-1000', 2, 'PCBA-1000', '边缘采集主控板组件', 'STM32H743；双以太网；4路RS485',
     '件', 'MANUFACTURING', 'ENABLED', 'MANUAL', 'system', now(), '主控板装配BOM'),
    (3, 'BOM-PWR-1000', 3, 'PWR-1000', '隔离电源模块组件', '24VDC输入；5V/3A输出；2kV隔离',
     '件', 'MANUFACTURING', 'ENABLED', 'MANUAL', 'system', now(), '隔离电源装配BOM');

insert into bom_version
    (id, bom_master_id, version_code, version_name, version_desc, base_qty, usage_type,
     effective_date, status, approve_status, default_flag, source_system,
     check_by, check_time, create_by, create_time)
values
    (1, 1, 'V1.0', '工程样机版', '用于工程样机和首轮验证', 1, 'GENERAL', '2026-09-15',
     'EFFECTIVE', 'APPROVED', 1, 'MANUAL', 'admin', now(), 'system', now()),
    (2, 2, 'V1.0', '主控板工程版', '首版主控板BOM', 1, 'GENERAL', '2026-09-15',
     'EFFECTIVE', 'APPROVED', 1, 'MANUAL', 'admin', now(), 'system', now()),
    (3, 3, 'V1.0', '电源工程版', '首版隔离电源BOM', 1, 'GENERAL', '2026-09-15',
     'EFFECTIVE', 'APPROVED', 1, 'MANUAL', 'admin', now(), 'system', now());

insert into bom_item
    (bom_version_id, line_no, parent_item_code, component_item_id, component_item_code,
     component_item_name, component_item_spec, component_item_unit, component_attribute,
     component_qty, fixed_loss_qty, change_loss_rate, supply_type, is_virtual,
     mrp_expand_flag, component_bom_version_id, source_system, create_by, create_time)
values
    (1, 10, 'EDG-1000', 2, 'PCBA-1000', '边缘采集主控板组件', 'STM32H743；双以太网；4路RS485', '件', 'MAKE', 1, 0, 0, 'PUSH', 0, 1, 2, 'MANUAL', 'system', now()),
    (1, 20, 'EDG-1000', 3, 'PWR-1000', '隔离电源模块组件', '24VDC输入；5V/3A输出；2kV隔离', '件', 'MAKE', 1, 0, 0, 'PUSH', 0, 1, 3, 'MANUAL', 'system', now()),
    (1, 30, 'EDG-1000', 11, 'ENC-1000', '铝合金整机外壳', 'IP30；导轨安装；阳极氧化', '套', 'PURCHASE', 1, 0, 0, 'PUSH', 0, 1, null, 'MANUAL', 'system', now()),
    (1, 40, 'EDG-1000', 12, 'DIN-CLIP', 'DIN导轨卡扣', '35mm标准导轨', '只', 'PURCHASE', 1, 0, 0, 'PUSH', 0, 1, null, 'MANUAL', 'system', now()),
    (1, 50, 'EDG-1000', 13, 'HARNESS-1000', '整机内部线束', '电源及通信连接线束', '套', 'PURCHASE', 1, 0, 0, 'PUSH', 0, 1, null, 'MANUAL', 'system', now()),
    (1, 60, 'EDG-1000', 14, 'LABEL-1000', '产品铭牌标签', '耐高温PET；二维码', '张', 'PURCHASE', 1, 0, 1, 'PUSH', 0, 1, null, 'MANUAL', 'system', now()),
    (1, 70, 'EDG-1000', 15, 'MANUAL-1000', '快速安装说明书', 'A5；彩色双面', '本', 'PURCHASE', 1, 0, 1, 'PUSH', 0, 1, null, 'MANUAL', 'system', now()),
    (1, 80, 'EDG-1000', 16, 'BOX-1000', '整机包装箱', '五层瓦楞；含EPE内衬', '套', 'PURCHASE', 1, 0, 1, 'PUSH', 0, 1, null, 'MANUAL', 'system', now()),
    (2, 10, 'PCBA-1000', 4, 'PCB-1000', '主控板裸板', '8层沉金；1.6mm；工控级', '片', 'PURCHASE', 1, 0, 2, 'PUSH', 0, 1, null, 'MANUAL', 'system', now()),
    (2, 20, 'PCBA-1000', 5, 'MCU-H743', '主控制器芯片', 'STM32H743ZIT6', '颗', 'PURCHASE', 1, 0, 1, 'PUSH', 0, 1, null, 'MANUAL', 'system', now()),
    (2, 30, 'PCBA-1000', 6, 'ETH-W5500', '工业以太网控制器', 'W5500', '颗', 'PURCHASE', 2, 0, 1, 'PUSH', 0, 1, null, 'MANUAL', 'system', now()),
    (2, 40, 'PCBA-1000', 7, 'RS485-ISO', '隔离RS485收发模块', 'ISO485-MOD', '只', 'PURCHASE', 4, 0, 1, 'PUSH', 0, 1, null, 'MANUAL', 'system', now()),
    (2, 50, 'PCBA-1000', 8, 'EMMC-8G', '工业级eMMC存储器', 'EMMC-8G-I', '颗', 'PURCHASE', 1, 0, 1, 'PUSH', 0, 1, null, 'MANUAL', 'system', now()),
    (2, 60, 'PCBA-1000', 10, 'TERM-12P', '可插拔接线端子', 'TERM-12P-508', '只', 'PURCHASE', 4, 0, 1, 'PUSH', 0, 1, null, 'MANUAL', 'system', now()),
    (3, 10, 'PWR-1000', 9, 'DCDC-24V5V', '隔离DC-DC模块', 'DCDC-24S05-15W', '只', 'PURCHASE', 1, 0, 1, 'PUSH', 0, 1, null, 'MANUAL', 'system', now()),
    (3, 20, 'PWR-1000', 10, 'TERM-12P', '可插拔接线端子', 'TERM-12P-508', '只', 'PURCHASE', 2, 0, 1, 'PUSH', 0, 1, null, 'MANUAL', 'system', now());

update material set default_bom_id = 1 where material_id = 1;
update material set default_bom_id = 2 where material_id = 2;
update material set default_bom_id = 3 where material_id = 3;

set foreign_key_checks = 1;

-- 初始化结果摘要
select 'project_category' as data_type, count(*) as row_count from pm_project_category
union all select 'professional_role', count(*) from pm_professional_role
union all select 'cost_category', count(*) from pm_cost_category
union all select 'deliverable_type', count(*) from pm_project_deliverable_type
union all select 'labor_rate', count(*) from pm_project_labor_rate
union all select 'supplier', count(*) from supplier
union all select 'warehouse', count(*) from warehouse
union all select 'location', count(*) from location
union all select 'material', count(*) from material
union all select 'bom_master', count(*) from bom_master
union all select 'bom_version', count(*) from bom_version
union all select 'bom_item', count(*) from bom_item;
