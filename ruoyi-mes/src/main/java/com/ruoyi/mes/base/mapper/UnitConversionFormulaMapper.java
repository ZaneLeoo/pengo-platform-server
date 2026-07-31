package com.ruoyi.mes.base.mapper;

import com.ruoyi.mes.base.domain.UnitConversionFormula;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 计量单位换算公式数据访问接口。
 *
 * @author ruoyi
 * @date 2026-07-30
 */
public interface UnitConversionFormulaMapper {

    /**
     * 三级作用域查找公式：按作用域类型+作用域ID精确匹配。
     * <p>调用方按优先级依次尝试 MATERIAL → CLASSIFICATION → UNIT_GROUP。</p>
     *
     * @param unitGroupId 单位组ID
     * @param scopeType   作用域类型
     * @param scopeId     作用域ID（CLASSIFICATION时传categoryId，MATERIAL时传materialId，UNIT_GROUP时传null）
     * @param fromUnit    源单位编码
     * @param toUnit      目标单位编码
     * @return 匹配的公式，null 表示不存在
     */
    UnitConversionFormula selectByScope(@Param("unitGroupId") Long unitGroupId,
                                         @Param("scopeType") String scopeType,
                                         @Param("scopeId") Long scopeId,
                                         @Param("fromUnit") String fromUnit,
                                         @Param("toUnit") String toUnit);

    /** 查询公式列表 */
    List<UnitConversionFormula> selectFormulaList(UnitConversionFormula formula);

    /** 按ID查询 */
    UnitConversionFormula selectFormulaById(Long id);

    /** 新增 */
    int insertFormula(UnitConversionFormula formula);

    /** 修改 */
    int updateFormula(UnitConversionFormula formula);

    /** 批量删除 */
    int deleteFormulaByIds(Long[] ids);
}
