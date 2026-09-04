package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.projectmanagement.execution.domain.LifecycleActionRequest;
import com.ruoyi.projectmanagement.project.service.IProjectInfoService;
import com.ruoyi.projectmanagement.task.domain.ProjectTask;
import com.ruoyi.projectmanagement.task.service.IProjectTaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 项目任务接口。 */
@RestController
@RequestMapping("/projectManagement/task")
public class ProjectTaskController extends BaseController {

    private final IProjectTaskService service;
    private final IProjectInfoService projectService;

    public ProjectTaskController(IProjectTaskService service, IProjectInfoService projectService) {
        this.service = service;
        this.projectService = projectService;
    }

    /** 查询任务列表。 */
    @GetMapping("/list")
    public TableDataInfo list(ProjectTask filter) {
        if (filter == null || filter.getProjectId() == null) {
            throw new com.ruoyi.common.exception.ServiceException("请选择项目");
        }
        projectService.assertViewable(filter.getProjectId(), getUserId());
        startPage();
        return getDataTable(service.list(filter));
    }

    /** 查询当前登录人员的执行任务。 */
    @GetMapping("/mine")
    public TableDataInfo mine(ProjectTask filter) {
        startPage();
        return getDataTable(service.listMine(getUserId(), filter));
    }

    /** 查询任务详细。 */
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        ProjectTask task = service.get(id);
        projectService.assertViewable(task.getProjectId(), getUserId());
        return success(task);
    }

    /** 查询执行任务的开始、暂停、恢复、完成记录。 */
    @GetMapping("/{id}/operation-logs")
    public AjaxResult operationLogs(@PathVariable Long id) {
        ProjectTask task = service.get(id);
        projectService.assertViewable(task.getProjectId(), getUserId());
        return success(service.operationLogs(id));
    }

    /** 新增任务，返回新任务ID。 */
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ProjectTask task) {
        return AjaxResult.success("新增成功", service.add(task, getUsername()));
    }

    /** 修改任务。 */
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ProjectTask task) {
        return toAjax(service.edit(task, getUsername()));
    }

    /** 删除任务。 */
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        return toAjax(service.remove(id, getUsername()));
    }

    /** 执行任务生命周期动作。 */
    @PostMapping("/{id}/lifecycle")
    public AjaxResult lifecycle(
            @PathVariable Long id, @Validated @RequestBody LifecycleActionRequest request) {
        return toAjax(service.lifecycle(id, request, getUsername(), getUserId()));
    }
}
