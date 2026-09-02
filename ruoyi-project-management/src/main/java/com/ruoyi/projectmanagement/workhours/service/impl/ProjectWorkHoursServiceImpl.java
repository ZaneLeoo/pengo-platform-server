package com.ruoyi.projectmanagement.workhours.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.projectmanagement.budget.domain.ProjectActualCost;
import com.ruoyi.projectmanagement.budget.domain.ProjectBudgetLine;
import com.ruoyi.projectmanagement.budget.domain.ProjectWorkPackageBudgetLine;
import com.ruoyi.projectmanagement.budget.mapper.ProjectActualCostMapper;
import com.ruoyi.projectmanagement.budget.mapper.ProjectBudgetMapper;
import com.ruoyi.projectmanagement.budget.mapper.ProjectWorkPackageBudgetMapper;
import com.ruoyi.projectmanagement.budget.service.IProjectActualCostService;
import com.ruoyi.projectmanagement.common.enums.ProjectStatus;
import com.ruoyi.projectmanagement.costcategory.domain.CostCategory;
import com.ruoyi.projectmanagement.costcategory.mapper.CostCategoryMapper;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.task.domain.ProjectTask;
import com.ruoyi.projectmanagement.task.mapper.ProjectTaskMapper;
import com.ruoyi.projectmanagement.team.service.IProjectTeamService;
import com.ruoyi.projectmanagement.wbs.domain.ProjectWbsNode;
import com.ruoyi.projectmanagement.wbs.mapper.ProjectWbsMapper;
import com.ruoyi.projectmanagement.workflow.service.IWorkflowService;
import com.ruoyi.projectmanagement.workflow.service.WorkflowBusinessCallback;
import com.ruoyi.projectmanagement.workhours.domain.ProjectLaborRate;
import com.ruoyi.projectmanagement.workhours.domain.ProjectWorkHoursEntry;
import com.ruoyi.projectmanagement.workhours.domain.ProjectWorkHoursSheet;
import com.ruoyi.projectmanagement.workhours.mapper.ProjectWorkHoursMapper;
import com.ruoyi.projectmanagement.workhours.service.IProjectWorkHoursService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/** 周工时单、单价和内部人工实际成本闭环。 */
@Service
public class ProjectWorkHoursServiceImpl implements IProjectWorkHoursService, WorkflowBusinessCallback {
    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final BigDecimal EIGHT = BigDecimal.valueOf(8);
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private final ProjectWorkHoursMapper mapper;
    private final ProjectTaskMapper taskMapper;
    private final ProjectInfoMapper projectMapper;
    private final ProjectWbsMapper wbsMapper;
    private final CostCategoryMapper categoryMapper;
    private final ProjectBudgetMapper budgetMapper;
    private final ProjectWorkPackageBudgetMapper workPackageBudgetMapper;
    private final ProjectActualCostMapper actualCostMapper;
    private final IProjectActualCostService actualCostService;
    private final IWorkflowService workflowService;
    private final ObjectMapper objectMapper;
    private final IProjectTeamService teamService;

    public ProjectWorkHoursServiceImpl(ProjectWorkHoursMapper mapper, ProjectTaskMapper taskMapper, ProjectInfoMapper projectMapper, ProjectWbsMapper wbsMapper, CostCategoryMapper categoryMapper, ProjectBudgetMapper budgetMapper, ProjectWorkPackageBudgetMapper workPackageBudgetMapper, ProjectActualCostMapper actualCostMapper, IProjectActualCostService actualCostService, @Lazy IWorkflowService workflowService, ObjectMapper objectMapper, IProjectTeamService teamService) {
        this.mapper = mapper; this.taskMapper = taskMapper; this.projectMapper = projectMapper; this.wbsMapper = wbsMapper;
        this.categoryMapper = categoryMapper; this.budgetMapper = budgetMapper; this.workPackageBudgetMapper = workPackageBudgetMapper;
        this.actualCostMapper = actualCostMapper; this.actualCostService = actualCostService; this.workflowService = workflowService; this.objectMapper = objectMapper; this.teamService = teamService;
    }

    @Override public String businessType() { return "WORK_HOURS"; }
    @Override public List<ProjectWorkHoursSheet> mySheets(Long userId) { List<ProjectWorkHoursSheet> list=mapper.selectMySheets(userId); list.forEach(this::loadEntries); return list; }
    @Override public ProjectWorkHoursSheet mySheet(Long userId, LocalDate weekStartDate) { LocalDate start=weekStart(weekStartDate); ProjectWorkHoursSheet correction=mapper.selectCorrectionSheetByUserWeek(userId,start); ProjectWorkHoursSheet sheet=correction!=null?correction:mapper.selectSheetByUserWeek(userId,start); if(sheet!=null) loadEntries(sheet); return sheet; }
    @Override public List<ProjectTask> eligibleTasks(Long userId) { ProjectTask q=new ProjectTask(); q.setAssigneeUserId(userId); return taskMapper.selectList(q).stream().filter(t -> activeProject(t.getProjectId())).toList(); }
    @Override public List<ProjectWorkHoursEntry> manage(ProjectWorkHoursEntry filter, Long userId) { return mapper.selectManage(filter).stream().filter(entry -> canView(entry, userId)).toList(); }
    @Override public List<ProjectLaborRate> rates(ProjectLaborRate filter) { return mapper.selectRates(filter); }

    @Override @Transactional
    public ProjectLaborRate saveRate(ProjectLaborRate rate, String operator) {
        if(rate.getUserId()==null || rate.getEffectiveStartDate()==null || rate.getHourlyRate()==null || rate.getHourlyRate().signum()<=0) throw new ServiceException("人员、生效开始日期和大于0的小时单价不能为空");
        if(rate.getEffectiveEndDate()!=null && rate.getEffectiveEndDate().isBefore(rate.getEffectiveStartDate())) throw new ServiceException("单价结束日期不能早于开始日期");
        if(rate.getHourlyRate().scale()>2) throw new ServiceException("小时单价最多保留两位小数");
        if(mapper.countRateOverlap(rate)>0) throw new ServiceException("同一人员的启用单价有效期不能重叠");
        if(rate.getUserName()==null) rate.setUserName(String.valueOf(rate.getUserId())); if(rate.getNickName()==null) rate.setNickName(rate.getUserName());
        rate.setStatus(rate.getStatus()==null?"0":rate.getStatus()); rate.setUpdateBy(operator);
        if(rate.getRateId()==null){rate.setCreateBy(operator);mapper.insertRate(rate);} else mapper.updateRate(rate);
        return mapper.selectRateById(rate.getRateId());
    }

    @Override @Transactional
    public ProjectWorkHoursSheet save(Long userId, String userName, ProjectWorkHoursSheet input) {
        LocalDate start=weekStart(input.getWeekStartDate()); assertWritableWeek(start,input.getLateReportReason(),input.getEntries());
        ProjectWorkHoursSheet sheet=input.getSheetId()==null?mapper.selectSheetByUserWeek(userId,start):ownedSheet(userId,input.getSheetId());
        if(sheet==null){ sheet=new ProjectWorkHoursSheet(); sheet.setUserId(userId); sheet.setUserName(userName); sheet.setNickName(userName); sheet.setWeekStartDate(start); sheet.setWeekEndDate(start.plusDays(6)); sheet.setSheetType("NORMAL"); sheet.setStatus("DRAFT"); sheet.setCreateBy(userName); mapper.insertSheet(sheet); }
        if(!"DRAFT".equals(sheet.getStatus()) && !"RETURNED".equals(sheet.getStatus())) throw new ServiceException("当前周工时单不能编辑");
        List<ProjectWorkHoursEntry> entries=input.getEntries()==null?List.of():input.getEntries();
        if ("CORRECTION".equals(sheet.getSheetType())) {
            if (blank(input.getLateReportReason())) throw new ServiceException("请填写工时更正原因");
            entries.forEach(entry -> entry.setCorrectionReason(input.getLateReportReason()));
        }
        validateEntries(userId,userName,start,entries);
        sheet.setProjectId(entries.isEmpty()?null:entries.get(0).getProjectId()); sheet.setLateReportReason(input.getLateReportReason()); sheet.setUpdateBy(userName); mapper.updateSheet(sheet);
        mapper.deleteEntries(sheet.getSheetId());
        for(ProjectWorkHoursEntry entry: entries){ entry.setSheetId(sheet.getSheetId()); entry.setReportUserId(userId); entry.setReportUserName(userName); entry.setReportNickName(userName); entry.setEntryStatus("DRAFT"); entry.setCreateBy(userName); mapper.insertEntry(entry); }
        loadEntries(sheet); return sheet;
    }

    @Override @Transactional
    public void submit(Long userId,String userName,Long sheetId) {
        ProjectWorkHoursSheet sheet=ownedSheet(userId,sheetId); if(!"DRAFT".equals(sheet.getStatus())&&!"RETURNED".equals(sheet.getStatus())) throw new ServiceException("当前周工时单不能提交");
        loadEntries(sheet); if(sheet.getEntries().isEmpty()) throw new ServiceException("请至少填报一条工时明细"); validateEntries(userId,userName,sheet.getWeekStartDate(),sheet.getEntries());
        try { Long instance=workflowService.start("WORK_HOURS",sheetId,sheet.getProjectId(),"周工时审批："+sheet.getUserName()+"（"+sheet.getWeekStartDate()+"）",objectMapper.writeValueAsString(sheet.getEntries()),userName,userId); sheet.setWorkflowInstanceId(instance); }
        catch(Exception e){ if(e instanceof ServiceException) throw (ServiceException)e; throw new ServiceException("创建工时审批失败"); }
        sheet.setStatus("IN_APPROVAL"); sheet.setSubmitTime(java.time.LocalDateTime.now(SHANGHAI)); sheet.setUpdateBy(userName); mapper.updateSheetStatus(sheet);
    }

    @Override @Transactional
    public void withdraw(Long userId,String userName,Long sheetId) { ProjectWorkHoursSheet sheet=ownedSheet(userId,sheetId); if(!"IN_APPROVAL".equals(sheet.getStatus())||sheet.getWorkflowInstanceId()==null) throw new ServiceException("当前周工时单不能撤回"); workflowService.withdraw(sheet.getWorkflowInstanceId(),userName,userId); sheet.setStatus("DRAFT"); sheet.setUpdateBy(userName); mapper.updateSheetStatus(sheet); }

    @Override @Transactional
    public ProjectWorkHoursSheet correction(Long userId,String userName,Long entryId) {
        ProjectWorkHoursEntry old=mapper.selectEntry(entryId); if(old==null||!userId.equals(old.getReportUserId())||!"ARCHIVED".equals(old.getEntryStatus())) throw new ServiceException("仅可更正本人已归档工时");
        ProjectWorkHoursSheet target=mapper.selectCorrectionSheetByUserWeek(userId,weekStart(old.getWorkDate())); if(target!=null && !("DRAFT".equals(target.getStatus())||"RETURNED".equals(target.getStatus()))) throw new ServiceException("该原周次已有不可编辑的更正工时单");
        if(target==null){target=new ProjectWorkHoursSheet();target.setUserId(userId);target.setUserName(userName);target.setNickName(userName);target.setWeekStartDate(weekStart(old.getWorkDate()));target.setWeekEndDate(target.getWeekStartDate().plusDays(6));target.setProjectId(old.getProjectId());target.setSheetType("CORRECTION");target.setStatus("DRAFT");target.setCreateBy(userName);mapper.insertSheet(target);} else { target.setProjectId(old.getProjectId()); target.setUpdateBy(userName); mapper.updateSheet(target); }
        List<ProjectWorkHoursEntry> existing=mapper.selectEntries(target.getSheetId()); if(existing.stream().anyMatch(entry -> old.getEntryId().equals(entry.getSourceEntryId()))){target.setEntries(existing);return target;}
        ProjectWorkHoursEntry copy=new ProjectWorkHoursEntry(); copy.setSheetId(target.getSheetId()); copy.setProjectId(old.getProjectId());copy.setWorkPackageId(old.getWorkPackageId());copy.setTaskId(old.getTaskId());copy.setReportUserId(userId);copy.setProjectName(old.getProjectName());copy.setWorkPackageName(old.getWorkPackageName());copy.setTaskName(old.getTaskName());copy.setReportUserName(userName);copy.setReportNickName(userName);copy.setWorkDate(old.getWorkDate());copy.setHours(old.getHours());copy.setOvertimeFlag(old.getOvertimeFlag());copy.setWorkDescription(old.getWorkDescription());copy.setAchievementDescription(old.getAchievementDescription());copy.setSourceEntryId(old.getEntryId());copy.setCorrectionReason("请填写更正原因");copy.setEntryStatus("DRAFT");copy.setCreateBy(userName);mapper.insertEntry(copy); loadEntries(target); return target;
    }

    @Override @Transactional
    public void approved(Long sheetId,String operator,String opinion,Long operatorUserId,Long instanceId) {
        ProjectWorkHoursSheet sheet=mapper.selectSheet(sheetId); if(sheet==null||!"IN_APPROVAL".equals(sheet.getStatus())) return; loadEntries(sheet);
        validateEntries(sheet.getUserId(),sheet.getUserName(),sheet.getWeekStartDate(),sheet.getEntries());
        List<Long> tasks=new ArrayList<>();
        for(ProjectWorkHoursEntry e:sheet.getEntries()) { ProjectLaborRate rate=mapper.selectRate(e.getReportUserId(),e.getWorkDate()); ProjectActualCost cost=new ProjectActualCost(); cost.setWorkPackageId(e.getWorkPackageId()); cost.setCostCategoryId(internalLaborCategory().getCostCategoryId()); cost.setActualAmount(e.getHours().multiply(rate.getHourlyRate()).setScale(2,RoundingMode.HALF_UP)); cost.setOccurDate(e.getWorkDate()); cost.setDescription("工时："+e.getTaskName()+"（"+e.getHours()+"小时）"); cost.setSourceType("WORK_HOURS"); cost.setSourceLineId(e.getEntryId()); cost.setSourceDocumentNo("WH-"+sheet.getSheetId()); cost.setSourceLineNo(String.valueOf(e.getEntryId())); ProjectActualCost saved=actualCostService.register(e.getProjectId(),cost,operator,e.getReportUserId()); e.setEntryStatus("ARCHIVED");e.setCostStatus("POSTED");e.setRateIdSnapshot(rate.getRateId());e.setRateAmountSnapshot(rate.getHourlyRate());e.setActualCostId(saved.getActualCostId());e.setUpdateBy(operator);mapper.updateEntryArchive(e);tasks.add(e.getTaskId()); if(e.getSourceEntryId()!=null){mapper.reverseEntry(e.getSourceEntryId(),operator);actualCostMapper.reverseBySourceLineId("WORK_HOURS",e.getSourceEntryId(),operator,"工时更正");tasks.add(mapper.selectEntry(e.getSourceEntryId()).getTaskId());} }
        sheet.setStatus("ARCHIVED");sheet.setArchiveTime(java.time.LocalDateTime.now(SHANGHAI));sheet.setUpdateBy(operator);mapper.updateSheetStatus(sheet);tasks.stream().distinct().forEach(mapper::updateTaskActualHours);
    }
    @Override @Transactional public void rejected(Long sheetId,String operator,String opinion,Long operatorUserId,Long instanceId){ProjectWorkHoursSheet sheet=mapper.selectSheet(sheetId);if(sheet!=null&&"IN_APPROVAL".equals(sheet.getStatus())){sheet.setStatus("RETURNED");sheet.setUpdateBy(operator);mapper.updateSheetStatus(sheet);}}
    @Override public void approved(Long id,String operator,String opinion){approved(id,operator,opinion,null,null);} @Override public void rejected(Long id,String operator,String opinion){rejected(id,operator,opinion,null,null);}

    private void validateEntries(Long userId,String userName,LocalDate start,List<ProjectWorkHoursEntry> entries){ if(entries.isEmpty()) return; Map<LocalDate,BigDecimal> normal=new HashMap<>(),all=new HashMap<>();Set<String> duplicate=new HashSet<>(); Map<String,BigDecimal> amounts=new HashMap<>(); for(ProjectWorkHoursEntry e:entries){ if(e.getTaskId()==null||e.getWorkDate()==null||e.getHours()==null||e.getHours().signum()<=0||e.getHours().multiply(BigDecimal.valueOf(2)).stripTrailingZeros().scale()>0) throw new ServiceException("工时必须大于0且以0.5小时为单位"); if(e.getWorkDate().isBefore(start)||e.getWorkDate().isAfter(start.plusDays(6))) throw new ServiceException("工时日期必须在当前周内"); if(blank(e.getWorkDescription())||blank(e.getAchievementDescription()))throw new ServiceException("工作说明和成果说明不能为空"); ProjectTask task=taskMapper.selectById(e.getTaskId());if(task==null||!"EXECUTION".equals(task.getTaskType())||!userId.equals(task.getAssigneeUserId()))throw new ServiceException("只能填报本人被分配的执行任务");if(!activeProject(task.getProjectId()))throw new ServiceException("仅执行中或暂停中项目可填报工时");e.setProjectId(task.getProjectId());e.setWorkPackageId(task.getWorkPackageId());e.setProjectName(task.getProjectName());e.setWorkPackageName(task.getWorkPackageName());e.setTaskName(task.getTaskName());String key=e.getWorkDate()+":"+e.getTaskId();if(!duplicate.add(key))throw new ServiceException("同一日期同一任务不能重复填报");all.merge(e.getWorkDate(),e.getHours(),BigDecimal::add);if(!"1".equals(e.getOvertimeFlag()))normal.merge(e.getWorkDate(),e.getHours(),BigDecimal::add);ProjectLaborRate rate=mapper.selectRate(userId,e.getWorkDate());if(rate==null)throw new ServiceException("填报日期未维护有效人员小时单价");amounts.merge(e.getProjectId()+":"+e.getWorkPackageId(),e.getHours().multiply(rate.getHourlyRate()),BigDecimal::add);} for(LocalDate d:all.keySet()){if(normal.getOrDefault(d,BigDecimal.ZERO).compareTo(EIGHT)>0)throw new ServiceException("同日普通工时不能超过8小时");if(all.get(d).compareTo(TWELVE)>0)throw new ServiceException("同日含加班总工时不能超过12小时");} for(Map.Entry<String,BigDecimal>a:amounts.entrySet()){String[] ids=a.getKey().split(":");assertBudget(Long.valueOf(ids[0]),Long.valueOf(ids[1]),a.getValue());}}
    private void assertBudget(Long projectId,Long workPackageId,BigDecimal amount){CostCategory category=internalLaborCategory();ProjectBudgetLine p=budgetMapper.selectByProjectId(projectId).stream().filter(x->category.getCostCategoryId().equals(x.getCostCategoryId())).findFirst().orElseThrow(()->new ServiceException("项目未配置内部人工分类预算"));ProjectWorkPackageBudgetLine w=workPackageBudgetMapper.selectByWorkPackageId(workPackageId).stream().filter(x->category.getCostCategoryId().equals(x.getCostCategoryId())).findFirst().orElseThrow(()->new ServiceException("工作包未分配内部人工预算"));if(actualCostMapper.categoryTotal(projectId,category.getCostCategoryId()).add(amount).compareTo(p.getBudgetAmount())>0)throw new ServiceException("本次工时成本超过项目内部人工预算余额");if(actualCostMapper.workPackageCategoryTotal(projectId,workPackageId,category.getCostCategoryId()).add(amount).compareTo(w.getBudgetAmount())>0)throw new ServiceException("本次工时成本超过工作包内部人工预算余额");}
    private CostCategory internalLaborCategory(){CostCategory c=categoryMapper.selectByCode("INTERNAL_LABOR");if(c==null)throw new ServiceException("未配置内部人工成本类别");return c;}
    private ProjectWorkHoursSheet ownedSheet(Long userId,Long sheetId){ProjectWorkHoursSheet s=mapper.selectSheet(sheetId);if(s==null||!userId.equals(s.getUserId()))throw new ServiceException("您无权操作该周工时单");return s;}
    private void loadEntries(ProjectWorkHoursSheet s){s.setEntries(mapper.selectEntries(s.getSheetId()));}
    private boolean activeProject(Long id){ProjectInfo p=projectMapper.selectProjectInfoById(id);return p!=null&&(ProjectStatus.ACTIVE.matches(p.getStatus())||ProjectStatus.PAUSED.matches(p.getStatus()));}
    private boolean canView(ProjectWorkHoursEntry entry, Long userId) { if (userId.equals(entry.getReportUserId())) return true; ProjectInfo project=projectMapper.selectProjectInfoById(entry.getProjectId()); return project != null && (userId.equals(project.getManagerId()) || teamService.isActiveMember(entry.getProjectId(), userId)); }
    private void assertWritableWeek(LocalDate start,String reason,List<ProjectWorkHoursEntry> entries){LocalDate current=weekStart(LocalDate.now(SHANGHAI));boolean correction=entries!=null&&entries.stream().anyMatch(e->e.getSourceEntryId()!=null);if(start.isAfter(current)||(!correction&&start.isBefore(current.minusWeeks(1))))throw new ServiceException("仅可填报当前周或上一自然周工时；历史工时只能通过更正处理");if(start.equals(current.minusWeeks(1))&&blank(reason))throw new ServiceException("补报上一周工时必须填写补报原因");}
    private LocalDate weekStart(LocalDate d){if(d==null)d=LocalDate.now(SHANGHAI);return d.with(DayOfWeek.MONDAY);}
    private boolean blank(String s){return s==null||s.trim().isEmpty();}
}
