package com.ruoyi.mes.base.mapper;

import com.ruoyi.mes.base.domain.UnitGroupDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 计量单位组明细数据访问接口。
 *
 * @author ruoyi
 * @date 2026-07-30
 */
@Mapper
public interface UnitGroupDetailMapper {

    /** 按单位组ID查所有明细 */
    List<UnitGroupDetail> selectByGroupId(@Param("groupId") Long groupId);

    /** 按单位编码和组ID查明细 */
    UnitGroupDetail selectByGroupAndUnit(@Param("groupId") Long groupId,
                                          @Param("unitCode") String unitCode);

    List<UnitGroupDetail> selectDetailList(UnitGroupDetail detail);

    UnitGroupDetail selectDetailById(Long id);

    int insertDetail(UnitGroupDetail detail);

    int updateDetail(UnitGroupDetail detail);

    int deleteDetailByIds(Long[] ids);

    /** 按单位编码统计单位组明细引用数量。 */
    int countByUnitCode(@Param("unitCode") String unitCode);

    /** 按公式ID统计单位组明细引用数量。 */
    int countByFormulaIds(@Param("ids") Long[] ids);

    /** 按组ID批量删除 */
    int deleteByGroupId(@Param("groupId") Long groupId);
}
