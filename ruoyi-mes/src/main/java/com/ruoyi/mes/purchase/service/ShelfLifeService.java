package com.ruoyi.mes.purchase.service;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mes.base.domain.Material;
import com.ruoyi.mes.base.mapper.MaterialMapper;
import com.ruoyi.mes.purchase.domain.PurchaseInboundLine;
import com.ruoyi.mes.purchase.domain.PurchaseReceiptLine;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.springframework.stereotype.Service;

/** 负责采购收货、入库环节的保质期计算与业务校验。 */
@Service
public class ShelfLifeService {
    private final MaterialMapper materialMapper;

    public ShelfLifeService(MaterialMapper materialMapper) {
        this.materialMapper = materialMapper;
    }

    /** 保存草稿时，在信息充分的情况下补全并校验效期。 */
    public void prepareReceiptLine(PurchaseReceiptLine line) {
        Material material = requiredMaterial(line.getMaterialId());
        if (!"Y".equals(material.getShelfLifeControlFlag())) {
            return;
        }
        LocalDate productionDate = parseOptionalDate(line.getProductionDate(), "生产日期");
        LocalDate expiryDate = parseOptionalDate(line.getExpiryDate(), "有效期");
        if (expiryDate == null && productionDate != null) {
            expiryDate = productionDate.plusDays(material.getShelfLifeDays());
            line.setExpiryDate(expiryDate.toString());
        }
        if (productionDate != null && expiryDate != null) {
            validateDateOrder(line.getMaterialCode(), productionDate, expiryDate);
        }
    }

    /** 到货单审核前严格校验批次与保质期信息。 */
    public void validateReceiptLine(PurchaseReceiptLine line) {
        Material material = requiredMaterial(line.getMaterialId());
        boolean lotControlled = "Y".equals(material.getLotControlFlag());
        boolean shelfLifeControlled = "Y".equals(material.getShelfLifeControlFlag());
        if ((lotControlled || shelfLifeControlled) && isBlank(line.getLotNo())) {
            throw new ServiceException("受批次管理物料 " + line.getMaterialCode() + " 必须填写批次号后才能审核");
        }
        if (!shelfLifeControlled) {
            return;
        }
        if (isBlank(line.getProductionDate()) && isBlank(line.getExpiryDate())) {
            throw new ServiceException("保质期物料 " + line.getMaterialCode() + " 必须填写生产日期或有效期后才能审核");
        }
        LocalDate productionDate = parseOptionalDate(line.getProductionDate(), "生产日期");
        LocalDate expiryDate = parseOptionalDate(line.getExpiryDate(), "有效期");
        if (productionDate != null && expiryDate != null) {
            validateDateOrder(line.getMaterialCode(), productionDate, expiryDate);
        }
    }

    /** 校验入库明细已完整继承来源到货批次的效期信息。 */
    public void validateInboundLine(PurchaseInboundLine line) {
        Material material = requiredMaterial(line.getMaterialId());
        if (!"Y".equals(material.getShelfLifeControlFlag())) {
            return;
        }
        requireLotAndDate(line.getMaterialCode(), line.getLotNo(), line.getProductionDate(), line.getExpiryDate());
        LocalDate productionDate = parseOptionalDate(line.getProductionDate(), "生产日期");
        LocalDate expiryDate = parseOptionalDate(line.getExpiryDate(), "有效期");
        if (productionDate != null && expiryDate != null) {
            validateDateOrder(line.getMaterialCode(), productionDate, expiryDate);
        }
    }

    private Material requiredMaterial(Long materialId) {
        Material material = materialMapper.selectMaterialById(materialId);
        if (material == null) {
            throw new ServiceException("物料不存在或已被删除");
        }
        return material;
    }

    private void requireLotAndDate(String materialCode, String lotNo, String productionDate, String expiryDate) {
        if (isBlank(lotNo)) {
            throw new ServiceException("保质期物料 " + materialCode + " 必须填写批次号");
        }
        if ((productionDate == null || productionDate.isBlank()) && (expiryDate == null || expiryDate.isBlank())) {
            throw new ServiceException("保质期物料 " + materialCode + " 必须填写生产日期或有效期");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private LocalDate parseOptionalDate(String value, String fieldName) {
        return value == null || value.isBlank() ? null : parseDate(value, fieldName);
    }

    private LocalDate parseDate(String value, String fieldName) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new ServiceException(fieldName + "格式必须为 yyyy-MM-dd");
        }
    }

    private void validateDateOrder(String materialCode, LocalDate productionDate, LocalDate expiryDate) {
        if (expiryDate.isBefore(productionDate)) {
            throw new ServiceException("物料 " + materialCode + " 的有效期不能早于生产日期");
        }
    }
}
