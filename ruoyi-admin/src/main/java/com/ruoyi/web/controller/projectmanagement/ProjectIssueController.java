package com.ruoyi.web.controller.projectmanagement;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.projectmanagement.issue.domain.ProjectIssue;
import com.ruoyi.projectmanagement.issue.service.IProjectIssueService;
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
    public ProjectIssueController(IProjectIssueService service){this.service=service;}
    @GetMapping("/list") public TableDataInfo list(ProjectIssue f){startPage();return getDataTable(service.list(f));}
    @GetMapping("/{id}") public AjaxResult get(@PathVariable Long id){return success(service.get(id));}
    @PostMapping public AjaxResult add(@RequestBody ProjectIssue i){return toAjax(service.add(i,getUsername()));}
    @PutMapping public AjaxResult edit(@RequestBody ProjectIssue i){return toAjax(service.edit(i,getUsername()));}
    @DeleteMapping("/{ids}") public AjaxResult remove(@PathVariable Long[] ids){return toAjax(service.remove(ids,getUsername()));}
}
