package com.ruoyi.mes.purchase.mapper;

import com.ruoyi.mes.purchase.domain.InventoryBalance;
import java.util.List;

public interface InventoryBalanceMapper {
    List<InventoryBalance> selectList(InventoryBalance q);

    InventoryBalance selectById(Long id);

    int insert(InventoryBalance o);

    int update(InventoryBalance o);

    int deleteByIds(Long[] ids);
}
