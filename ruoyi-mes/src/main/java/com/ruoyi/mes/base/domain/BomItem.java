package com.ruoyi.mes.base.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * BOM子件明细 bom_item。
 *
 * @author ruoyi
 */
public class BomItem extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long bomVersionId;
    @Excel(name = "行号")
    private Integer lineNo;
    private String parentItemCode;
    private Long componentItemId;
    @Excel(name = "子件编码")
    private String componentItemCode;
    @Excel(name = "子件名称")
    private String componentItemName;
    private String componentItemSpec;
    private String componentItemUnit;
    private String componentAttribute;
    @Excel(name = "子件用量")
    private BigDecimal componentQty;
    private BigDecimal fixedLossQty;
    private BigDecimal changeLossRate;
    private BigDecimal length;
    private BigDecimal width;
    private String supplyType;
    private Integer isVirtual;
    private Integer mrpExpandFlag;
    private String sourceSystem;

    /** 是否有下级BOM（自制/委外件且已创建BOM主数据）—— 仅查询填充，不入库 */
    private Boolean hasChildBom;

    /** 子件引用的BOM版本ID，为空则取默认版本 */
    private Long componentBomVersionId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    @NotNull(message = "BOM版本ID不能为空")
    public Long getBomVersionId() { return bomVersionId; }
    public void setBomVersionId(Long bomVersionId) { this.bomVersionId = bomVersionId; }
    @NotNull(message = "行号不能为空")
    public Integer getLineNo() { return lineNo; }
    public void setLineNo(Integer lineNo) { this.lineNo = lineNo; }
    public String getParentItemCode() { return parentItemCode; }
    public void setParentItemCode(String parentItemCode) { this.parentItemCode = parentItemCode; }
    @NotNull(message = "子件物料不能为空")
    public Long getComponentItemId() { return componentItemId; }
    public void setComponentItemId(Long componentItemId) { this.componentItemId = componentItemId; }
    @NotBlank(message = "子件编码不能为空")
    public String getComponentItemCode() { return componentItemCode; }
    public void setComponentItemCode(String componentItemCode) { this.componentItemCode = componentItemCode; }
    @NotBlank(message = "子件名称不能为空")
    public String getComponentItemName() { return componentItemName; }
    public void setComponentItemName(String componentItemName) { this.componentItemName = componentItemName; }
    public String getComponentItemSpec() { return componentItemSpec; }
    public void setComponentItemSpec(String componentItemSpec) { this.componentItemSpec = componentItemSpec; }
    public String getComponentItemUnit() { return componentItemUnit; }
    public void setComponentItemUnit(String componentItemUnit) { this.componentItemUnit = componentItemUnit; }
    public String getComponentAttribute() { return componentAttribute; }
    public void setComponentAttribute(String componentAttribute) { this.componentAttribute = componentAttribute; }
    @NotNull(message = "子件用量不能为空")
    @DecimalMin(value = "0.000001", message = "子件用量必须大于0")
    public BigDecimal getComponentQty() { return componentQty; }
    public void setComponentQty(BigDecimal componentQty) { this.componentQty = componentQty; }
    @DecimalMin(value = "0", message = "固定损耗不能小于0")
    public BigDecimal getFixedLossQty() { return fixedLossQty; }
    public void setFixedLossQty(BigDecimal fixedLossQty) { this.fixedLossQty = fixedLossQty; }
    @DecimalMin(value = "0", message = "变动损耗率不能小于0")
    @DecimalMax(value = "100", message = "变动损耗率不能大于100")
    public BigDecimal getChangeLossRate() { return changeLossRate; }
    public void setChangeLossRate(BigDecimal changeLossRate) { this.changeLossRate = changeLossRate; }
    public BigDecimal getLength() { return length; }
    public void setLength(BigDecimal length) { this.length = length; }
    public BigDecimal getWidth() { return width; }
    public void setWidth(BigDecimal width) { this.width = width; }
    @NotBlank(message = "发料方式不能为空")
    public String getSupplyType() { return supplyType; }
    public void setSupplyType(String supplyType) { this.supplyType = supplyType; }
    @NotNull(message = "是否虚拟件不能为空")
    public Integer getIsVirtual() { return isVirtual; }
    public void setIsVirtual(Integer isVirtual) { this.isVirtual = isVirtual; }
    @NotNull(message = "是否MRP展开不能为空")
    public Integer getMrpExpandFlag() { return mrpExpandFlag; }
    public void setMrpExpandFlag(Integer mrpExpandFlag) { this.mrpExpandFlag = mrpExpandFlag; }
    public String getSourceSystem() { return sourceSystem; }
    public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }

    public Boolean getHasChildBom() { return hasChildBom; }
    public void setHasChildBom(Boolean hasChildBom) { this.hasChildBom = hasChildBom; }

    public Long getComponentBomVersionId() { return componentBomVersionId; }
    public void setComponentBomVersionId(Long componentBomVersionId) { this.componentBomVersionId = componentBomVersionId; }
}
