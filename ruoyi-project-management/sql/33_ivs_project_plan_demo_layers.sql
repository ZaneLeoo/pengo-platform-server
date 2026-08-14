-- 智能装配视觉检测工作站研发项目（project_id=3）计划示例。
-- 在顶层 WBS 2、3、4、5 下各增加：一层汇总 WBS → 一个工作包 → 一个执行子任务。
-- 同时配置一项必交正式交付物，使工作包满足项目启动前的完整性要求。
set @ivs_project_id := 3;

-- 2. 总体方案与详细设计：李白黑负责
set @ivs_parent_2 := (select wbs_id from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='2');
insert into pm_project_wbs_node(project_id,parent_id,wbs_code,node_type,wbs_name,scope_description,
                                plan_start_date,plan_end_date,status,progress,sort_order,create_by,create_time)
select @ivs_project_id,@ivs_parent_2,'2.1','SUMMARY','方案设计分解','总体方案细化、专业设计与评审',
       '2026-09-22','2026-10-20','NOT_STARTED',0,1,'admin',sysdate()
where @ivs_parent_2 is not null
  and not exists(select 1 from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='2.1');
set @ivs_summary_21 := (select wbs_id from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='2.1');
insert into pm_project_wbs_node(project_id,parent_id,wbs_code,node_type,wbs_name,scope_description,owner_id,
                                plan_start_date,plan_end_date,acceptance_criteria,definition_of_done,
                                deliverable_required,priority,estimated_hours,status,progress,sort_order,create_by,create_time)
select @ivs_project_id,@ivs_summary_21,'2.1.1','WORK_PACKAGE','机械与电气总体方案设计','完成机械、电气总体方案及内部评审',2,
       '2026-09-22','2026-10-20','方案满足节拍、精度、安全和可维护性要求','设计评审通过，必交成果已提交',
       '1','HIGH',120,'NOT_STARTED',0,1,'admin',sysdate()
where @ivs_summary_21 is not null
  and not exists(select 1 from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='2.1.1');
set @ivs_wp_211 := (select wbs_id from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='2.1.1');
insert into pm_project_task(project_id,work_package_id,parent_task_id,task_code,task_type,task_name,description,
                            assignee_id,plan_start_date,plan_end_date,estimated_hours,priority,status,progress,sort_order,create_by,create_time)
select @ivs_project_id,@ivs_wp_211,0,'2.1.1-T1','EXECUTION','完成机械与电气总体方案','输出总体布局、关键机构及电气控制方案',
       2,'2026-09-22','2026-10-15',96,'HIGH','NOT_STARTED',0,1,'admin',sysdate()
where @ivs_wp_211 is not null
  and not exists(select 1 from pm_project_task where project_id=@ivs_project_id and task_code='2.1.1-T1');
insert into pm_project_deliverable(project_id,work_package_id,deliverable_name,deliverable_type,deliverable_type_id,
                                   submission_mode,allowed_extensions,required_flag,approval_required,reviewer,status,
                                   description,planned_date,acceptance_criteria,create_by,create_time)
select @ivs_project_id,@ivs_wp_211,'机械与电气方案评审包','DRAWING',2,'FILE','dwg,dxf,pdf','1','1','admin','PENDING',
       '包含总体布局图、关键机构图和电气控制方案','2026-10-20','完成内部方案评审并形成结论','admin',sysdate()
where @ivs_wp_211 is not null
  and not exists(select 1 from pm_project_deliverable where project_id=@ivs_project_id and work_package_id=@ivs_wp_211 and deliverable_name='机械与电气方案评审包');

-- 3. 样机制造与软件开发：刘浩负责
set @ivs_parent_3 := (select wbs_id from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='3');
insert into pm_project_wbs_node(project_id,parent_id,wbs_code,node_type,wbs_name,scope_description,
                                plan_start_date,plan_end_date,status,progress,sort_order,create_by,create_time)
select @ivs_project_id,@ivs_parent_3,'3.1','SUMMARY','样机制造与联调分解','样机制造、软件部署与联调准备',
       '2026-11-16','2026-12-31','NOT_STARTED',0,1,'admin',sysdate()
where @ivs_parent_3 is not null
  and not exists(select 1 from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='3.1');
set @ivs_summary_31 := (select wbs_id from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='3.1');
insert into pm_project_wbs_node(project_id,parent_id,wbs_code,node_type,wbs_name,scope_description,owner_id,
                                plan_start_date,plan_end_date,acceptance_criteria,definition_of_done,
                                deliverable_required,priority,estimated_hours,status,progress,sort_order,create_by,create_time)
select @ivs_project_id,@ivs_summary_31,'3.1.1','WORK_PACKAGE','样机制造与软件联调','完成样机装配、控制软件部署及基础联调',8,
       '2026-11-16','2026-12-31','样机运行稳定，基础功能联调通过','样机联调完成，必交成果已提交',
       '1','HIGH',180,'NOT_STARTED',0,1,'admin',sysdate()
where @ivs_summary_31 is not null
  and not exists(select 1 from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='3.1.1');
set @ivs_wp_311 := (select wbs_id from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='3.1.1');
insert into pm_project_task(project_id,work_package_id,parent_task_id,task_code,task_type,task_name,description,
                            assignee_id,plan_start_date,plan_end_date,estimated_hours,priority,status,progress,sort_order,create_by,create_time)
select @ivs_project_id,@ivs_wp_311,0,'3.1.1-T1','EXECUTION','完成样机装配与软件联调','完成样机装配、控制软件部署和核心流程联调',
       8,'2026-11-16','2026-12-25',144,'HIGH','NOT_STARTED',0,1,'admin',sysdate()
where @ivs_wp_311 is not null
  and not exists(select 1 from pm_project_task where project_id=@ivs_project_id and task_code='3.1.1-T1');
insert into pm_project_deliverable(project_id,work_package_id,deliverable_name,deliverable_type,deliverable_type_id,
                                   submission_mode,allowed_extensions,required_flag,approval_required,reviewer,status,
                                   description,planned_date,acceptance_criteria,create_by,create_time)
select @ivs_project_id,@ivs_wp_311,'样机制造与联调报告','REPORT',5,'FILE','doc,docx,pdf','1','1','admin','PENDING',
       '记录样机制造、软件版本及联调结果','2026-12-31','样机连续运行并通过内部联调确认','admin',sysdate()
where @ivs_wp_311 is not null
  and not exists(select 1 from pm_project_deliverable where project_id=@ivs_project_id and work_package_id=@ivs_wp_311 and deliverable_name='样机制造与联调报告');

-- 4. 系统集成与验证：周晓岚负责
set @ivs_parent_4 := (select wbs_id from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='4');
insert into pm_project_wbs_node(project_id,parent_id,wbs_code,node_type,wbs_name,scope_description,
                                plan_start_date,plan_end_date,status,progress,sort_order,create_by,create_time)
select @ivs_project_id,@ivs_parent_4,'4.1','SUMMARY','系统集成测试分解','系统集成、性能验证与问题闭环',
       '2027-01-21','2027-02-15','NOT_STARTED',0,1,'admin',sysdate()
where @ivs_parent_4 is not null
  and not exists(select 1 from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='4.1');
set @ivs_summary_41 := (select wbs_id from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='4.1');
insert into pm_project_wbs_node(project_id,parent_id,wbs_code,node_type,wbs_name,scope_description,owner_id,
                                plan_start_date,plan_end_date,acceptance_criteria,definition_of_done,
                                deliverable_required,priority,estimated_hours,status,progress,sort_order,create_by,create_time)
select @ivs_project_id,@ivs_summary_41,'4.1.1','WORK_PACKAGE','系统集成测试','完成系统集成、节拍精度及稳定性验证',4,
       '2027-01-21','2027-02-15','达到约定节拍、精度和稳定性测试要求','测试通过，问题闭环，必交成果已提交',
       '1','HIGH',140,'NOT_STARTED',0,1,'admin',sysdate()
where @ivs_summary_41 is not null
  and not exists(select 1 from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='4.1.1');
set @ivs_wp_411 := (select wbs_id from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='4.1.1');
insert into pm_project_task(project_id,work_package_id,parent_task_id,task_code,task_type,task_name,description,
                            assignee_id,plan_start_date,plan_end_date,estimated_hours,priority,status,progress,sort_order,create_by,create_time)
select @ivs_project_id,@ivs_wp_411,0,'4.1.1-T1','EXECUTION','完成系统集成验证','执行集成测试、性能验证并推动问题闭环',
       4,'2027-01-21','2027-02-10',112,'HIGH','NOT_STARTED',0,1,'admin',sysdate()
where @ivs_wp_411 is not null
  and not exists(select 1 from pm_project_task where project_id=@ivs_project_id and task_code='4.1.1-T1');
insert into pm_project_deliverable(project_id,work_package_id,deliverable_name,deliverable_type,deliverable_type_id,
                                   submission_mode,allowed_extensions,required_flag,approval_required,reviewer,status,
                                   description,planned_date,acceptance_criteria,create_by,create_time)
select @ivs_project_id,@ivs_wp_411,'系统测试与验收报告','REPORT',5,'FILE','doc,docx,pdf','1','1','admin','PENDING',
       '记录集成测试结果、问题关闭情况和验收结论','2027-02-15','测试项覆盖完整，关键问题全部关闭','admin',sysdate()
where @ivs_wp_411 is not null
  and not exists(select 1 from pm_project_deliverable where project_id=@ivs_project_id and work_package_id=@ivs_wp_411 and deliverable_name='系统测试与验收报告');

-- 5. 客户试用与量产移交：孙宁负责
set @ivs_parent_5 := (select wbs_id from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='5');
insert into pm_project_wbs_node(project_id,parent_id,wbs_code,node_type,wbs_name,scope_description,
                                plan_start_date,plan_end_date,status,progress,sort_order,create_by,create_time)
select @ivs_project_id,@ivs_parent_5,'5.1','SUMMARY','客户试用与移交分解','客户试用、培训、问题闭环和量产资料移交',
       '2027-03-01','2027-03-25','NOT_STARTED',0,1,'admin',sysdate()
where @ivs_parent_5 is not null
  and not exists(select 1 from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='5.1');
set @ivs_summary_51 := (select wbs_id from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='5.1');
insert into pm_project_wbs_node(project_id,parent_id,wbs_code,node_type,wbs_name,scope_description,owner_id,
                                plan_start_date,plan_end_date,acceptance_criteria,definition_of_done,
                                deliverable_required,priority,estimated_hours,status,progress,sort_order,create_by,create_time)
select @ivs_project_id,@ivs_summary_51,'5.1.1','WORK_PACKAGE','客户试用与量产移交','完成现场试用、客户培训和量产资料移交',7,
       '2027-03-01','2027-03-25','客户试用通过，量产资料完整且已移交','客户确认完成，必交成果已提交',
       '1','MEDIUM',96,'NOT_STARTED',0,1,'admin',sysdate()
where @ivs_summary_51 is not null
  and not exists(select 1 from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='5.1.1');
set @ivs_wp_511 := (select wbs_id from pm_project_wbs_node where project_id=@ivs_project_id and wbs_code='5.1.1');
insert into pm_project_task(project_id,work_package_id,parent_task_id,task_code,task_type,task_name,description,
                            assignee_id,plan_start_date,plan_end_date,estimated_hours,priority,status,progress,sort_order,create_by,create_time)
select @ivs_project_id,@ivs_wp_511,0,'5.1.1-T1','EXECUTION','完成客户试用问题闭环','组织客户试用、培训并关闭遗留问题',
       7,'2027-03-01','2027-03-20',80,'MEDIUM','NOT_STARTED',0,1,'admin',sysdate()
where @ivs_wp_511 is not null
  and not exists(select 1 from pm_project_task where project_id=@ivs_project_id and task_code='5.1.1-T1');
insert into pm_project_deliverable(project_id,work_package_id,deliverable_name,deliverable_type,deliverable_type_id,
                                   submission_mode,allowed_extensions,required_flag,approval_required,reviewer,status,
                                   description,planned_date,acceptance_criteria,create_by,create_time)
select @ivs_project_id,@ivs_wp_511,'客户试用总结与量产移交清单','FORM',6,'FILE','xlsx,pdf','1','1','admin','PENDING',
       '包含客户试用结论、培训记录和量产资料移交清单','2027-03-25','客户确认试用完成，移交项签字确认','admin',sysdate()
where @ivs_wp_511 is not null
  and not exists(select 1 from pm_project_deliverable where project_id=@ivs_project_id and work_package_id=@ivs_wp_511 and deliverable_name='客户试用总结与量产移交清单');

-- 脚本可重复执行：将已生成的示例节点同步回各顶层 WBS 的立项批准目标窗口内。
update pm_project_wbs_node
set plan_start_date = case wbs_code
    when '2.1' then '2026-09-22' when '2.1.1' then '2026-09-22'
    when '3.1' then '2026-11-16' when '3.1.1' then '2026-11-16'
    when '4.1' then '2027-01-21' when '4.1.1' then '2027-01-21'
    when '5.1' then '2027-03-01' when '5.1.1' then '2027-03-01'
end
where project_id = @ivs_project_id
  and wbs_code in ('2.1','2.1.1','3.1','3.1.1','4.1','4.1.1','5.1','5.1.1');

update pm_project_task
set plan_start_date = case task_code
    when '2.1.1-T1' then '2026-09-22'
    when '3.1.1-T1' then '2026-11-16'
    when '4.1.1-T1' then '2027-01-21'
    when '5.1.1-T1' then '2027-03-01'
end
where project_id = @ivs_project_id
  and task_code in ('2.1.1-T1','3.1.1-T1','4.1.1-T1','5.1.1-T1');
