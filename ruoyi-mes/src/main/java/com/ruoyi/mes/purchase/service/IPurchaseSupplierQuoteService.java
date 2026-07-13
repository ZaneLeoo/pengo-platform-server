package com.ruoyi.mes.purchase.service;

import com.ruoyi.mes.purchase.domain.PurchaseSupplierQuote;
import com.ruoyi.mes.purchase.domain.dto.PurchaseQuoteCompareRequest;
import com.ruoyi.mes.purchase.domain.dto.PurchaseQuoteCompareResult;
import java.util.List;

/** 供应商报价业务接口。 */
public interface IPurchaseSupplierQuoteService {
    List<PurchaseSupplierQuote> selectList(PurchaseSupplierQuote query);

    PurchaseSupplierQuote selectById(Long id);

    boolean checkQuoteCodeUnique(PurchaseSupplierQuote quote);

    int insert(PurchaseSupplierQuote quote);

    int update(PurchaseSupplierQuote quote);

    int deleteByIds(Long[] ids);

    void approve(Long id, String operator);

    void unapprove(Long id, String operator);

    /** 查询当前有效报价并按可比价格返回候选项。 */
    PurchaseQuoteCompareResult compare(PurchaseQuoteCompareRequest request);

    /** 校验采购订单草稿引用的报价仍有效，避免 AI 使用过期或被篡改的单价。 */
    boolean validateSelection(Long quoteId, Long quoteLineId, String supplierCode, String materialCode,
            java.math.BigDecimal quantity, java.math.BigDecimal orderUnitPrice);
}
