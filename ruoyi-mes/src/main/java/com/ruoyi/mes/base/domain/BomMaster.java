package com.ruoyi.mes.base.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * BOM主表 bom_master。
 *
 * @author ruoyi
 */
public class BomMaster extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** BOM编码 */
    @Excel(name = "BOM编码")
    private String bomCode;

    /** 母件物料ID */
    private Long parentItemId;

    /** 母件编码 */
    @Excel(name = "母件编码")
    private String parentItemCode;

    /** 母件名称 */
    @Excel(name = "母件名称")
    private String parentItemName;

    /** 母件规格 */
    @Excel(name = "母件规格")
    private String parentItemSpec;

    /** 母件单位 */
    @Excel(name = "母件单位")
    private String parentItemUnit;

    /** BOM类型 */
    @Excel(name = "BOM类型")
    private String bomType;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    /** 来源系统 */
    @Excel(name = "来源系统")
    private String sourceSystem;

    /** 外部ID */
    private String sourceId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @NotBlank(message = "BOM编码不能为空")
    @Size(max = 64, message = "BOM编码长度不能超过64个字符")
    public String getBomCode() { return bomCode; }
    public void setBomCode(String bomCode) { this.bomCode = bomCode; }

    public Long getParentItemId() { return parentItemId; }
    public void setParentItemId(Long parentItemId) { this.parentItemId = parentItemId; }

    @NotBlank(message = "母件编码不能为空")
    public String getParentItemCode() { return parentItemCode; }
    public void setParentItemCode(String parentItemCode) { this.parentItemCode = parentItemCode; }

    @NotBlank(message = "母件名称不能为空")
    public String getParentItemName() { return parentItemName; }
    public void setParentItemName(String parentItemName) { this.parentItemName = parentItemName; }

    public String getParentItemSpec() { return parentItemSpec; }
    public void setParentItemSpec(String parentItemSpec) { this.parentItemSpec = parentItemSpec; }

    public String getParentItemUnit() { return parentItemUnit; }
    public void setParentItemUnit(String parentItemUnit) { this.parentItemUnit = parentItemUnit; }

    @NotBlank(message = "BOM类型不能为空")
    public String getBomType() { return bomType; }
    public void setBomType(String bomType) { this.bomType = bomType; }

    @NotBlank(message = "状态不能为空")
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSourceSystem() { return sourceSystem; }
    public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
}
