package com.ruoyi.mes.purchase.mapper;

import com.ruoyi.mes.purchase.domain.PurchaseInbound;
import com.ruoyi.mes.purchase.domain.PurchaseInboundLine;
import java.util.List;

/**
 * 采购入库单数据访问接口。
 */
public interface PurchaseInboundMapper {

    List<PurchaseInbound> selectList(PurchaseInbound q);

    PurchaseInbound selectById(Long id);

    int insert(PurchaseInbound o);

    int update(PurchaseInbound o);

    int deleteByIds(Long[] ids);

    List<PurchaseInboundLine> selectLines(Long inboundId);

    int insertLine(PurchaseInboundLine o);

    int deleteLines(List<Long> ids);
}
