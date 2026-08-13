-- 新领域模型示例：智能装配设备开发项目。
update sys_menu set menu_name='任务管理' where menu_id=2142 or (path='task' and component='projectManagement/task/index');
set @manager=(select person_id from pm_person where person_code='001' limit 1);
set @member=(select person_id from pm_person where person_code='002' limit 1);
set @category=(select category_id from pm_project_category order by category_id limit 1);
insert into pm_project(project_code,project_name,category_id,manager_id,applicant,start_date,end_date,status,progress,project_goal,project_background,project_scope,expected_outcome,resource_requirement,major_risk,feasibility_conclusion,initiation_version,initiation_time,create_by,create_time)
select 'AUTO-2026-001','智能装配自动化设备项目',@category,@manager,'admin','2026-09-01','2027-02-28','APPROVED',0,'完成智能装配设备的设计、制造、调试与客户验收。','现有人工装配节拍和一致性无法满足新增产能。','完成机械、电气、软件设计，设备制造、调试及验收。','一套满足节拍、精度和安全要求的智能装配设备。','机械、电气、软件、采购和测试人员及试制资源。','关键器件交期与现场接口变更。','技术路线成熟、资源可协调，项目可实施。',1,sysdate(),'admin',sysdate()
where not exists(select 1 from pm_project where project_code='AUTO-2026-001');
set @project=(select project_id from pm_project where project_code='AUTO-2026-001');
insert into pm_project_member(project_id,person_id,role_id,specialty_role,responsibility,join_date,status,create_by,create_time)
select @project,@manager,(select role_id from pm_project_role where project_id=0 and role_code='PROJECT_MANAGER'),'项目经理','项目统筹、技术决策和客户接口','2026-08-13','ACTIVE','admin',sysdate() where not exists(select 1 from pm_project_member where project_id=@project and person_id=@manager);
insert into pm_project_member(project_id,person_id,role_id,specialty_role,responsibility,join_date,status,create_by,create_time)
select @project,@member,(select role_id from pm_project_role where project_id=0 and role_code='CORE_MEMBER'),'机械工程师','机械方案、详细设计和图纸交付','2026-08-13','ACTIVE','admin',sysdate() where @member is not null and not exists(select 1 from pm_project_member where project_id=@project and person_id=@member);
insert into pm_project_wbs_node(project_id,parent_id,wbs_code,node_type,wbs_name,scope_description,target_start_date,target_end_date,target_milestone,status,progress,sort_order,create_by,create_time)
select @project,0,'1','SUMMARY','设备研发与交付','从方案设计到客户验收的完整范围','2026-09-01','2027-02-28','客户终验通过','NOT_STARTED',0,1,'admin',sysdate() where not exists(select 1 from pm_project_wbs_node where project_id=@project and wbs_code='1');
set @root=(select wbs_id from pm_project_wbs_node where project_id=@project and wbs_code='1');
insert into pm_project_wbs_node(project_id,parent_id,wbs_code,node_type,wbs_name,scope_description,status,progress,sort_order,create_by,create_time)
select @project,@root,'1.1','SUMMARY','机械系统','设备机械结构及执行机构范围','NOT_STARTED',0,1,'admin',sysdate() where not exists(select 1 from pm_project_wbs_node where project_id=@project and wbs_code='1.1');
set @summary=(select wbs_id from pm_project_wbs_node where project_id=@project and wbs_code='1.1');
insert into pm_project_wbs_node(project_id,parent_id,wbs_code,node_type,wbs_name,scope_description,owner_id,plan_start_date,plan_end_date,acceptance_criteria,definition_of_done,priority,estimated_hours,status,progress,sort_order,create_by,create_time)
select @project,@summary,'1.1.1','WORK_PACKAGE','机械方案设计','完成设备总体布局、机构选型和评审',@manager,'2026-09-01','2026-09-30','方案满足节拍、精度、安全和维护性要求','方案评审通过且必交成果完成','HIGH',160,'NOT_STARTED',0,1,'admin',sysdate() where not exists(select 1 from pm_project_wbs_node where project_id=@project and wbs_code='1.1.1');
set @wp=(select wbs_id from pm_project_wbs_node where project_id=@project and wbs_code='1.1.1');
insert into pm_project_task(project_id,work_package_id,parent_task_id,task_code,task_type,task_name,description,plan_start_date,plan_end_date,status,progress,sort_order,create_by,create_time)
select @project,@wp,0,'1.1.1-T1','SUMMARY','方案设计与评审','机械方案设计执行计划','2026-09-01','2026-09-30','NOT_STARTED',0,1,'admin',sysdate() where not exists(select 1 from pm_project_task where project_id=@project and task_code='1.1.1-T1');
set @st=(select task_id from pm_project_task where project_id=@project and task_code='1.1.1-T1');
insert into pm_project_task(project_id,work_package_id,parent_task_id,task_code,task_type,task_name,description,assignee_id,plan_start_date,plan_end_date,estimated_hours,priority,status,progress,sort_order,create_by,create_time)
select @project,@wp,@st,'1.1.1-T1.1','EXECUTION','完成总体布局设计','输出设备总体布局和关键机构选型',@manager,'2026-09-01','2026-09-15',80,'HIGH','NOT_STARTED',0,1,'admin',sysdate() where not exists(select 1 from pm_project_task where project_id=@project and task_code='1.1.1-T1.1');
insert into pm_project_task(project_id,work_package_id,parent_task_id,task_code,task_type,task_name,description,assignee_id,plan_start_date,plan_end_date,estimated_hours,priority,status,progress,sort_order,create_by,create_time)
select @project,@wp,@st,'1.1.1-T1.2','EXECUTION','组织机械方案评审','准备评审材料并关闭评审意见',coalesce(@member,@manager),'2026-09-16','2026-09-30',40,'MEDIUM','NOT_STARTED',0,2,'admin',sysdate() where not exists(select 1 from pm_project_task where project_id=@project and task_code='1.1.1-T1.2');
insert into pm_project_deliverable(project_id,work_package_id,deliverable_name,deliverable_type,required_flag,approval_required,reviewer,status,description,planned_date,acceptance_criteria,create_by,create_time)
select @project,@wp,'机械总体方案说明书','DOCUMENT','1','1','admin','PENDING','正式方案文档','2026-09-30','评审结论为通过，意见全部关闭','admin',sysdate() where not exists(select 1 from pm_project_deliverable where project_id=@project and work_package_id=@wp and deliverable_name='机械总体方案说明书');
insert into pm_project_task_output(task_id,output_name,file_url,description,create_by,create_time)
select task_id,'总体布局草图','/profile/upload/demo/layout-draft.pdf','过程成果示例，不参与正式交付审批','admin',sysdate() from pm_project_task where project_id=@project and task_code='1.1.1-T1.1' and not exists(select 1 from pm_project_task_output o where o.task_id=pm_project_task.task_id);
