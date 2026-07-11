package com.ruoyi.web.controller.mes;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mes.purchase.domain.PurchaseReceipt;
import com.ruoyi.mes.purchase.mapper.PurchaseReceiptMapper;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;

@RestController
@RequestMapping("/mes/purchase/receipt")
public class PurchaseReceiptController extends BaseController {
    private final PurchaseReceiptMapper mapper;
    public PurchaseReceiptController(PurchaseReceiptMapper mapper) { this.mapper = mapper; }
    @PreAuthorize("@ss.hasPermi('mes:purchaseReceipt:list')") @GetMapping("/list") public TableDataInfo list(PurchaseReceipt q) { startPage(); return getDataTable(mapper.selectList(q)); }
    @PreAuthorize("@ss.hasPermi('mes:purchaseReceipt:query')") @GetMapping("/{id}") public AjaxResult get(@PathVariable Long id) { PurchaseReceipt o=mapper.selectById(id); if(o!=null)o.setLines(mapper.selectLines(id)); return success(o); }
    @PreAuthorize("@ss.hasPermi('mes:purchaseReceipt:add')") @Log(title="采购到货",businessType=BusinessType.INSERT) @PostMapping public AjaxResult add(@Valid @RequestBody PurchaseReceipt o) { o.setCreateBy(getUsername()); mapper.insert(o); if(o.getLines()!=null)o.getLines().forEach(x->{x.setReceiptId(o.getId());mapper.insertLine(x);}); return success(o.getId()); }
    @PreAuthorize("@ss.hasPermi('mes:purchaseReceipt:edit')") @Log(title="采购到货",businessType=BusinessType.UPDATE) @PutMapping public AjaxResult edit(@Valid @RequestBody PurchaseReceipt o) { o.setUpdateBy(getUsername()); return toAjax(mapper.update(o)); }
    @PreAuthorize("@ss.hasPermi('mes:purchaseReceipt:remove')") @DeleteMapping("/{ids}") public AjaxResult remove(@PathVariable Long[] ids) { mapper.deleteLines(Arrays.asList(ids)); return toAjax(mapper.deleteByIds(ids)); }
}
