package com.ruoyi.web.controller.mes;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mes.common.enums.PurchaseDocumentStatus;
import com.ruoyi.mes.purchase.domain.PurchaseReceipt;
import com.ruoyi.mes.purchase.domain.dto.InspectionRequest;
import com.ruoyi.mes.purchase.mapper.PurchaseReceiptMapper;
import com.ruoyi.mes.purchase.service.IPurchaseFlowService;
import jakarta.validation.Valid;
import java.util.Arrays;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 采购到货单控制器。 */
@RestController
@RequestMapping("/mes/purchase/receipt")
public class PurchaseReceiptController extends BaseController {
    private static final String DRAFT = PurchaseDocumentStatus.DRAFT.getCode();

    private final PurchaseReceiptMapper receiptMapper;
    private final IPurchaseFlowService flowService;

    public PurchaseReceiptController(PurchaseReceiptMapper receiptMapper, IPurchaseFlowService flowService) {
        this.receiptMapper = receiptMapper;
        this.flowService = flowService;
    }

    /** 查询到货单列表。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseReceipt:list')")
    @GetMapping("/list")
    public TableDataInfo list(PurchaseReceipt query) {
        startPage();
        return getDataTable(receiptMapper.selectList(query));
    }

    /** 查询到货单及其明细。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseReceipt:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        PurchaseReceipt receipt = receiptMapper.selectById(id);
        if (receipt != null) {
            receipt.setLines(receiptMapper.selectLines(id));
        }
        return success(receipt);
    }

    /** 新增草稿到货单。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseReceipt:add')")
    @Log(title = "采购到货单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody PurchaseReceipt receipt) {
        receipt.setCreateBy(getUsername());
        receiptMapper.insert(receipt);
        saveLines(receipt);
        return success(receipt.getId());
    }

    /** 修改草稿到货单，并整体替换其明细。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseReceipt:edit')")
    @Log(title = "采购到货单", businessType = BusinessType.UPDATE)
    @Transactional(rollbackFor = Exception.class)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody PurchaseReceipt receipt) {
        PurchaseReceipt existing = receiptMapper.selectById(receipt.getId());
        if (existing == null) {
            return error("到货单不存在");
        }
        if (!DRAFT.equals(existing.getStatus())) {
            return error("已审核到货单不允许编辑");
        }
        receipt.setUpdateBy(getUsername());
        receiptMapper.update(receipt);
        receiptMapper.deleteLines(Arrays.asList(receipt.getId()));
        saveLines(receipt);
        return success();
    }

    /** 删除草稿到货单。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseReceipt:remove')")
    @Log(title = "采购到货单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        for (Long id : ids) {
            PurchaseReceipt existing = receiptMapper.selectById(id);
            if (existing != null && !DRAFT.equals(existing.getStatus())) {
                return error("已审核到货单不允许删除");
            }
        }
        receiptMapper.deleteLines(Arrays.asList(ids));
        return toAjax(receiptMapper.deleteByIds(ids));
    }

    /** 审核到货单并反写订单到货数量。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseReceipt:approve')")
    @Log(title = "采购到货单审核", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/approve")
    public AjaxResult approve(@PathVariable Long id) {
        flowService.approveReceipt(id, getUsername());
        return success();
    }

    /** 弃审尚未质检的到货单。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseReceipt:unapprove')")
    @Log(title = "采购到货单弃审", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/unapprove")
    public AjaxResult unapprove(@PathVariable Long id) {
        flowService.unapproveReceipt(id, getUsername());
        return success();
    }

    /** 提交到货单质检结果。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseReceipt:inspect')")
    @Log(title = "采购到货单质检", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/inspect")
    public AjaxResult inspect(@PathVariable Long id, @Valid @RequestBody InspectionRequest request) {
        flowService.inspectReceipt(id, request, getUsername());
        return success();
    }

    /** 反质检，以便修正质检结果。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseReceipt:inspect')")
    @Log(title = "采购到货单反质检", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/uninspect")
    public AjaxResult uninspect(@PathVariable Long id) {
        flowService.uninspectReceipt(id, getUsername());
        return success();
    }

    /** 查询可参照的已审核采购订单明细。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseReceipt:reference')")
    @GetMapping("/reference/order-lines")
    public AjaxResult referenceOrders(@RequestParam(required = false) String orderCode,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String materialCode) {
        return success(flowService.selectReceiptReferenceLines(orderCode, supplierName, materialCode));
    }

    /** 保存到货单所有明细。 */
    private void saveLines(PurchaseReceipt receipt) {
        receipt.getLines().forEach(line -> {
            line.setReceiptId(receipt.getId());
            line.setCreateBy(getUsername());
            receiptMapper.insertLine(line);
        });
    }
}
