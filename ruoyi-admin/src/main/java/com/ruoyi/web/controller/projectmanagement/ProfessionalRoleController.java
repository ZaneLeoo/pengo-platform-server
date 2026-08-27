package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.projectmanagement.professionalrole.domain.ProfessionalRole;
import com.ruoyi.projectmanagement.professionalrole.service.IProfessionalRoleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 项目专业角色配置接口。 */
@RestController
@RequestMapping("/projectManagement/professional-role")
public class ProfessionalRoleController extends BaseController {

    private final IProfessionalRoleService service;

    public ProfessionalRoleController(IProfessionalRoleService service) {
        this.service = service;
    }

    /** 查询专业角色配置列表。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:professionalRole:list')")
    @GetMapping("/list")
    public AjaxResult list(ProfessionalRole filter) {
        return success(service.list(filter));
    }

    /** 查询项目团队可选的启用角色。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:project:query')")
    @GetMapping("/options")
    public AjaxResult options() {
        return success(service.options());
    }

    /** 查询专业角色详情。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:professionalRole:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(service.get(id));
    }

    /** 新增专业角色。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:professionalRole:add')")
    @Log(title = "项目专业角色", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody ProfessionalRole role) {
        return toAjax(service.add(role, getUsername()));
    }

    /** 修改专业角色。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:professionalRole:edit')")
    @Log(title = "项目专业角色", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody ProfessionalRole role) {
        return toAjax(service.edit(role, getUsername()));
    }

    /** 删除专业角色。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:professionalRole:remove')")
    @Log(title = "项目专业角色", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        return toAjax(service.remove(id));
    }
}
