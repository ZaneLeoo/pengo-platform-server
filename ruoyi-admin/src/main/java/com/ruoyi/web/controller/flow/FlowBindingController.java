package com.ruoyi.web.controller.flow;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.flow.binding.domain.FlowBinding;
import com.ruoyi.flow.binding.service.FlowBindingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流程绑定接口：业务类型与审批流程的绑定。
 */
@RestController
@RequestMapping("/flow/binding")
public class FlowBindingController extends BaseController {

    private final FlowBindingService service;

    public FlowBindingController(FlowBindingService service) {
        this.service = service;
    }

    /** 查询绑定列表。 */
    @PreAuthorize("@ss.hasPermi('flow:definition:query')")
    @GetMapping("/list")
    public AjaxResult list(FlowBinding binding) {
        return success(service.list(binding));
    }

    /** 保存绑定（业务类型已存在则更新流程）。 */
    @PreAuthorize("@ss.hasPermi('flow:definition:edit')")
    @Log(title = "流程绑定", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult save(@Validated @RequestBody FlowBinding binding) {
        binding.setCreateBy(getUsername());
        return toAjax(service.save(binding));
    }

    /** 删除绑定。 */
    @PreAuthorize("@ss.hasPermi('flow:definition:edit')")
    @Log(title = "流程绑定", businessType = BusinessType.DELETE)
    @DeleteMapping("/{bindingIds}")
    public AjaxResult remove(@PathVariable Long[] bindingIds) {
        return toAjax(service.remove(bindingIds));
    }
}
