package com.ruoyi.mes.base.service.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.exception.ServiceException;
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
     * @param material
     *            物料
     * @return 物料集合
     */
    @Override
    public List<Material> selectMaterialList(Material material) {
        return materialMapper.selectMaterialList(material);
    }

    /** 查询 Agent 工具使用的物料列表。 */
    @Override
    public List<Material> selectMaterialListForAgent(String keyword, Long categoryId, String materialType,
            String status) {
        return materialMapper.selectMaterialListForAgent(keyword, categoryId, materialType, status);
    }

    /**
     * 根据物料ID查询物料。
     *
     * @param materialId
     *            物料ID
     * @return 物料
     */
    @Override
    public Material selectMaterialById(Long materialId) {
        return materialMapper.selectMaterialById(materialId);
    }

    /**
     * 校验物料编码是否唯一。
     *
     * @param material
     *            物料
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
     * @param material
     *            物料
     * @return 结果
     */
    @Override
    public int insertMaterial(Material material) {
        validateShelfLife(material);
        return materialMapper.insertMaterial(material);
    }

    /**
     * 修改物料。
     *
     * @param material
     *            物料
     * @return 结果
     */
    @Override
    public int updateMaterial(Material material) {
        validateShelfLife(material);
        return materialMapper.updateMaterial(material);
    }

    /**
     * 批量删除物料。
     *
     * @param materialIds
     *            物料ID数组
     * @return 结果
     */
    @Override
    public int deleteMaterialByIds(Long[] materialIds) {
        return materialMapper.deleteMaterialByIds(materialIds);
    }

    /** 校验物料保质期配置。 */
    private void validateShelfLife(Material material) {
        if (!"Y".equals(material.getShelfLifeControlFlag())) {
            material.setShelfLifeControlFlag("N");
            material.setShelfLifeDays(null);
            material.setExpiryWarningDays(null);
            return;
        }
        if (!"Y".equals(material.getLotControlFlag())) {
            throw new ServiceException("启用保质期管理的物料必须同时启用批次管理");
        }
        if (material.getShelfLifeDays() == null || material.getShelfLifeDays() <= 0) {
            throw new ServiceException("启用保质期管理后必须填写大于0的保质期天数");
        }
        if (material.getExpiryWarningDays() == null) {
            material.setExpiryWarningDays(30);
        }
    }
}
