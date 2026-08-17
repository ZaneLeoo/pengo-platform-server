-- 产品研发项目立项申请示例：智能视觉尺寸检测工作站研发项目。
-- 仅在目标项目不存在时创建，负责人和团队成员使用 sys_user.user_id。
set @project_code = 'PRD-2026-001';
set @manager_user_id = (select user_id from sys_user where user_name = 'lbh' and del_flag = '0' limit 1);
set @project_category_id = (select category_id from pm_project_category where status = '0' order by category_id limit 1);

insert into pm_project(project_code, project_name, category_id, manager_id, manager_user_id, applicant,
                       applicant_dept_id, start_date, end_date, status, progress, project_goal,
                       project_background, project_scope, out_of_scope, expected_outcome,
                       resource_requirement, budget_required, budget_amount, budget_description,
                       major_risk, technical_feasibility, resource_feasibility, feasibility_conclusion,
                       initiation_version, create_by, create_time)
select @project_code, '智能视觉尺寸检测工作站研发项目', @project_category_id, @manager_user_id, @manager_user_id,
       'lbh', u.dept_id, '2026-09-01', '2027-03-31', 'PENDING_APPROVAL', 0,
       '研发一套面向离散制造现场的智能视觉尺寸检测工作站，实现工件自动上料、视觉测量、结果判定与质量数据追溯。',
       '现有人工检测效率低、测量一致性不足，关键尺寸数据无法自动沉淀，需要建设可复制的智能检测工作站。',
       '包含机械结构、视觉光学、电气控制、检测软件、数据追溯、样机调试和客户试用。',
       '不包含量产线大规模改造、客户MES深度集成和后续批量设备交付。',
       '形成可现场运行的检测工作站样机、检测软件、BOM与工艺资料，并完成客户试用验收。',
       '项目经理1名，机械工程师1名，电气工程师1名，视觉算法工程师1名，软件工程师2名，测试工程师1名。',
       '1', 280000, '用于机械加工、视觉器件、工控机、样机装配和现场试用。',
       '视觉测量精度可能受光照、工件反光和定位误差影响；关键器件交期存在不确定性。',
       '采用成熟工业相机、镜头和视觉算法框架，先进行标准件标定和实验室验证，技术路径可行。',
       '已有机械、电气、软件和测试人员可组成核心团队，关键加工和器件采购由相关部门协同保障。',
       '目标明确、技术路线可验证、资源可组织，建议进入立项审批。',
       1, 'lbh', sysdate()
from sys_user u
where u.user_id = @manager_user_id
  and not exists (select 1 from pm_project where project_code = @project_code);

set @project_id = (select project_id from pm_project where project_code = @project_code limit 1);
insert into pm_project_preliminary_plan(project_id, outline_name, start_date, end_date, milestone_name, outline_description, sort_order, create_by, create_time)
select @project_id, x.outline_name, x.start_date, x.end_date, x.milestone_name, x.outline_description, x.sort_order, 'lbh', sysdate()
from (select '需求与总体方案' outline_name, '2026-09-01' start_date, '2026-09-30' end_date, '总体方案评审通过' milestone_name, '完成检测对象、精度指标和系统总体方案确认' outline_description, 1 sort_order union all
      select '机械电气与视觉设计','2026-10-01','2026-11-30','详细设计评审通过','完成机械、电气、光学和软件架构详细设计',2 union all
      select '样机制造与系统联调','2026-12-01','2027-01-31','样机联调通过','完成样机装配、软件部署、标定和系统验证',3 union all
      select '客户试用与验收','2027-02-01','2027-03-31','客户验收通过','完成现场试用、问题关闭和验收资料归档',4) x
where not exists (select 1 from pm_project_preliminary_plan where project_id = @project_id);
