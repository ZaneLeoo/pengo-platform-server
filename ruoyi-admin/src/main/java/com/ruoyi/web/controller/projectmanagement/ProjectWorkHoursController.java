package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.projectmanagement.task.domain.ProjectTask;
import com.ruoyi.projectmanagement.workhours.domain.ProjectLaborRate;
import com.ruoyi.projectmanagement.workhours.domain.ProjectWorkHoursEntry;
import com.ruoyi.projectmanagement.workhours.domain.ProjectWorkHoursSheet;
import com.ruoyi.projectmanagement.workhours.service.IProjectWorkHoursService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 项目工时、人工单价接口。项目接口权限注解维持现有临时关闭策略，业务校验在服务层执行。 */
@RestController
@RequestMapping("/projectManagement/work-hours")
public class ProjectWorkHoursController extends BaseController {
    private final IProjectWorkHoursService service;

    public ProjectWorkHoursController(IProjectWorkHoursService service) {
        this.service = service;
    }

    @GetMapping("/my-sheets")
    public AjaxResult mySheets() {
        return success(service.mySheets(SecurityUtils.getUserId()));
    }

    @GetMapping("/my-sheet")
    public AjaxResult mySheet(@RequestParam(required = false) String weekStartDate) {
        return success(
                service.mySheet(
                        SecurityUtils.getUserId(),
                        weekStartDate == null ? null : LocalDate.parse(weekStartDate)));
    }

    @PostMapping("/my-sheets")
    public AjaxResult save(@RequestBody ProjectWorkHoursSheet sheet) {
        return success(service.save(SecurityUtils.getUserId(), getUsername(), sheet));
    }

    @PutMapping("/my-sheets")
    public AjaxResult edit(@RequestBody ProjectWorkHoursSheet sheet) {
        return success(service.save(SecurityUtils.getUserId(), getUsername(), sheet));
    }

    @PostMapping("/sheets/{id}/submit")
    public AjaxResult submit(@PathVariable Long id) {
        service.submit(SecurityUtils.getUserId(), getUsername(), id);
        return success();
    }

    @PostMapping("/sheets/{id}/withdraw")
    public AjaxResult withdraw(@PathVariable Long id) {
        service.withdraw(SecurityUtils.getUserId(), getUsername(), id);
        return success();
    }

    @PostMapping("/entries/{id}/correction")
    public AjaxResult correction(@PathVariable Long id) {
        return success(service.correction(SecurityUtils.getUserId(), getUsername(), id));
    }

    @GetMapping("/eligible-tasks")
    public AjaxResult eligibleTasks() {
        List<ProjectTask> list = service.eligibleTasks(SecurityUtils.getUserId());
        return success(list);
    }

    @GetMapping("/manage")
    public TableDataInfo manage(
            ProjectWorkHoursEntry filter, @RequestParam(required = false) String status) {
        if (filter == null) filter = new ProjectWorkHoursEntry();
        if ((filter.getEntryStatus() == null || filter.getEntryStatus().isBlank())
                && status != null
                && !status.isBlank()) {
            filter.setEntryStatus(status);
        }
        startPage();
        return getDataTable(service.manage(filter, SecurityUtils.getUserId()));
    }

    @GetMapping("/labor-rates")
    public TableDataInfo rates(ProjectLaborRate filter) {
        startPage();
        return getDataTable(service.rates(filter));
    }

    @PostMapping("/labor-rates")
    public AjaxResult addRate(@RequestBody ProjectLaborRate rate) {
        return success(service.saveRate(rate, getUsername()));
    }

    @PutMapping("/labor-rates")
    public AjaxResult editRate(@RequestBody ProjectLaborRate rate) {
        return success(service.saveRate(rate, getUsername()));
    }
}
