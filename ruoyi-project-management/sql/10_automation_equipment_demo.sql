-- 自动化设备项目及 WBS 演示数据，可重复执行。
set @category_id = (select category_id from pm_project_category where category_code = 'new-project-dev' limit 1);
set @manager_id = (select person_id from pm_person where status = '0' order by person_id limit 1);

insert into pm_project (
    project_code, project_name, category_id, manager_id, start_date, end_date,
    status, progress, project_goal, remark, create_by, create_time
)
select
    'AUTO-2026-001', '智能装配自动化设备项目', @category_id, @manager_id,
    '2026-09-01', '2027-02-28', 'ACTIVE', 18,
    '完成一套智能装配自动化设备的设计、制造、调试和客户验收，在满足节拍、精度和安全要求的前提下按期交付。',
    '项目管理 MVP 演示项目', 'admin', sysdate()
where not exists (select 1 from pm_project where project_code = 'AUTO-2026-001');

set @project_id = (select project_id from pm_project where project_code = 'AUTO-2026-001' limit 1);

-- 一级 WBS
insert into pm_project_work_item (project_id,parent_id,item_type,item_code,item_name,owner_id,status,priority,start_date,due_date,progress,description,sort_order,create_by,create_time)
select @project_id,0,'TASK','AUTO-WBS-01','方案设计',@manager_id,'ACTIVE','MEDIUM','2026-09-01','2026-09-30',60,'完成需求澄清及总体方案评审。',10,'admin',sysdate()
where not exists (select 1 from pm_project_work_item where item_code='AUTO-WBS-01');
set @wbs_01=(select item_id from pm_project_work_item where item_code='AUTO-WBS-01' limit 1);

insert into pm_project_work_item (project_id,parent_id,item_type,item_code,item_name,owner_id,status,priority,start_date,due_date,progress,description,sort_order,create_by,create_time)
select @project_id,0,'TASK','AUTO-WBS-02','详细设计',@manager_id,'ACTIVE','MEDIUM','2026-10-01','2026-11-15',20,'输出可用于采购、加工和编程的详细设计资料。',20,'admin',sysdate()
where not exists (select 1 from pm_project_work_item where item_code='AUTO-WBS-02');
set @wbs_02=(select item_id from pm_project_work_item where item_code='AUTO-WBS-02' limit 1);

insert into pm_project_work_item (project_id,parent_id,item_type,item_code,item_name,owner_id,status,priority,start_date,due_date,progress,description,sort_order,create_by,create_time)
select @project_id,0,'TASK','AUTO-WBS-03','采购与制造',@manager_id,'NOT_STARTED','MEDIUM','2026-11-01','2026-12-31',0,'完成标准件采购、非标件加工和来料检验。',30,'admin',sysdate()
where not exists (select 1 from pm_project_work_item where item_code='AUTO-WBS-03');
set @wbs_03=(select item_id from pm_project_work_item where item_code='AUTO-WBS-03' limit 1);

insert into pm_project_work_item (project_id,parent_id,item_type,item_code,item_name,owner_id,status,priority,start_date,due_date,progress,description,sort_order,create_by,create_time)
select @project_id,0,'TASK','AUTO-WBS-04','装配与交付',@manager_id,'NOT_STARTED','HIGH','2027-01-01','2027-02-28',0,'完成装配调试、厂内验收、现场安装和最终验收。',40,'admin',sysdate()
where not exists (select 1 from pm_project_work_item where item_code='AUTO-WBS-04');
set @wbs_04=(select item_id from pm_project_work_item where item_code='AUTO-WBS-04' limit 1);

-- 二级 WBS：方案设计
insert into pm_project_work_item (project_id,parent_id,item_type,item_code,item_name,owner_id,status,priority,start_date,due_date,progress,description,sort_order,create_by,create_time)
select @project_id,@wbs_01,'TASK','AUTO-WBS-01-01','需求确认',@manager_id,'COMPLETED','HIGH','2026-09-01','2026-09-07',100,'确认产品、产能、节拍、精度、安全及现场接口要求。',11,'admin',sysdate()
where not exists (select 1 from pm_project_work_item where item_code='AUTO-WBS-01-01');
insert into pm_project_work_item (project_id,parent_id,item_type,item_code,item_name,owner_id,status,priority,start_date,due_date,progress,description,sort_order,create_by,create_time)
select @project_id,@wbs_01,'TASK','AUTO-WBS-01-02','机械方案设计',@manager_id,'ACTIVE','MEDIUM','2026-09-08','2026-09-22',60,'完成机构布局、关键选型和节拍分析。',12,'admin',sysdate()
where not exists (select 1 from pm_project_work_item where item_code='AUTO-WBS-01-02');
insert into pm_project_work_item (project_id,parent_id,item_type,item_code,item_name,owner_id,status,priority,start_date,due_date,progress,description,sort_order,create_by,create_time)
select @project_id,@wbs_01,'TASK','AUTO-WBS-01-03','电气方案设计',@manager_id,'ACTIVE','MEDIUM','2026-09-08','2026-09-22',50,'完成控制架构、安全回路和主要电气元件选型。',13,'admin',sysdate()
where not exists (select 1 from pm_project_work_item where item_code='AUTO-WBS-01-03');
insert into pm_project_work_item (project_id,parent_id,item_type,item_code,item_name,owner_id,status,priority,start_date,due_date,progress,description,sort_order,create_by,create_time)
select @project_id,@wbs_01,'TASK','AUTO-WBS-01-04','总体方案评审',@manager_id,'NOT_STARTED','HIGH','2026-09-23','2026-09-30',0,'组织机械、电气、软件、制造和客户代表完成方案评审。',14,'admin',sysdate()
where not exists (select 1 from pm_project_work_item where item_code='AUTO-WBS-01-04');

-- 二级 WBS：详细设计
insert into pm_project_work_item (project_id,parent_id,item_type,item_code,item_name,owner_id,status,priority,start_date,due_date,progress,description,sort_order,create_by,create_time)
select @project_id,@wbs_02,'TASK','AUTO-WBS-02-01','机械图纸设计',@manager_id,'ACTIVE','MEDIUM','2026-10-01','2026-10-31',30,'完成三维模型、二维加工图和机械 BOM。',21,'admin',sysdate()
where not exists (select 1 from pm_project_work_item where item_code='AUTO-WBS-02-01');
insert into pm_project_work_item (project_id,parent_id,item_type,item_code,item_name,owner_id,status,priority,start_date,due_date,progress,description,sort_order,create_by,create_time)
select @project_id,@wbs_02,'TASK','AUTO-WBS-02-02','电气图纸设计',@manager_id,'NOT_STARTED','MEDIUM','2026-10-01','2026-10-31',0,'完成电气原理图、接线图和电气 BOM。',22,'admin',sysdate()
where not exists (select 1 from pm_project_work_item where item_code='AUTO-WBS-02-02');
insert into pm_project_work_item (project_id,parent_id,item_type,item_code,item_name,owner_id,status,priority,start_date,due_date,progress,description,sort_order,create_by,create_time)
select @project_id,@wbs_02,'TASK','AUTO-WBS-02-03','PLC与HMI程序设计',@manager_id,'NOT_STARTED','MEDIUM','2026-10-15','2026-11-15',0,'完成控制程序、操作画面、报警和配方功能。',23,'admin',sysdate()
where not exists (select 1 from pm_project_work_item where item_code='AUTO-WBS-02-03');

-- 二级 WBS：采购与制造
insert into pm_project_work_item (project_id,parent_id,item_type,item_code,item_name,owner_id,status,priority,start_date,due_date,progress,description,sort_order,create_by,create_time)
select @project_id,@wbs_03,'TASK','AUTO-WBS-03-01','标准件采购',@manager_id,'NOT_STARTED','MEDIUM','2026-11-01','2026-12-10',0,'采购电机、气缸、传感器、PLC等标准件。',31,'admin',sysdate()
where not exists (select 1 from pm_project_work_item where item_code='AUTO-WBS-03-01');
insert into pm_project_work_item (project_id,parent_id,item_type,item_code,item_name,owner_id,status,priority,start_date,due_date,progress,description,sort_order,create_by,create_time)
select @project_id,@wbs_03,'TASK','AUTO-WBS-03-02','非标件加工',@manager_id,'NOT_STARTED','HIGH','2026-11-10','2026-12-20',0,'按发布图纸完成非标零件加工。',32,'admin',sysdate()
where not exists (select 1 from pm_project_work_item where item_code='AUTO-WBS-03-02');
insert into pm_project_work_item (project_id,parent_id,item_type,item_code,item_name,owner_id,status,priority,start_date,due_date,progress,description,sort_order,create_by,create_time)
select @project_id,@wbs_03,'TASK','AUTO-WBS-03-03','零件检验与齐套',@manager_id,'NOT_STARTED','HIGH','2026-12-01','2026-12-31',0,'完成来料检验、不合格处理及装配齐套确认。',33,'admin',sysdate()
where not exists (select 1 from pm_project_work_item where item_code='AUTO-WBS-03-03');

-- 二级 WBS：装配与交付
insert into pm_project_work_item (project_id,parent_id,item_type,item_code,item_name,owner_id,status,priority,start_date,due_date,progress,description,sort_order,create_by,create_time)
select @project_id,@wbs_04,'TASK','AUTO-WBS-04-01','设备装配与调试',@manager_id,'NOT_STARTED','HIGH','2027-01-01','2027-01-31',0,'完成机械装配、电气接线、单机调试和联动调试。',41,'admin',sysdate()
where not exists (select 1 from pm_project_work_item where item_code='AUTO-WBS-04-01');
insert into pm_project_work_item (project_id,parent_id,item_type,item_code,item_name,owner_id,status,priority,start_date,due_date,progress,description,sort_order,create_by,create_time)
select @project_id,@wbs_04,'TASK','AUTO-WBS-04-02','厂内验收 FAT',@manager_id,'NOT_STARTED','HIGH','2027-02-01','2027-02-07',0,'按技术协议完成厂内功能、节拍、精度和安全验收。',42,'admin',sysdate()
where not exists (select 1 from pm_project_work_item where item_code='AUTO-WBS-04-02');
insert into pm_project_work_item (project_id,parent_id,item_type,item_code,item_name,owner_id,status,priority,start_date,due_date,progress,description,sort_order,create_by,create_time)
select @project_id,@wbs_04,'TASK','AUTO-WBS-04-03','客户现场安装调试',@manager_id,'NOT_STARTED','HIGH','2027-02-08','2027-02-21',0,'完成运输、就位、现场接口连接及试生产。',43,'admin',sysdate()
where not exists (select 1 from pm_project_work_item where item_code='AUTO-WBS-04-03');
insert into pm_project_work_item (project_id,parent_id,item_type,item_code,item_name,owner_id,status,priority,start_date,due_date,progress,description,sort_order,create_by,create_time)
select @project_id,@wbs_04,'TASK','AUTO-WBS-04-04','最终验收 SAT',@manager_id,'NOT_STARTED','HIGH','2027-02-22','2027-02-28',0,'完成现场验收、培训、资料移交和项目收尾。',44,'admin',sysdate()
where not exists (select 1 from pm_project_work_item where item_code='AUTO-WBS-04-04');
