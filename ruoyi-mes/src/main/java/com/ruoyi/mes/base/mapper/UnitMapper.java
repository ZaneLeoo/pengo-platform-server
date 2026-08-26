package com.ruoyi.mes.base.mapper;

import com.ruoyi.mes.base.domain.Unit;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 计量单位主档数据访问接口。
 *
 * @author ruoyi
 * @date 2026-07-31
 */
@Mapper
public interface UnitMapper {

    List<Unit> selectUnitList(Unit unit);

    Unit selectUnitById(Long id);

    Unit selectUnitByCode(String unitCode);

    int insertUnit(Unit unit);

    int updateUnit(Unit unit);

    int deleteUnitByIds(Long[] ids);
}
