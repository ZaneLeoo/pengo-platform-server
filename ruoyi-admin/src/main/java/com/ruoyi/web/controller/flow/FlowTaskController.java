package com.ruoyi.web.controller.flow;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.flow.engine.domain.FlowTaskRequest;
import com.ruoyi.flow.engine.service.FlowEngineService;
import com.ruoyi.flow.engine.service.FlowTaskService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审批任务接口：待办/已办与审批处理。
 */
@RestController
@RequestMapping("/flow/task")
public class FlowTaskController extends BaseController {

    private final FlowTaskService taskService;
    private final FlowEngineService engineService;

    public FlowTaskController(FlowTaskService taskService, FlowEngineService engineService) {
        this.taskService = taskService;
        this.engineService = engineService;
    }

    /** 我的待办列表。 */
    @PreAuthorize("@ss.hasPermi('flow:task:list')")
    @GetMapping("/todo")
    public AjaxResult todo() {
        return success(taskService.myTodo(getUsername()));
    }

    /** 我已处理列表。 */
    @PreAuthorize("@ss.hasPermi('flow:task:list')")
    @GetMapping("/done")
    public AjaxResult done() {
        return success(taskService.myDone(getUsername()));
    }

    /** 我的待办数量。 */
    @PreAuthorize("@ss.hasPermi('flow:task:list')")
    @GetMapping("/todo-count")
    public AjaxResult todoCount() {
        return success(taskService.todoCount(getUsername()));
    }

    /** 同意审批。 */
    @PreAuthorize("@ss.hasPermi('flow:task:handle')")
    @Log(title = "审批同意", businessType = BusinessType.UPDATE)
    @PostMapping("/{taskId}/approve")
    public AjaxResult approve(@PathVariable Long taskId, @RequestBody FlowTaskRequest request) {
        engineService.approve(taskId, request == null ? null : request.getComment(), getUsername());
        return success();
    }

    /** 驳回审批。 */
    @PreAuthorize("@ss.hasPermi('flow:task:handle')")
    @Log(title = "审批驳回", businessType = BusinessType.UPDATE)
    @PostMapping("/{taskId}/reject")
    public AjaxResult reject(@PathVariable Long taskId, @RequestBody FlowTaskRequest request) {
        engineService.reject(taskId, request == null ? null : request.getComment(), getUsername());
        return success();
    }
}
