package com.ruoyi.agent.tool.purchaseorder;

import com.ruoyi.agent.business.automation.application.PurchaseOrderAutomationService;
import com.ruoyi.agent.business.automation.domain.AutomationCandidate;
import com.ruoyi.agent.business.automation.domain.AutomationCandidateOption;
import com.ruoyi.agent.business.automation.domain.PurchaseOrderDraftRequest;
import com.ruoyi.agent.business.automation.domain.PurchaseOrderDraft;
import com.ruoyi.agent.business.automation.domain.PurchaseOrderPreparationResult;
import com.ruoyi.agent.tool.shared.AgentToolCandidate;
import com.ruoyi.agent.tool.shared.AgentToolIssue;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import com.ruoyi.agent.tool.shared.AgentToolResults;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/** 采购订单自动化业务的 Agent 工具适配层。 */
@Service
public class PurchaseOrderToolService {
    private static final Pattern LINE_NUMBER_PATTERN = Pattern.compile("第\\s*(\\d+)\\s*行");
    private final PurchaseOrderAutomationService automationService;

    public PurchaseOrderToolService(PurchaseOrderAutomationService automationService) {
        this.automationService = automationService;
    }

    /** 校验并准备采购订单草稿，绝不写入真实单据。 */
    public AgentToolResult<PurchaseOrderDraft> prepare(PurchaseOrderDraftRequest request) {
        PurchaseOrderPreparationResult result = automationService.prepare(request);
        return switch (result.getStatus()) {
            case NEED_INPUT -> AgentToolResults.needInput(PurchaseOrderToolResultCode.MISSING_REQUIRED_FIELDS,
                    result.getMessage(), issues(result), null);
            case AMBIGUOUS -> AgentToolResults.ambiguous(PurchaseOrderToolResultCode.AMBIGUOUS_MASTER_DATA,
                    result.getMessage(), issues(result), null);
            case INVALID -> AgentToolResults.rejected(PurchaseOrderToolResultCode.BUSINESS_VALIDATION_FAILED,
                    result.getMessage(), issues(result), null);
            case READY -> AgentToolResults.confirm(PurchaseOrderToolResultCode.PURCHASE_ORDER_DRAFT_READY,
                    result.getMessage(), result.getDraft(), "由 agent-ui 展示采购订单确认卡片并等待用户确认。");
        };
    }

    /** 把业务校验信息转换成字段路径明确的协议问题。 */
    private List<AgentToolIssue> issues(PurchaseOrderPreparationResult result) {
        List<AgentToolIssue> issues = new ArrayList<>();
        result.getMissingFields().forEach(field -> issues.add(missingFieldIssue(field)));
        result.getCandidates().forEach(candidate -> issues.add(candidateIssue(candidate)));
        if (issues.isEmpty() && result.getStatus().name().equals("INVALID")) {
            issues.add(invalidIssue(result.getMessage()));
        }
        return issues;
    }

    /** 转换需要用户选择的主数据候选项。 */
    private AgentToolIssue candidateIssue(AutomationCandidate candidate) {
        String field = "supplier".equals(candidate.getField()) ? "supplierKeyword" : "lines[].materialKeyword";
        List<AgentToolCandidate> options = candidate.getOptions().stream().map(this::toCandidate).toList();
        return new AgentToolIssue("AMBIGUOUS_MASTER_DATA", field, "存在多个候选项，请按编码明确选择",
                "传入选中候选项的 code", options);
    }

    /** 转换候选项的展示字段。 */
    private AgentToolCandidate toCandidate(AutomationCandidateOption option) {
        String description = String.join(" / ", nonBlank(option.getSpec(), option.getModel(), option.getUnit()));
        return new AgentToolCandidate(option.getCode(), option.getName(), description);
    }

    /** 返回常见业务失败对应的实体字段。 */
    private AgentToolIssue invalidIssue(String message) {
        if (message.startsWith("供应商"))
            return AgentToolIssue.of("SUPPLIER_DISABLED", "supplierKeyword", message, "启用供应商编码或名称");
        if (message.startsWith("物料") || message.startsWith("未找到启用物料"))
            return AgentToolIssue.of("MATERIAL_NOT_AVAILABLE", "lines[].materialKeyword", message, "启用物料编码或名称");
        if (message.contains("日期"))
            return AgentToolIssue.of("INVALID_DATE", "orderDate", message, "yyyy-MM-dd");
        return AgentToolIssue.of("BUSINESS_VALIDATION_FAILED", "", message, "符合采购业务规则的数据");
    }

    /** 将业务中文字段映射为下一次工具调用使用的 Java 字段路径。 */
    private AgentToolIssue missingFieldIssue(String label) {
        if ("供应商".equals(label))
            return AgentToolIssue.of("MISSING_REQUIRED_FIELD", "supplierKeyword", "缺少供应商", "供应商编码或名称");
        if ("订单日期".equals(label))
            return AgentToolIssue.of("MISSING_REQUIRED_FIELD", "orderDate", "缺少订单日期", "yyyy-MM-dd");
        if ("采购明细".equals(label))
            return AgentToolIssue.of("MISSING_REQUIRED_FIELD", "lines", "缺少采购明细", "非空数组");
        int lineNumber = parseLineNumber(label);
        String prefix = lineNumber > 0 ? "lines[" + (lineNumber - 1) + "]." : "lines[].";
        if (label.endsWith("物料"))
            return AgentToolIssue.of("MISSING_REQUIRED_FIELD", prefix + "materialKeyword", "缺少物料", "物料编码或名称");
        if (label.endsWith("采购数量"))
            return AgentToolIssue.of("MISSING_REQUIRED_FIELD", prefix + "quantity", "缺少采购数量", "大于 0 的数值");
        if (label.endsWith("含税单价"))
            return AgentToolIssue.of("MISSING_REQUIRED_FIELD", prefix + "unitPrice", "缺少含税单价", "大于等于 0 的数值");
        return AgentToolIssue.of("MISSING_REQUIRED_FIELD", "", "缺少字段：" + label, label);
    }

    /** 提取业务标签中的明细行号。 */
    private int parseLineNumber(String label) {
        Matcher matcher = LINE_NUMBER_PATTERN.matcher(label);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    /** 过滤空白候选描述。 */
    private List<String> nonBlank(String... values) {
        return java.util.Arrays.stream(values).filter(value -> value != null && !value.isBlank()).toList();
    }
}
