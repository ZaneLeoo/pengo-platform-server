package com.ruoyi.web.controller.agent;

import com.ruoyi.agent.business.automation.application.PurchaseOrderAutomationService;
import com.ruoyi.agent.business.automation.domain.CreatePurchaseOrderDraftRequest;
import com.ruoyi.agent.business.automation.domain.CreatePurchaseOrderDraftResult;
import com.ruoyi.agent.business.automation.domain.PurchaseOrderDraftRequest;
import com.ruoyi.agent.business.automation.domain.PurchaseOrderPreparationResult;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** AI 协助建立采购订单的工具与确认入口。 */
@RestController
@RequestMapping("/agent")
public class AgentPurchaseOrderAutomationController extends BaseController
{
    private static final String TOOL_KEY_CONFIG = "agent.tool.key";
    private static final String TOOL_KEY_HEADER = "X-Agent-Tool-Key";

    private final PurchaseOrderAutomationService automationService;
    private final ISysConfigService configService;

    public AgentPurchaseOrderAutomationController(PurchaseOrderAutomationService automationService,
                                                   ISysConfigService configService)
    {
        this.automationService = automationService;
        this.configService = configService;
    }

    /** 供 Dify 调用：只校验并准备采购订单草稿，绝不写入单据。 */
    @PostMapping("/tools/purchase-orders/prepare")
    public ResponseEntity<Map<String, Object>> prepare(
        @RequestHeader(value = TOOL_KEY_HEADER, required = false) String toolKey,
        @RequestBody(required = false) PurchaseOrderDraftRequest request)
    {
        if (!isToolAuthorized(toolKey))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false, "message", "工具鉴权失败", "data", Map.of()));
        }
        PurchaseOrderPreparationResult result = automationService.prepare(request);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", result.message());
        body.put("data", result);
        return ResponseEntity.ok(body);
    }

    /** 供 agent-ui 的当前登录用户调用：确认后创建采购订单草稿。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseOrder:add')")
    @Log(title = "AI 创建采购订单草稿", businessType = BusinessType.INSERT)
    @PostMapping("/automation/purchase-orders")
    public AjaxResult createPurchaseOrderDraft(@RequestBody CreatePurchaseOrderDraftRequest request)
    {
        CreatePurchaseOrderDraftResult result = automationService.createDraft(request, getUserId(), getUsername());
        return success(result);
    }

    /** 校验仅供 Dify 使用的工具密钥。 */
    private boolean isToolAuthorized(String toolKey)
    {
        String configuredKey = configService.selectConfigByKey(TOOL_KEY_CONFIG);
        return StringUtils.isNotBlank(configuredKey) && configuredKey.equals(toolKey);
    }
}
