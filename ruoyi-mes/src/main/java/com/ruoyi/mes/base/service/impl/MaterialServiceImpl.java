package com.ruoyi.mes.base.service.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.base.domain.Material;
import com.ruoyi.mes.base.mapper.MaterialMapper;
import com.ruoyi.mes.base.service.IMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 物料主数据业务处理。
 *
 * @author ruoyi
 */
@Service
public class MaterialServiceImpl implements IMaterialService {

    @Autowired
    private MaterialMapper materialMapper;

    /**
     * 查询物料列表。
     *
     * @param material 物料
     * @return 物料集合
     */
    @Override
    public List<Material> selectMaterialList(Material material) {
        return materialMapper.selectMaterialList(material);
    }

    /** 查询 Agent 工具使用的物料列表。 */
    @Override
    public List<Material> selectMaterialListForAgent(String keyword, Long categoryId, String materialType, String status) {
        return materialMapper.selectMaterialListForAgent(keyword, categoryId, materialType, status);
    }

    /**
     * 根据物料ID查询物料。
     *
     * @param materialId 物料ID
     * @return 物料
     */
    @Override
    public Material selectMaterialById(Long materialId) {
        return materialMapper.selectMaterialById(materialId);
    }

    /**
     * 校验物料编码是否唯一。
     *
     * @param material 物料
     * @return true 唯一
     */
    @Override
    public boolean checkMaterialCodeUnique(Material material) {
        Long materialId = StringUtils.isNull(material.getMaterialId()) ? -1L : material.getMaterialId();
        Material info = materialMapper.selectMaterialByCode(material.getMaterialCode());
        return StringUtils.isNull(info) || info.getMaterialId().longValue() == materialId.longValue();
    }

    /**
     * 新增物料。
     *
     * @param material 物料
     * @return 结果
     */
    @Override
    public int insertMaterial(Material material) {
        return materialMapper.insertMaterial(material);
    }

    /**
     * 修改物料。
     *
     * @param material 物料
     * @return 结果
     */
    @Override
    public int updateMaterial(Material material) {
        return materialMapper.updateMaterial(material);
    }

    /**
     * 批量删除物料。
     *
     * @param materialIds 物料ID数组
     * @return 结果
     */
    @Override
    public int deleteMaterialByIds(Long[] materialIds) {
        return materialMapper.deleteMaterialByIds(materialIds);
    }
}
