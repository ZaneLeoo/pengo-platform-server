package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.projectmanagement.change.domain.ProjectPlanChange;
import com.ruoyi.projectmanagement.change.domain.ProjectPlanChangeAttachment;
import com.ruoyi.projectmanagement.change.service.IProjectPlanChangeService;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.web.bind.annotation.*;

/** 项目计划基线与变更接口。 */
@RestController
@RequestMapping("/projectManagement/plan-change")
public class ProjectPlanChangeController extends BaseController {
    private final IProjectPlanChangeService service;

    public ProjectPlanChangeController(IProjectPlanChangeService service) {
        this.service = service;
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:planChange:query')")
    @GetMapping("/{projectId}/baselines")
    public AjaxResult baselines(@PathVariable Long projectId) {
        return success(service.baselines(projectId, SecurityUtils.getUserId()));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:planChange:query')")
    @GetMapping("/{projectId}/baselines/compare")
    public AjaxResult compare(
            @PathVariable Long projectId, @RequestParam Long from, @RequestParam Long to) {
        return success(service.compare(projectId, from, to, SecurityUtils.getUserId()));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:planChange:query')")
    @GetMapping("/{projectId}")
    public AjaxResult list(@PathVariable Long projectId) {
        return success(service.list(projectId, SecurityUtils.getUserId()));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:planChange:query')")
    @GetMapping("/{projectId}/capability")
    public AjaxResult capability(@PathVariable Long projectId) {
        return success(service.capability(projectId, SecurityUtils.getUserId()));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:planChange:edit')")
    @GetMapping("/{projectId}/member-candidates")
    public AjaxResult memberCandidates(
            @PathVariable Long projectId, @RequestParam(required = false) String keyword) {
        return success(service.memberCandidates(projectId, keyword, SecurityUtils.getUserId()));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:planChange:query')")
    @GetMapping("/detail/{changeId}")
    public AjaxResult detail(@PathVariable Long changeId) {
        return success(service.detail(changeId, SecurityUtils.getUserId()));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:planChange:query')")
    @GetMapping("/{changeId}/attachments/{attachmentId}/download")
    public void download(
            @PathVariable Long changeId,
            @PathVariable Long attachmentId,
            HttpServletResponse response)
            throws Exception {
        ProjectPlanChangeAttachment attachment =
                service.attachment(changeId, attachmentId, SecurityUtils.getUserId());
        String resource = attachment.getFileUrl();
        if (!FileUtils.checkAllowDownload(resource))
            throw new IllegalArgumentException("附件资源不允许下载");
        Path path =
                Paths.get(RuoYiConfig.getProfile() + FileUtils.stripPrefix(resource)).normalize();
        if (!Files.isRegularFile(path)) throw new java.io.FileNotFoundException("附件文件不存在");
        response.setContentType("application/octet-stream");
        FileUtils.setAttachmentResponseHeader(response, attachment.getFileName());
        Files.copy(path, response.getOutputStream());
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:planChange:edit')")
    @PostMapping
    public AjaxResult save(@RequestBody ProjectPlanChange change) {
        return AjaxResult.success(service.save(change, getUsername(), SecurityUtils.getUserId()));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:planChange:edit')")
    @DeleteMapping("/{changeId}")
    public AjaxResult delete(@PathVariable Long changeId) {
        service.delete(changeId, getUsername(), SecurityUtils.getUserId());
        return success();
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:planChange:edit')")
    @PostMapping("/{changeId}/submit")
    public AjaxResult submit(@PathVariable Long changeId) {
        service.submit(changeId, getUsername(), SecurityUtils.getUserId());
        return success();
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:planChange:edit')")
    @PostMapping("/{changeId}/withdraw")
    public AjaxResult withdraw(@PathVariable Long changeId) {
        service.withdraw(changeId, getUsername(), SecurityUtils.getUserId());
        return success();
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:planChange:apply')")
    @PostMapping("/{changeId}/apply")
    public AjaxResult apply(@PathVariable Long changeId) {
        service.apply(changeId, getUsername(), SecurityUtils.getUserId());
        return success();
    }
}
