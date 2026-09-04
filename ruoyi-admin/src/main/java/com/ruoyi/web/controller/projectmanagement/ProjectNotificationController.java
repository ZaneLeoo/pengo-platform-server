package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.projectmanagement.notification.service.IProjectNotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 当前登录人的待办与站内通知。 */
@RestController
@RequestMapping("/projectManagement/notification")
public class ProjectNotificationController extends BaseController {
    private final IProjectNotificationService service;

    public ProjectNotificationController(IProjectNotificationService service) {
        this.service = service;
    }

    @PreAuthorize("@ss.hasPermi('projectManagement:notification:list')")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam(required = false) String readFlag) {
        return success(service.list(getUserId(), readFlag));
    }

    @PreAuthorize("@ss.hasPermi('projectManagement:notification:list')")
    @GetMapping("/todos")
    public AjaxResult todos() {
        return success(service.todos(getUserId()));
    }

    @PreAuthorize("@ss.hasPermi('projectManagement:notification:list')")
    @GetMapping("/summary")
    public AjaxResult summary() {
        return success(service.summary(getUserId()));
    }

    @PreAuthorize("@ss.hasPermi('projectManagement:notification:edit')")
    @PutMapping("/{id}/read")
    public AjaxResult markRead(@PathVariable Long id) {
        return toAjax(service.markRead(id, getUserId()));
    }

    @PreAuthorize("@ss.hasPermi('projectManagement:notification:edit')")
    @PutMapping("/read-all")
    public AjaxResult markAllRead() {
        service.markAllRead(getUserId());
        return success();
    }
}
