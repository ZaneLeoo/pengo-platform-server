package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.projectmanagement.person.domain.ProjectPerson;
import com.ruoyi.projectmanagement.person.service.IProjectPersonService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 项目人员档案控制器。 */
@RestController
@RequestMapping("/projectManagement/person")
public class ProjectPersonController extends BaseController {
    private final IProjectPersonService personService;

    public ProjectPersonController(IProjectPersonService personService) {
        this.personService = personService;
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:person:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProjectPerson person) {
        startPage();
        return getDataTable(personService.selectProjectPersonList(person));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:person:list')")
    @GetMapping("/options")
    public AjaxResult options(@RequestParam(required = false) String keyword) {
        return success(personService.selectEnabledPersonOptions(keyword));
    }

    /** 查询可绑定到人员档案的系统账号。 */
    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasAnyPermi('projectManagement:person:add,projectManagement:person:edit')")
    @GetMapping("/account-options")
    public AjaxResult accountOptions(
            @RequestParam(required = false) Long personId,
            @RequestParam(required = false) String keyword) {
        return success(personService.selectAvailableUserOptions(personId, keyword));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:person:export')")
    @Log(title = "项目人员档案", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProjectPerson person) {
        List<ProjectPerson> list = personService.selectProjectPersonList(person);
        new ExcelUtil<>(ProjectPerson.class).exportExcel(response, list, "项目人员档案");
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:person:query')")
    @GetMapping("/{personId}")
    public AjaxResult getInfo(@PathVariable Long personId) {
        return success(personService.selectProjectPersonById(personId));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:person:add')")
    @Log(title = "项目人员档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ProjectPerson person) {
        if (!personService.checkPersonCodeUnique(person)) {
            return error("新增人员'" + person.getPersonName() + "'失败，工号已存在");
        }
        person.setCreateBy(getUsername());
        return toAjax(personService.insertProjectPerson(person));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:person:edit')")
    @Log(title = "项目人员档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ProjectPerson person) {
        if (!personService.checkPersonCodeUnique(person)) {
            return error("修改人员'" + person.getPersonName() + "'失败，工号已存在");
        }
        person.setUpdateBy(getUsername());
        return toAjax(personService.updateProjectPerson(person));
    }

    // 临时关闭项目管理接口权限校验：@PreAuthorize("@ss.hasPermi('projectManagement:person:remove')")
    @Log(title = "项目人员档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{personIds}")
    public AjaxResult remove(@PathVariable Long[] personIds) {
        return toAjax(personService.deleteProjectPersonByIds(personIds));
    }
}
