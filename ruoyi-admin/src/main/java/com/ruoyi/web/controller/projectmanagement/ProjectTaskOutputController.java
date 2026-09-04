package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.projectmanagement.project.service.IProjectInfoService;
import com.ruoyi.projectmanagement.task.domain.ProjectTaskOutput;
import com.ruoyi.projectmanagement.task.service.IProjectTaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 项目任务成果接口。 */
@RestController
@RequestMapping("/projectManagement/task-output")
public class ProjectTaskOutputController extends BaseController {

    private final IProjectTaskService service;
    private final IProjectInfoService projectService;

    public ProjectTaskOutputController(
            IProjectTaskService service, IProjectInfoService projectService) {
        this.service = service;
        this.projectService = projectService;
    }

    /** 查询任务成果列表。 */
    @GetMapping("/task/{taskId}")
    public AjaxResult list(@PathVariable Long taskId) {
        projectService.assertViewable(service.get(taskId).getProjectId(), getUserId());
        return success(service.outputs(taskId));
    }

    /** 新增任务成果。 */
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ProjectTaskOutput output) {
        return toAjax(service.addOutput(output, getUsername(), getUserId()));
    }

    /** 删除任务成果。 */
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        return toAjax(service.removeOutput(id, getUsername(), getUserId()));
    }
}
