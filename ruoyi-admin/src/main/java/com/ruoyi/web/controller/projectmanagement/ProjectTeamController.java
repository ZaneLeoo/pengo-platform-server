package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.projectmanagement.project.service.IProjectInfoService;
import com.ruoyi.projectmanagement.team.domain.ProjectMember;
import com.ruoyi.projectmanagement.team.domain.ProjectRole;
import com.ruoyi.projectmanagement.team.service.IProjectTeamService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 项目团队接口。 */
@RestController
@RequestMapping("/projectManagement/team")
public class ProjectTeamController extends BaseController {

    private final IProjectTeamService service;
    private final IProjectInfoService projectService;

    public ProjectTeamController(IProjectTeamService service, IProjectInfoService projectService) {
        this.service = service;
        this.projectService = projectService;
    }

    /** 查询项目成员列表。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:project:query')")
    @GetMapping("/{projectId}/members")
    public AjaxResult members(
            @PathVariable Long projectId, @RequestParam(required = false) String status) {
        projectService.assertViewable(projectId, getUserId());
        ProjectMember filter = new ProjectMember();
        filter.setProjectId(projectId);
        filter.setStatus(status);
        return success(service.members(filter));
    }

    /** 批量新增项目成员。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:project:edit')")
    @Log(title = "项目团队成员", businessType = BusinessType.INSERT)
    @PostMapping("/{projectId}/members")
    public AjaxResult add(@PathVariable Long projectId, @RequestBody List<ProjectMember> members) {
        service.addMembers(projectId, members, getUsername(), getUserId());
        return success();
    }

    /** 修改项目成员。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:project:edit')")
    @Log(title = "项目团队成员", businessType = BusinessType.UPDATE)
    @PutMapping("/member")
    public AjaxResult edit(@RequestBody ProjectMember member) {
        return toAjax(service.updateMember(member, getUsername(), getUserId()));
    }

    /** 成员退出项目。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:project:edit')")
    @Log(title = "项目团队成员退出", businessType = BusinessType.UPDATE)
    @PostMapping("/member/{id}/exit")
    public AjaxResult exit(@PathVariable Long id) {
        return toAjax(service.exitMember(id, getUsername(), getUserId()));
    }

    /** 查询项目角色列表。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:project:query')")
    @GetMapping("/{projectId}/roles")
    public AjaxResult roles(@PathVariable Long projectId) {
        projectService.assertViewable(projectId, getUserId());
        return success(service.roles(projectId));
    }

    /** 新增项目角色。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:project:edit')")
    @PostMapping("/{projectId}/role")
    public AjaxResult addRole(@PathVariable Long projectId, @RequestBody ProjectRole role) {
        role.setProjectId(projectId);
        return toAjax(service.addRole(role, getUsername(), getUserId()));
    }

    /** 修改项目角色。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:project:edit')")
    @PutMapping("/role")
    public AjaxResult editRole(@RequestBody ProjectRole role) {
        return toAjax(service.updateRole(role, getUsername(), getUserId()));
    }
}
