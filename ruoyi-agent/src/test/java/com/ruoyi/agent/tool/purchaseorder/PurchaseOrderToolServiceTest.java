package com.ruoyi.agent.tool.purchaseorder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ruoyi.agent.business.automation.application.PurchaseOrderAutomationService;
import com.ruoyi.agent.business.automation.domain.AutomationPreparationStatus;
import com.ruoyi.agent.business.automation.domain.PurchaseOrderDraft;
import com.ruoyi.agent.business.automation.domain.PurchaseOrderDraftRequest;
import com.ruoyi.agent.business.automation.domain.PurchaseOrderPreparationResult;
import com.ruoyi.agent.tool.shared.AgentToolNextAction;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import com.ruoyi.agent.tool.shared.AgentToolStatus;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 采购订单工具适配协议测试。 */
class PurchaseOrderToolServiceTest
{
    @Test
    void readyDraftShouldPreserveLegacyDataAndRequireConfirmation()
    {
        PurchaseOrderAutomationService automationService = mock(PurchaseOrderAutomationService.class);
        PurchaseOrderToolService toolService = new PurchaseOrderToolService(automationService);
        PurchaseOrderDraftRequest request = mock(PurchaseOrderDraftRequest.class);
        PurchaseOrderDraft draft = new PurchaseOrderDraft("SUP001", "测试供应商", "CNY", "2026-07-13",
            null, null, BigDecimal.ONE, BigDecimal.TEN, List.of());
        PurchaseOrderPreparationResult prepared = new PurchaseOrderPreparationResult(
            AutomationPreparationStatus.READY, "采购订单草稿已准备", List.of(), List.of(), draft);
        when(automationService.prepare(request)).thenReturn(prepared);

        AgentToolResult<PurchaseOrderDraft> result = toolService.prepare(request);

        assertEquals(AgentToolStatus.SUCCESS, result.status());
        assertEquals(AgentToolNextAction.CONFIRM_ACTION, result.nextAction());
        assertEquals("PURCHASE_ORDER_DRAFT_READY", result.resultCode());
        assertSame(draft, result.data());
    }

    @Test
    void missingFieldShouldExposeJavaFieldPath()
    {
        PurchaseOrderAutomationService automationService = mock(PurchaseOrderAutomationService.class);
        PurchaseOrderToolService toolService = new PurchaseOrderToolService(automationService);
        PurchaseOrderPreparationResult prepared = new PurchaseOrderPreparationResult(
            AutomationPreparationStatus.NEED_INPUT, "信息不完整", List.of("第 1 行含税单价"), List.of(), null);
        when(automationService.prepare(null)).thenReturn(prepared);

        AgentToolResult<PurchaseOrderDraft> result = toolService.prepare(null);

        assertEquals(AgentToolStatus.NEED_INPUT, result.status());
        assertEquals("lines[0].unitPrice", result.issues().get(0).field());
        assertTrue(result.agentInstruction().contains("不要用相同参数重试"));
    }
}
