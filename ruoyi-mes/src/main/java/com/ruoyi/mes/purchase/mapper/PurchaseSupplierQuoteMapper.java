package com.ruoyi.mes.purchase.mapper;

import com.ruoyi.mes.purchase.domain.PurchaseSupplierQuote;
import com.ruoyi.mes.purchase.domain.PurchaseSupplierQuoteLine;
import com.ruoyi.mes.purchase.domain.dto.PurchaseQuoteCandidate;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 供应商报价数据访问接口。 */
public interface PurchaseSupplierQuoteMapper
{
    List<PurchaseSupplierQuote> selectList(PurchaseSupplierQuote query);

    PurchaseSupplierQuote selectById(Long id);

    PurchaseSupplierQuote selectByCode(String quoteCode);

    int insert(PurchaseSupplierQuote quote);

    int update(PurchaseSupplierQuote quote);

    int deleteByIds(Long[] ids);

    int deleteLinesByQuoteIds(Long[] ids);

    List<PurchaseSupplierQuoteLine> selectLinesByQuoteId(Long quoteId);

    int insertLine(PurchaseSupplierQuoteLine line);

    List<PurchaseQuoteCandidate> selectCandidates(@Param("materialCode") String materialCode,
                                                  @Param("quantity") java.math.BigDecimal quantity,
                                                  @Param("currency") String currency,
                                                  @Param("requiredDate") String requiredDate);

    int updateStatus(@Param("id") Long id, @Param("sourceStatus") String sourceStatus,
                     @Param("targetStatus") String targetStatus, @Param("operator") String operator);
}
