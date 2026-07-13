package com.ruoyi.web.controller.mes;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mes.purchase.domain.PurchaseSupplierQuote;
import com.ruoyi.mes.purchase.service.IPurchaseSupplierQuoteService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 供应商报价单控制器。 */
@RestController
@RequestMapping("/mes/purchase/quote")
public class PurchaseSupplierQuoteController extends BaseController {
    private final IPurchaseSupplierQuoteService quoteService;

    public PurchaseSupplierQuoteController(IPurchaseSupplierQuoteService quoteService) {
        this.quoteService = quoteService;
    }

    /** 查询供应商报价列表。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseQuote:list')")
    @GetMapping("/list")
    public TableDataInfo list(PurchaseSupplierQuote query) {
        startPage();
        return getDataTable(quoteService.selectList(query));
    }

    /** 查询供应商报价详情及明细。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseQuote:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(quoteService.selectById(id));
    }

    /** 新增供应商报价草稿。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseQuote:add')")
    @Log(title = "供应商报价", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody PurchaseSupplierQuote quote) {
        if (!quoteService.checkQuoteCodeUnique(quote))
            return error("报价单号已存在");
        quote.setCreateBy(getUsername());
        return toAjax(quoteService.insert(quote));
    }

    /** 修改供应商报价草稿。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseQuote:edit')")
    @Log(title = "供应商报价", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody PurchaseSupplierQuote quote) {
        if (!quoteService.checkQuoteCodeUnique(quote))
            return error("报价单号已存在");
        quote.setUpdateBy(getUsername());
        return toAjax(quoteService.update(quote));
    }

    /** 删除供应商报价草稿。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseQuote:remove')")
    @Log(title = "供应商报价", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(quoteService.deleteByIds(ids));
    }

    /** 审核供应商报价。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseQuote:approve')")
    @Log(title = "供应商报价审核", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/approve")
    public AjaxResult approve(@PathVariable Long id) {
        quoteService.approve(id, getUsername());
        return success();
    }

    /** 弃审供应商报价。 */
    @PreAuthorize("@ss.hasPermi('mes:purchaseQuote:unapprove')")
    @Log(title = "供应商报价弃审", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/unapprove")
    public AjaxResult unapprove(@PathVariable Long id) {
        quoteService.unapprove(id, getUsername());
        return success();
    }
}
