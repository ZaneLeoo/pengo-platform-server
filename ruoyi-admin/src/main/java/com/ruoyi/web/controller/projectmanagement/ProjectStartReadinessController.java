package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.projectmanagement.project.service.IProjectInfoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 项目启动前计划完整性检查。 */
@RestController
@RequestMapping("/projectManagement/project")
public class ProjectStartReadinessController extends BaseController {
    private final IProjectInfoService service;

    public ProjectStartReadinessController(IProjectInfoService service) {
        this.service = service;
    }

    @PreAuthorize("@ss.hasPermi('projectManagement:project:query')")
    @GetMapping("/{id}/start-readiness")
    public AjaxResult check(@PathVariable Long id) {
        return success(service.startReadiness(id));
    }
}
