package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.projectmanagement.workflow.domain.WorkflowActionRequest;
import com.ruoyi.projectmanagement.workflow.domain.WorkflowDefinition;
import com.ruoyi.projectmanagement.workflow.service.IWorkflowService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 审批流程配置和个人审批中心接口。 */
@RestController
@RequestMapping("/projectManagement/workflow")
public class WorkflowController extends BaseController {
    private final IWorkflowService service;

    public WorkflowController(IWorkflowService service) {
        this.service = service;
    }

    @PreAuthorize("@ss.hasPermi('projectManagement:workflow:config')")
    @GetMapping("/definitions")
    public AjaxResult definitions() {
        return success(service.definitions());
    }

    @PreAuthorize("@ss.hasPermi('projectManagement:workflow:config')")
    @GetMapping("/definitions/{id}")
    public AjaxResult definition(@PathVariable Long id) {
        return success(service.definition(id));
    }

    @PreAuthorize("@ss.hasPermi('projectManagement:workflow:config')")
    @PostMapping("/definitions/draft")
    public AjaxResult saveDraft(@Valid @RequestBody WorkflowDefinition definition) {
        return success(service.saveDraft(definition, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('projectManagement:workflow:config')")
    @PostMapping("/definitions/{definitionId}/versions/{versionId}/publish")
    public AjaxResult publish(@PathVariable Long definitionId, @PathVariable Long versionId) {
        service.publish(definitionId, versionId, getUsername());
        return success();
    }

    @GetMapping("/tasks")
    public AjaxResult tasks(@RequestParam(defaultValue = "pending") String scope) {
        return success(service.tasks(getUserId(), scope));
    }

    @GetMapping("/tasks/unread-count")
    public AjaxResult unreadCount() {
        return success(service.unreadCount(getUserId()));
    }

    @GetMapping("/tasks/{id}")
    public AjaxResult task(@PathVariable Long id) {
        return success(service.taskDetail(id, getUserId()));
    }

    @GetMapping("/instances/{id}")
    public AjaxResult instance(@PathVariable Long id) {
        return success(service.instanceDetail(id, getUserId()));
    }

    @PutMapping("/tasks/{id}/action")
    public AjaxResult action(
            @PathVariable Long id, @Valid @RequestBody WorkflowActionRequest request) {
        service.act(id, request, getUsername(), getUserId());
        return success();
    }
}
