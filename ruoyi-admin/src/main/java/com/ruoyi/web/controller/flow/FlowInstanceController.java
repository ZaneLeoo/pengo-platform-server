package com.ruoyi.web.controller.flow;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.flow.engine.domain.FlowInstance;
import com.ruoyi.flow.engine.service.FlowEngineService;
import com.ruoyi.flow.engine.service.FlowInstanceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流程实例接口：我发起的、实例详情、审批链与撤销。
 */
@RestController
@RequestMapping("/flow/instance")
public class FlowInstanceController extends BaseController {

    private final FlowInstanceService instanceService;
    private final FlowEngineService engineService;

    public FlowInstanceController(FlowInstanceService instanceService, FlowEngineService engineService) {
        this.instanceService = instanceService;
        this.engineService = engineService;
    }

    /** 查询实例列表。 */
    @PreAuthorize("@ss.hasPermi('flow:task:list')")
    @GetMapping("/list")
    public AjaxResult list(FlowInstance instance) {
        return success(instanceService.list(instance));
    }

    /** 我发起的实例列表。 */
    @PreAuthorize("@ss.hasPermi('flow:task:list')")
    @GetMapping("/my")
    public AjaxResult my() {
        return success(instanceService.myStarted(getUsername()));
    }

    /** 按业务查询最新实例。 */
    @PreAuthorize("@ss.hasPermi('flow:task:list')")
    @GetMapping("/by-biz")
    public AjaxResult byBiz(@RequestParam String bizType, @RequestParam Long bizId) {
        return success(instanceService.byBiz(bizType, bizId));
    }

    /** 实例详情。 */
    @PreAuthorize("@ss.hasPermi('flow:task:list')")
    @GetMapping("/{instanceId}")
    public AjaxResult detail(@PathVariable Long instanceId) {
        return success(instanceService.detail(instanceId));
    }

    /** 审批链。 */
    @PreAuthorize("@ss.hasPermi('flow:task:list')")
    @GetMapping("/{instanceId}/history")
    public AjaxResult history(@PathVariable Long instanceId) {
        return success(instanceService.history(instanceId));
    }

    /** 撤销流程（仅发起人）。 */
    @PreAuthorize("@ss.hasPermi('flow:task:handle')")
    @Log(title = "流程撤销", businessType = BusinessType.UPDATE)
    @PostMapping("/{instanceId}/cancel")
    public AjaxResult cancel(@PathVariable Long instanceId) {
        engineService.cancel(instanceId, getUsername());
        return success();
    }
}
