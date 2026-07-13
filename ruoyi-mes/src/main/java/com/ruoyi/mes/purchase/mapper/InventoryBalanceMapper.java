package com.ruoyi.mes.purchase.mapper;
import java.util.List;
import com.ruoyi.mes.purchase.domain.InventoryBalance;
public interface InventoryBalanceMapper {
    List<InventoryBalance> selectList(InventoryBalance q);
    InventoryBalance selectById(Long id);
    int insert(InventoryBalance o);
    int update(InventoryBalance o);
    int deleteByIds(Long[] ids);
}
