package com.ruoyi.mes.base.service;

import com.ruoyi.mes.base.domain.Material;
import java.util.List;

/**
 * 物料主数据业务接口。
 *
 * @author ruoyi
 */
public interface IMaterialService {

    /**
     * 查询物料列表。
     *
     * @param material 物料
     * @return 物料集合
     */
    List<Material> selectMaterialList(Material material);

    /** 查询 Agent 工具使用的物料列表。 */
    List<Material> selectMaterialListForAgent(String keyword, Long categoryId, String materialType, String status);

    /**
     * 根据物料ID查询物料。
     *
     * @param materialId 物料ID
     * @return 物料
     */
    Material selectMaterialById(Long materialId);

    /**
     * 校验物料编码是否唯一。
     *
     * @param material 物料
     * @return true 唯一
     */
    boolean checkMaterialCodeUnique(Material material);

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
