package com.ruoyi.mes.purchase.mapper;

import com.ruoyi.mes.purchase.domain.PurchaseReceipt;
import com.ruoyi.mes.purchase.domain.PurchaseReceiptLine;
import java.util.List;

/**
 * 采购到货单数据访问接口。
 */
public interface PurchaseReceiptMapper {

    List<PurchaseReceipt> selectList(PurchaseReceipt q);

    PurchaseReceipt selectById(Long id);

    int insert(PurchaseReceipt o);

    int update(PurchaseReceipt o);

    int deleteByIds(Long[] ids);

    List<PurchaseReceiptLine> selectLines(Long receiptId);

    int insertLine(PurchaseReceiptLine o);

    int deleteLines(List<Long> ids);
}
