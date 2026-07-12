package com.ruoyi.web.controller.agent;

import com.ruoyi.agent.business.automation.application.PurchaseOrderAutomationService;
import com.ruoyi.agent.business.automation.domain.CreatePurchaseOrderDraftRequest;
import com.ruoyi.agent.business.automation.domain.CreatePurchaseOrderDraftResult;
import com.ruoyi.agent.business.automation.domain.AutomationCandidate;
import com.ruoyi.agent.business.automation.domain.AutomationPreparationStatus;
import com.ruoyi.agent.business.automation.domain.AutomationToolIssue;
import com.ruoyi.agent.business.automation.domain.AutomationToolNextAction;
import com.ruoyi.agent.business.automation.domain.PurchaseOrderDraftRequest;
import com.ruoyi.agent.business.automation.domain.PurchaseOrderPreparationResult;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
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
        AutomationToolNextAction nextAction = resolveNextAction(result);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("status", result.status().name());
        body.put("resultCode", resolveResultCode(result));
        body.put("nextAction", nextAction.name());
        body.put("retryTool", false);
        body.put("agentInstruction", buildAgentInstruction(result, nextAction));
        body.put("issues", resolveIssues(result));
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

    /** 将业务准备状态映射为 Agent 可以直接执行的下一步动作。 */
    private AutomationToolNextAction resolveNextAction(PurchaseOrderPreparationResult result)
    {
        if (result.status() == AutomationPreparationStatus.AMBIGUOUS)
        {
            return AutomationToolNextAction.SELECT_CANDIDATE;
        }
        if (result.status() == AutomationPreparationStatus.READY)
        {
            return AutomationToolNextAction.SHOW_DRAFT;
        }
        return AutomationToolNextAction.ASK_USER;
    }

    /** 为 Agent 提供稳定的业务结果码，而不是仅依赖自然语言消息。 */
    private String resolveResultCode(PurchaseOrderPreparationResult result)
    {
        return switch (result.status())
        {
            case NEED_INPUT -> "MISSING_REQUIRED_FIELDS";
            case AMBIGUOUS -> "AMBIGUOUS_MASTER_DATA";
            case INVALID -> "BUSINESS_VALIDATION_FAILED";
            case READY -> "DRAFT_READY";
        };
    }

    /** 生成明确的 Agent 行为指令，阻止模型拿相同参数反复重试工具。 */
    private String buildAgentInstruction(PurchaseOrderPreparationResult result, AutomationToolNextAction nextAction)
    {
        return switch (nextAction)
        {
            case ASK_USER -> "不要再次调用 preparePurchaseOrderDraft。请向用户说明 issues 中的问题，"
                + "仅在用户补充或修正字段后，使用 issues.field 指定的实体字段名重新调用。";
            case SELECT_CANDIDATE -> "不要再次调用 preparePurchaseOrderDraft。请展示 candidates 中的候选项，"
                + "等待用户明确选择编码后再重新调用。";
            case SHOW_DRAFT -> "不要创建或审核采购订单。请简要说明草稿已就绪，"
                + "由 agent-ui 展示确认卡片并等待用户点击确认。";
        };
    }

    /** 将当前服务的校验结果转换成字段路径明确的工具问题列表。 */
    private List<AutomationToolIssue> resolveIssues(PurchaseOrderPreparationResult result)
    {
        List<AutomationToolIssue> issues = new ArrayList<>();
        for (String field : result.missingFields())
        {
            issues.add(missingFieldIssue(field));
        }
        if (result.status() == AutomationPreparationStatus.AMBIGUOUS)
        {
            for (AutomationCandidate candidate : result.candidates())
            {
                String field = "supplier".equals(candidate.field()) ? "supplierKeyword" : "lines[].materialKeyword";
                issues.add(new AutomationToolIssue("AMBIGUOUS_MASTER_DATA", field,
                    "存在多个候选项，请让用户按编码明确选择：" + candidate.keyword()));
            }
        }
        if (result.status() == AutomationPreparationStatus.INVALID && issues.isEmpty())
        {
            issues.add(invalidIssue(result.message()));
        }
        return issues;
    }

    /** 将常见主数据校验失败映射为模型可直接修正的字段。 */
    private AutomationToolIssue invalidIssue(String message)
    {
        if (message.startsWith("供应商"))
            return new AutomationToolIssue("SUPPLIER_DISABLED", "supplierKeyword", message);
        if (message.startsWith("物料") || message.startsWith("未找到启用物料"))
            return new AutomationToolIssue("MATERIAL_NOT_AVAILABLE", "lines[].materialKeyword", message);
        if (message.contains("日期"))
            return new AutomationToolIssue("INVALID_DATE", "orderDate", message);
        return new AutomationToolIssue("BUSINESS_VALIDATION_FAILED", "", message);
    }

    /** 将中文展示名映射为下一次调用时必须使用的 Java 实体字段路径。 */
    private AutomationToolIssue missingFieldIssue(String label)
    {
        if ("供应商".equals(label))
            return new AutomationToolIssue("MISSING_REQUIRED_FIELD", "supplierKeyword", "缺少供应商；必须传 supplierKeyword。");
        if ("订单日期".equals(label))
            return new AutomationToolIssue("MISSING_REQUIRED_FIELD", "orderDate", "缺少订单日期；必须传 orderDate，格式 yyyy-MM-dd。");
        if ("采购明细".equals(label))
            return new AutomationToolIssue("MISSING_REQUIRED_FIELD", "lines", "缺少采购明细；必须传 lines 数组。");
        int lineNumber = parseLineNumber(label);
        String prefix = lineNumber > 0 ? "lines[" + (lineNumber - 1) + "]." : "lines[].";
        if (label.endsWith("物料"))
            return new AutomationToolIssue("MISSING_REQUIRED_FIELD", prefix + "materialKeyword",
                "缺少物料；必须传 materialKeyword。");
        if (label.endsWith("采购数量"))
            return new AutomationToolIssue("MISSING_REQUIRED_FIELD", prefix + "quantity",
                "缺少采购数量；必须传 quantity，且大于 0。");
        if (label.endsWith("含税单价"))
            return new AutomationToolIssue("MISSING_REQUIRED_FIELD", prefix + "unitPrice",
                "缺少含税单价；必须传 unitPrice，不能使用中文字段名、taxPrice 或 price。");
        return new AutomationToolIssue("MISSING_REQUIRED_FIELD", "", "缺少字段：" + label);
    }

    /** 提取“第 N 行”中的行号，供返回 Java 字段路径使用。 */
    private int parseLineNumber(String label)
    {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("第\\s*(\\d+)\\s*行").matcher(label);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }
}
