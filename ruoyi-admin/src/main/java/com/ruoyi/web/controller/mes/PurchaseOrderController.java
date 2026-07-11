package com.ruoyi.web.controller.mes;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mes.common.enums.PurchaseDocumentStatus;
import com.ruoyi.mes.purchase.domain.PurchaseOrder;
import com.ruoyi.mes.purchase.service.IPurchaseOrderService;
import com.ruoyi.mes.purchase.service.IPurchaseFlowService;
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

/** 采购订单控制器。 */
@RestController
@RequestMapping("/mes/purchase/order")
public class PurchaseOrderController extends BaseController
{
    private final IPurchaseOrderService orderService;
    private final IPurchaseFlowService flowService;

    public PurchaseOrderController(IPurchaseOrderService orderService, IPurchaseFlowService flowService)
    {
        this.orderService = orderService;
        this.flowService = flowService;
    }

    /** 查询采购订单列表。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseOrder:list')")
    @GetMapping("/list")
    public TableDataInfo list(PurchaseOrder order)
    {
        startPage();
        return getDataTable(orderService.selectPurchaseOrderList(order));
    }

    /** 查询采购订单详情及明细。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseOrder:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(orderService.selectPurchaseOrderById(id));
    }

    /** 新增采购订单。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseOrder:add')")
    @Log(title = "采购订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PurchaseOrder order)
    {
        if (!orderService.checkOrderCodeUnique(order)) return error("采购订单编号已存在");
        order.setCreateBy(getUsername());
        return toAjax(orderService.insertPurchaseOrder(order));
    }

    /** 修改采购订单。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseOrder:edit')")
    @Log(title = "采购订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody PurchaseOrder order)
    {
        PurchaseOrder existing = orderService.selectPurchaseOrderById(order.getId());
        if (existing == null) return error("采购订单不存在");
        if (!PurchaseDocumentStatus.DRAFT.getCode().equals(existing.getStatus())) return error("已审核采购订单不允许编辑");
        if (!orderService.checkOrderCodeUnique(order)) return error("采购订单编号已存在");
        order.setUpdateBy(getUsername());
        return toAjax(orderService.updatePurchaseOrder(order));
    }

    /** 删除采购订单。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseOrder:remove')")
    @Log(title = "采购订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        for (Long id : ids) {
            PurchaseOrder existing = orderService.selectPurchaseOrderById(id);
            if (existing != null && !PurchaseDocumentStatus.DRAFT.getCode().equals(existing.getStatus())) return error("已审核采购订单不允许删除");
        }
        return toAjax(orderService.deletePurchaseOrderByIds(ids));
    }

    /** 审核采购订单。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseOrder:approve')")
    @Log(title = "采购订单审核", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/approve")
    public AjaxResult approve(@PathVariable Long id)
    {
        flowService.approveOrder(id, getUsername());
        return success();
    }

    /** 弃审采购订单。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseOrder:unapprove')")
    @Log(title = "采购订单弃审", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/unapprove")
    public AjaxResult unapprove(@PathVariable Long id)
    {
        flowService.unapproveOrder(id, getUsername());
        return success();
    }
}
