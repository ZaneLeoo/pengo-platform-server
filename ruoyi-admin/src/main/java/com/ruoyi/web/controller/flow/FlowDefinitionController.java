package com.ruoyi.web.controller.flow;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.flow.definition.domain.FlowDefinition;
import com.ruoyi.flow.definition.domain.FlowDefinitionNode;
import com.ruoyi.flow.definition.service.FlowDefinitionService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审批流程定义接口。
 */
@RestController
@RequestMapping("/flow/definition")
public class FlowDefinitionController extends BaseController {

    private final FlowDefinitionService service;

    public FlowDefinitionController(FlowDefinitionService service) {
        this.service = service;
    }

    /** 查询流程定义列表。 */
    @PreAuthorize("@ss.hasPermi('flow:definition:list')")
    @GetMapping("/list")
    public AjaxResult list(FlowDefinition definition) {
        return success(service.list(definition));
    }

    /** 查询流程定义详细。 */
    @PreAuthorize("@ss.hasPermi('flow:definition:query')")
    @GetMapping("/{flowId}")
    public AjaxResult get(@PathVariable Long flowId) {
        return success(service.get(flowId));
    }

    /** 查询流程节点列表。 */
    @PreAuthorize("@ss.hasPermi('flow:definition:query')")
    @GetMapping("/{flowId}/nodes")
    public AjaxResult nodes(@PathVariable Long flowId) {
        return success(service.nodes(flowId));
    }

    /** 新增流程定义。 */
    @PreAuthorize("@ss.hasPermi('flow:definition:add')")
    @Log(title = "流程定义", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody FlowDefinition definition) {
        definition.setCreateBy(getUsername());
        return toAjax(service.add(definition));
    }

    /** 修改流程定义。 */
    @PreAuthorize("@ss.hasPermi('flow:definition:edit')")
    @Log(title = "流程定义", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody FlowDefinition definition) {
        definition.setUpdateBy(getUsername());
        return toAjax(service.edit(definition));
    }

    /** 整体保存流程节点链。 */
    @PreAuthorize("@ss.hasPermi('flow:definition:edit')")
    @Log(title = "流程节点", businessType = BusinessType.UPDATE)
    @PutMapping("/{flowId}/nodes")
    public AjaxResult saveNodes(@PathVariable Long flowId, @RequestBody List<FlowDefinitionNode> nodes) {
        service.saveNodes(flowId, nodes);
        return success();
    }

    /** 启用/停用流程。 */
    @PreAuthorize("@ss.hasPermi('flow:definition:switch')")
    @Log(title = "流程启停", businessType = BusinessType.UPDATE)
    @PutMapping("/status")
    public AjaxResult updateStatus(@RequestBody FlowDefinition definition) {
        definition.setUpdateBy(getUsername());
        return toAjax(service.updateStatus(definition));
    }

    /** 删除流程定义。 */
    @PreAuthorize("@ss.hasPermi('flow:definition:remove')")
    @Log(title = "流程定义", businessType = BusinessType.DELETE)
    @DeleteMapping("/{flowIds}")
    public AjaxResult remove(@PathVariable Long[] flowIds) {
        return toAjax(service.remove(flowIds));
    }
}
