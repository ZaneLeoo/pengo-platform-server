package com.ruoyi.mes.base.mapper;

import com.ruoyi.mes.base.domain.Material;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * 物料主数据访问接口。
 *
 * @author ruoyi
 */
public interface MaterialMapper {

    /**
     * 查询物料列表。
     *
     * @param material 物料
     * @return 物料集合
     */
    List<Material> selectMaterialList(Material material);

    /** 查询 Agent 工具使用的物料列表。 */
    List<Material> selectMaterialListForAgent(@Param("keyword") String keyword,
        @Param("categoryId") Long categoryId, @Param("materialType") String materialType,
        @Param("status") String status);

    /**
     * 根据物料ID查询物料。
     *
     * @param materialId 物料ID
     * @return 物料
     */
    Material selectMaterialById(Long materialId);

    /**
     * 根据物料编码查询物料。
     *
     * @param materialCode 物料编码
     * @return 物料
     */
    Material selectMaterialByCode(String materialCode);

    /**
     * 查询分类下物料数量。
     *
     * @param categoryId 分类ID
     * @return 物料数量
     */
    int countMaterialByCategoryId(Long categoryId);

    /**
     * 新增物料。
     *
     * @param material 物料
     * @return 结果
     */
    int insertMaterial(Material material);

    /**
     * 修改物料。
     *
     * @param material 物料
     * @return 结果
     */
    int updateMaterial(Material material);

    /**
     * 批量删除物料。
     *
     * @param materialIds 物料ID数组
     * @return 结果
     */
    int deleteMaterialByIds(Long[] materialIds);
}
