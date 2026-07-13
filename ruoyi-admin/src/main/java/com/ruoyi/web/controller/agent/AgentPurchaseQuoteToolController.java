package com.ruoyi.web.controller.agent;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.purchase.domain.dto.PurchaseQuoteCompareRequest;
import com.ruoyi.mes.purchase.domain.dto.PurchaseQuoteCompareResult;
import com.ruoyi.mes.purchase.service.IPurchaseSupplierQuoteService;
import com.ruoyi.system.service.ISysConfigService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 面向 Dify Agent 的供应商报价比较工具。 */
@RestController
@RequestMapping("/agent/tools/purchase-quotes")
public class AgentPurchaseQuoteToolController
{
    private static final String TOOL_KEY_CONFIG = "agent.tool.key";
    private static final String TOOL_KEY_HEADER = "X-Agent-Tool-Key";

    private final IPurchaseSupplierQuoteService quoteService;
    private final ISysConfigService configService;

    public AgentPurchaseQuoteToolController(IPurchaseSupplierQuoteService quoteService,
                                             ISysConfigService configService)
    {
        this.quoteService = quoteService;
        this.configService = configService;
    }

    /** 只读比较当前有效报价，不创建采购订单。 */
    @PostMapping("/compare")
    public ResponseEntity<Map<String, Object>> compare(
        @RequestHeader(value = TOOL_KEY_HEADER, required = false) String toolKey,
        @RequestBody(required = false) PurchaseQuoteCompareRequest request)
    {
        if (!isAuthorized(toolKey))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false, "message", "工具鉴权失败", "data", Map.of()));
        }
        PurchaseQuoteCompareResult result = quoteService.compare(request);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("status", result.status());
        body.put("resultCode", result.resultCode());
        body.put("nextAction", result.nextAction());
        body.put("retryTool", false);
        body.put("agentInstruction", instruction(result));
        body.put("issues", result.issues());
        body.put("message", result.message());
        body.put("data", result);
        return ResponseEntity.ok(body);
    }

    /** 明确告诉 Agent 报价比较失败时如何处理，避免相同参数循环重试。 */
    private String instruction(PurchaseQuoteCompareResult result)
    {
        return switch (result.status())
        {
            case "READY" -> "请展示推荐供应商、可比单价、报价有效期、交货周期和其他候选项。"
                + "不要直接创建采购订单，等待用户确认后再调用采购订单准备工具。";
            case "NEED_INPUT" -> "不要使用相同参数重试。请根据 issues 中的字段名向用户补充信息。";
            case "NO_CANDIDATE" -> "当前没有符合条件的有效报价。请询问用户是否手工指定供应商和单价。";
            default -> "不要使用相同参数重试。请根据 issues 中的字段名修正请求。";
        };
    }

    /** 校验 Dify 专用工具密钥。 */
    private boolean isAuthorized(String toolKey)
    {
        String configuredKey = configService.selectConfigByKey(TOOL_KEY_CONFIG);
        return StringUtils.isNotBlank(configuredKey) && configuredKey.equals(toolKey);
    }
}
