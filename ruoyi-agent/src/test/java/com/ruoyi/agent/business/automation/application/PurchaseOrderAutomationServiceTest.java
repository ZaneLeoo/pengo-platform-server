package com.ruoyi.agent.business.automation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ruoyi.agent.business.automation.domain.AutomationAction;
import com.ruoyi.agent.business.automation.domain.AutomationPreparationStatus;
import com.ruoyi.agent.business.automation.domain.CreatePurchaseOrderDraftRequest;
import com.ruoyi.agent.business.automation.domain.CreatePurchaseOrderDraftResult;
import com.ruoyi.agent.business.automation.domain.PurchaseOrderDraftLineRequest;
import com.ruoyi.agent.business.automation.domain.PurchaseOrderDraftRequest;
import com.ruoyi.agent.business.automation.domain.PurchaseOrderPreparationResult;
import com.ruoyi.agent.business.automation.mapper.AutomationActionMapper;
import com.ruoyi.mes.base.domain.Material;
import com.ruoyi.mes.base.domain.Supplier;
import com.ruoyi.mes.base.service.IMaterialService;
import com.ruoyi.mes.base.service.ISupplierService;
import com.ruoyi.mes.purchase.domain.PurchaseOrder;
import com.ruoyi.mes.purchase.service.IPurchaseOrderService;
import com.ruoyi.mes.purchase.service.IPurchaseSupplierQuoteService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** 采购订单自动化服务的核心业务规则测试。 */
@ExtendWith(MockitoExtension.class)
class PurchaseOrderAutomationServiceTest
{
    @Mock
    private ISupplierService supplierService;
    @Mock
    private IMaterialService materialService;
    @Mock
    private IPurchaseOrderService purchaseOrderService;
    @Mock
    private AutomationActionMapper actionMapper;
    @Mock
    private IPurchaseSupplierQuoteService quoteService;

    private PurchaseOrderAutomationService service;

    @BeforeEach
    void setUp()
    {
        service = new PurchaseOrderAutomationService(supplierService, materialService, purchaseOrderService, actionMapper,
            quoteService);
    }

    @Test
    void shouldPrepareNormalizedDraftFromMasterData()
    {
        Supplier supplier = supplier("SUP001", "深圳鸿发电子科技有限公司", new BigDecimal("13"));
        Material material = material(10L, "PCB-CTRL", "PCB控制板", "块");
        when(supplierService.selectList(any())).thenReturn(List.of(supplier));
        when(materialService.selectMaterialListForAgent(anyString(), any(), any(), anyString())).thenReturn(List.of(material));

        PurchaseOrderPreparationResult result = service.prepare(new PurchaseOrderDraftRequest("SUP001", "2026-07-12",
            "2026-07-20", "生产补料", List.of(new PurchaseOrderDraftLineRequest("PCB-CTRL", new BigDecimal("2"),
                new BigDecimal("25.50"), "2026-07-20"))));

        assertThat(result.status()).isEqualTo(AutomationPreparationStatus.READY);
        assertThat(result.draft().supplierName()).isEqualTo("深圳鸿发电子科技有限公司");
        assertThat(result.draft().totalAmount()).isEqualByComparingTo("51.00");
        assertThat(result.draft().lines().get(0).materialId()).isEqualTo(10L);
        assertThat(result.draft().lines().get(0).taxRate()).isEqualByComparingTo("13");
    }

    @Test
    void shouldRequestMissingLineFieldsInsteadOfGuessing()
    {
        PurchaseOrderPreparationResult result = service.prepare(new PurchaseOrderDraftRequest("SUP001", "2026-07-12",
            null, null, List.of(new PurchaseOrderDraftLineRequest("", null, null, null))));

        assertThat(result.status()).isEqualTo(AutomationPreparationStatus.NEED_INPUT);
        assertThat(result.missingFields()).contains("第 1 行物料", "第 1 行采购数量", "第 1 行含税单价");
    }

    @Test
    void shouldDefaultOrderDateToToday()
    {
        Supplier supplier = supplier("SUP001", "深圳鸿发电子科技有限公司", new BigDecimal("13"));
        Material material = material(10L, "PCB-CTRL", "PCB控制板", "块");
        when(supplierService.selectList(any())).thenReturn(List.of(supplier));
        when(materialService.selectMaterialListForAgent(anyString(), any(), any(), anyString())).thenReturn(List.of(material));

        PurchaseOrderPreparationResult result = service.prepare(new PurchaseOrderDraftRequest("SUP001", null,
            null, null, List.of(new PurchaseOrderDraftLineRequest("PCB-CTRL", BigDecimal.ONE, BigDecimal.TEN, null))));

        assertThat(result.status()).isEqualTo(AutomationPreparationStatus.READY);
        assertThat(result.draft().orderDate()).isEqualTo(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
    }

    @Test
    void shouldCreateDraftOnceAndReuseCompletedAction()
    {
        Supplier supplier = supplier("SUP001", "深圳鸿发电子科技有限公司", new BigDecimal("13"));
        Material material = material(10L, "PCB-CTRL", "PCB控制板", "块");
        when(supplierService.selectList(any())).thenReturn(List.of(supplier));
        when(materialService.selectMaterialListForAgent(anyString(), any(), any(), anyString())).thenReturn(List.of(material));
        when(actionMapper.selectByActionKey("request-001")).thenReturn(null);
        doAnswer(invocation -> {
            PurchaseOrder order = invocation.getArgument(0);
            order.setId(99L);
            return 1;
        }).when(purchaseOrderService).insertPurchaseOrder(any(PurchaseOrder.class));

        PurchaseOrderPreparationResult prepared = service.prepare(new PurchaseOrderDraftRequest("SUP001", "2026-07-12",
            null, null, List.of(new PurchaseOrderDraftLineRequest("PCB-CTRL", BigDecimal.ONE, BigDecimal.TEN, null))));
        CreatePurchaseOrderDraftResult created = service.createDraft(
            new CreatePurchaseOrderDraftRequest("request-001", prepared.draft()), 1L, "admin");

        ArgumentCaptor<PurchaseOrder> orderCaptor = ArgumentCaptor.forClass(PurchaseOrder.class);
        verify(purchaseOrderService).insertPurchaseOrder(orderCaptor.capture());
        assertThat(created.duplicated()).isFalse();
        assertThat(created.orderId()).isEqualTo(99L);
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo("DRAFT");
        assertThat(orderCaptor.getValue().getCreateBy()).isEqualTo("admin");
        assertThat(orderCaptor.getValue().getLines().get(0).getMaterialName()).isEqualTo("PCB控制板");
        assertThat(orderCaptor.getValue().getLines().get(0).getCreateBy()).isEqualTo("admin");
        verify(actionMapper).complete("request-001", 99L, created.orderCode());
    }

    private Supplier supplier(String code, String name, BigDecimal taxRate)
    {
        Supplier value = new Supplier();
        value.setId(1L);
        value.setSupplierCode(code);
        value.setSupplierName(name);
        value.setCurrency("CNY");
        value.setTaxRate(taxRate);
        value.setStatus("NORMAL");
        return value;
    }

    private Material material(Long id, String code, String name, String unit)
    {
        Material value = new Material();
        value.setMaterialId(id);
        value.setMaterialCode(code);
        value.setMaterialName(name);
        value.setUnit(unit);
        value.setStatus("0");
        return value;
    }
}
