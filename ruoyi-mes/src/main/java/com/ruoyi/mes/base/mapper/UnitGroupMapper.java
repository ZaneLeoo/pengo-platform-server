package com.ruoyi.mes.base.mapper;

import com.ruoyi.mes.base.domain.UnitGroup;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 计量单位组数据访问接口。
 *
 * @author ruoyi
 * @date 2026-07-30
 */
@Mapper
public interface UnitGroupMapper {

    List<UnitGroup> selectUnitGroupList(UnitGroup unitGroup);

    UnitGroup selectUnitGroupById(Long id);

    UnitGroup selectUnitGroupByCode(String groupCode);

    int insertUnitGroup(UnitGroup unitGroup);

    int updateUnitGroup(UnitGroup unitGroup);

    int deleteUnitGroupByIds(Long[] ids);
}
