package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.projectmanagement.issue.domain.IssueTransitionRequest;
import com.ruoyi.projectmanagement.issue.domain.ProjectIssue;
import com.ruoyi.projectmanagement.issue.domain.ProjectIssueActivity;
import com.ruoyi.projectmanagement.issue.service.IProjectIssueService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projectManagement/issue")
public class ProjectIssueController extends BaseController {
    private final IProjectIssueService service;

    public ProjectIssueController(IProjectIssueService service) {
        this.service = service;
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:issue:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProjectIssue filter) {
        startPage();
        return getDataTable(service.list(filter, getUserId()));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:issue:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(service.get(id, getUserId()));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:issue:query')")
    @GetMapping("/project/{projectId}/capability")
    public AjaxResult capability(@PathVariable Long projectId) {
        return success(service.capability(projectId, getUserId()));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:issue:add')")
    @PostMapping
    public AjaxResult add(@Valid @RequestBody ProjectIssue issue) {
        return toAjax(service.add(issue, getUsername(), getUserId()));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:issue:edit')")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody ProjectIssue issue) {
        return toAjax(service.edit(issue, getUsername(), getUserId()));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:issue:edit')")
    @PutMapping("/{id}/transition")
    public AjaxResult transition(
            @PathVariable Long id, @Valid @RequestBody IssueTransitionRequest request) {
        return toAjax(service.transition(id, request, getUsername(), getUserId()));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:issue:query')")
    @GetMapping("/{id}/activities")
    public AjaxResult activities(@PathVariable Long id) {
        List<ProjectIssueActivity> activities = service.activities(id, getUserId());
        return success(activities);
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:issue:edit')")
    @PostMapping("/{id}/activities")
    public AjaxResult addActivity(
            @PathVariable Long id, @Valid @RequestBody ProjectIssueActivity activity) {
        return toAjax(service.addActivity(id, activity, getUsername(), getUserId()));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:issue:remove')")
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(service.remove(ids, getUsername(), getUserId()));
    }
}
