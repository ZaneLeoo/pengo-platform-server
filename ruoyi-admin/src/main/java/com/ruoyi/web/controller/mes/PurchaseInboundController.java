package com.ruoyi.web.controller.mes;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mes.common.enums.PurchaseDocumentStatus;
import com.ruoyi.mes.purchase.domain.PurchaseInbound;
import com.ruoyi.mes.purchase.mapper.PurchaseInboundMapper;
import com.ruoyi.mes.purchase.service.IPurchaseFlowService;
import com.ruoyi.mes.purchase.service.ShelfLifeService;
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

/** 采购入库单控制器。 */
@RestController
@RequestMapping("/mes/purchase/inbound")
public class PurchaseInboundController extends BaseController {
    private static final String DRAFT = PurchaseDocumentStatus.DRAFT.getCode();

    private final PurchaseInboundMapper inboundMapper;
    private final IPurchaseFlowService flowService;
    private final ShelfLifeService shelfLifeService;

    public PurchaseInboundController(PurchaseInboundMapper inboundMapper, IPurchaseFlowService flowService,
            ShelfLifeService shelfLifeService) {
        this.inboundMapper = inboundMapper;
        this.flowService = flowService;
        this.shelfLifeService = shelfLifeService;
    }

    /** 查询入库单列表。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseInbound:list')")
    @GetMapping("/list")
    public TableDataInfo list(PurchaseInbound query) {
        startPage();
        return getDataTable(inboundMapper.selectList(query));
    }

    /** 查询入库单及其明细。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseInbound:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        PurchaseInbound inbound = inboundMapper.selectById(id);
        if (inbound != null) {
            inbound.setLines(inboundMapper.selectLines(id));
        }
        return success(inbound);
    }

    /** 新增草稿入库单。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseInbound:add')")
    @Log(title = "采购入库单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody PurchaseInbound inbound) {
        inbound.setCreateBy(getUsername());
        inboundMapper.insert(inbound);
        saveLines(inbound);
        return success(inbound.getId());
    }

    /** 修改草稿入库单，并整体替换其明细。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseInbound:edit')")
    @Log(title = "采购入库单", businessType = BusinessType.UPDATE)
    @Transactional(rollbackFor = Exception.class)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody PurchaseInbound inbound) {
        PurchaseInbound existing = inboundMapper.selectById(inbound.getId());
        if (existing == null) {
            return error("入库单不存在");
        }
        if (!DRAFT.equals(existing.getStatus())) {
            return error("已审核入库单不允许编辑");
        }
        inbound.setUpdateBy(getUsername());
        inboundMapper.update(inbound);
        inboundMapper.deleteLines(Arrays.asList(inbound.getId()));
        saveLines(inbound);
        return success();
    }

    /** 删除草稿入库单。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseInbound:remove')")
    @Log(title = "采购入库单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        for (Long id : ids) {
            PurchaseInbound existing = inboundMapper.selectById(id);
            if (existing != null && !DRAFT.equals(existing.getStatus())) {
                return error("已审核入库单不允许删除");
            }
        }
        inboundMapper.deleteLines(Arrays.asList(ids));
        return toAjax(inboundMapper.deleteByIds(ids));
    }

    /** 审核入库单、更新库存余额并记录库存流水。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseInbound:approve')")
    @Log(title = "采购入库单审核", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/approve")
    public AjaxResult approve(@PathVariable Long id) {
        flowService.approveInbound(id, getUsername());
        return success();
    }

    /** 弃审入库单并冲回库存余额。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseInbound:unapprove')")
    @Log(title = "采购入库单弃审", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/unapprove")
    public AjaxResult unapprove(@PathVariable Long id) {
        flowService.unapproveInbound(id, getUsername());
        return success();
    }

    /** 查询可参照的已质检到货明细。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseInbound:reference')")
    @GetMapping("/reference/receipt-lines")
    public AjaxResult referenceReceipts(@RequestParam(required = false) String receiptCode,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String materialCode) {
        return success(flowService.selectInboundReferenceLines(receiptCode, warehouseCode, materialCode));
    }

    /** 保存入库单所有明细。 */
    private void saveLines(PurchaseInbound inbound) {
        inbound.getLines().forEach(line -> {
            shelfLifeService.validateInboundLine(line);
            line.setInboundId(inbound.getId());
            line.setCreateBy(getUsername());
            inboundMapper.insertLine(line);
        });
    }
}
