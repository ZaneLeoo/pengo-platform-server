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

    /** 根据物料配置补全并校验到货批次的效期信息。 */
    public void prepareReceiptLine(PurchaseReceiptLine line) {
        Material material = requiredMaterial(line.getMaterialId());
        if (!"Y".equals(material.getShelfLifeControlFlag())) {
            return;
        }
        requireLotAndDate(line.getMaterialCode(), line.getLotNo(), line.getProductionDate(), line.getExpiryDate());
        LocalDate productionDate = parseOptionalDate(line.getProductionDate(), "生产日期");
        LocalDate expiryDate = parseOptionalDate(line.getExpiryDate(), "有效期");
        if (expiryDate == null) {
            if (productionDate == null) {
                throw new ServiceException("保质期物料 " + line.getMaterialCode() + " 缺少生产日期，无法计算有效期");
            }
            expiryDate = productionDate.plusDays(material.getShelfLifeDays());
            line.setExpiryDate(expiryDate.toString());
        }
        if (productionDate != null) {
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
        if (lotNo == null || lotNo.isBlank()) {
            throw new ServiceException("保质期物料 " + materialCode + " 必须填写批次号");
        }
        if ((productionDate == null || productionDate.isBlank()) && (expiryDate == null || expiryDate.isBlank())) {
            throw new ServiceException("保质期物料 " + materialCode + " 必须填写生产日期或有效期");
        }
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
