package com.ruoyi.mes.base.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;

/**
 * 物料/产品主数据 material。
 *
 * @author ruoyi
 */
public class Material extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 物料ID */
    private Long materialId;

    /** 物料编码 */
    @Excel(name = "物料编码")
    private String materialCode;

    /** 物料名称 */
    @Excel(name = "物料名称")
    private String materialName;

    /** 物料类型 */
    @Excel(name = "物料类型")
    private String materialType;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称 */
    @Excel(name = "物料分类")
    private String categoryName;

    /** 规格 */
    @Excel(name = "规格")
    private String spec;

    /** 型号 */
    @Excel(name = "型号")
    private String model;

    /** 主单位 */
    @Excel(name = "主单位")
    private String unit;

    /** 图号 */
    @Excel(name = "图号")
    private String drawingNo;

    /** 物料版本 */
    @Excel(name = "物料版本")
    private String materialVersion;

    /** 来源类型 */
    @Excel(name = "来源类型")
    private String sourceType;

    /** 是否批次管理 */
    @Excel(name = "批次管理", readConverterExp = "Y=是,N=否")
    private String lotControlFlag;

    /** 是否序列号管理 */
    @Excel(name = "序列号管理", readConverterExp = "Y=是,N=否")
    private String snControlFlag;

    /** 是否需要检验 */
    @Excel(name = "需要检验", readConverterExp = "Y=是,N=否")
    private String inspectionFlag;

    /** 安全库存 */
    @Excel(name = "安全库存")
    private BigDecimal safeStock;

    /** 默认BOM ID */
    private Long defaultBomId;

    /** 默认工艺路线ID */
    private Long defaultRouteId;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    @NotBlank(message = "物料编码不能为空")
    @Size(max = 64, message = "物料编码长度不能超过64个字符")
    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    @NotBlank(message = "物料名称不能为空")
    @Size(max = 100, message = "物料名称长度不能超过100个字符")
    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    @NotBlank(message = "物料类型不能为空")
    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @NotBlank(message = "主单位不能为空")
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDrawingNo() {
        return drawingNo;
    }

    public void setDrawingNo(String drawingNo) {
        this.drawingNo = drawingNo;
    }

    public String getMaterialVersion() {
        return materialVersion;
    }

    public void setMaterialVersion(String materialVersion) {
        this.materialVersion = materialVersion;
    }

    @NotBlank(message = "来源类型不能为空")
    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getLotControlFlag() {
        return lotControlFlag;
    }

    public void setLotControlFlag(String lotControlFlag) {
        this.lotControlFlag = lotControlFlag;
    }

    public String getSnControlFlag() {
        return snControlFlag;
    }

    public void setSnControlFlag(String snControlFlag) {
        this.snControlFlag = snControlFlag;
    }

    public String getInspectionFlag() {
        return inspectionFlag;
    }

    public void setInspectionFlag(String inspectionFlag) {
        this.inspectionFlag = inspectionFlag;
    }

    public BigDecimal getSafeStock() {
        return safeStock;
    }

    public void setSafeStock(BigDecimal safeStock) {
        this.safeStock = safeStock;
    }

    public Long getDefaultBomId() {
        return defaultBomId;
    }

    public void setDefaultBomId(Long defaultBomId) {
        this.defaultBomId = defaultBomId;
    }

    public Long getDefaultRouteId() {
        return defaultRouteId;
    }

    public void setDefaultRouteId(Long defaultRouteId) {
        this.defaultRouteId = defaultRouteId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("materialId", getMaterialId())
                .append("materialCode", getMaterialCode())
                .append("materialName", getMaterialName())
                .append("materialType", getMaterialType())
                .append("categoryId", getCategoryId())
                .append("spec", getSpec())
                .append("model", getModel())
                .append("unit", getUnit())
                .append("sourceType", getSourceType())
                .append("status", getStatus())
                .append("remark", getRemark())
                .toString();
    }
}
