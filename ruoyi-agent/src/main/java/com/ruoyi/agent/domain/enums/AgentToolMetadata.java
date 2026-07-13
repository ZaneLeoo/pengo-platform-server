package com.ruoyi.agent.domain.enums;

import java.util.Arrays;

/**
 * Agent 工具展示元数据。
 *
 * <p>operationId 是机器标识，label/description 是前端展示和模型提示使用的中文信息。
 * Dify 自定义 OpenAPI 工具可能只返回 operationId，因此这里作为稳定兜底，不使用数据字典存储。</p>
 */
public enum AgentToolMetadata
{
    QUERY_MATERIALS("queryMaterials", "查询物料", "根据物料编码、名称、规格、分类或物料类型查询物料。"),
    QUERY_MATERIAL_CATEGORIES("queryMaterialCategories", "查询物料分类", "查询物料分类及各级分类信息。"),
    QUERY_SUPPLIERS("querySuppliers", "查询供应商", "查询供应商编码、名称、币种和启用状态。"),
    QUERY_INVENTORY_BALANCES("queryInventoryBalances", "查询当前库存余额", "查询物料在仓库或货位的当前可用库存。"),
    QUERY_INVENTORY_TRANSACTIONS("queryInventoryTransactions", "查询库存变动流水", "查询采购入库、领料和其他库存变动流水。"),
    PREPARE_PURCHASE_ORDER("preparePurchaseOrderDraft", "准备采购订单草稿", "校验采购订单信息并准备待用户确认的草稿。"),
    COMPARE_PURCHASE_QUOTES("comparePurchaseSupplierQuotes", "比较供应商当前有效报价", "根据物料、数量和有效期比较供应商报价。"),
    BAR_CHART("bar_chart", "生成柱状图", "根据业务数据生成柱状图。"),
    LINE_CHART("line_chart", "生成折线图", "根据业务数据生成折线图。"),
    PIE_CHART("pie_chart", "生成饼图", "根据业务数据生成饼图。"),
    UNKNOWN("", "", "");

    private final String operationId;
    private final String label;
    private final String description;

    AgentToolMetadata(String operationId, String label, String description)
    {
        this.operationId = operationId;
        this.label = label;
        this.description = description;
    }

    public String getOperationId() { return operationId; }
    public String getLabel() { return label; }
    public String getDescription() { return description; }

    /** 按 Dify operationId 查找工具元数据。 */
    public static AgentToolMetadata fromOperationId(String operationId)
    {
        if (operationId == null || operationId.isBlank()) return UNKNOWN;
        return Arrays.stream(values())
            .filter(item -> item != UNKNOWN && item.operationId.equals(operationId))
            .findFirst()
            .orElse(UNKNOWN);
    }
}
