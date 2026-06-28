package com.ruoyi.mes.base.domain;

import java.io.Serializable;

/**
 * BOM版本差异项。
 *
 * @author ruoyi
 */
public class BomVersionCompareItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String diffType;
    private String parentItemCode;
    private String componentItemCode;
    private String componentItemName;
    private String fieldName;
    private String fieldLabel;
    private String baseValue;
    private String targetValue;

    public String getDiffType() {
        return diffType;
    }

    public void setDiffType(String diffType) {
        this.diffType = diffType;
    }

    public String getParentItemCode() {
        return parentItemCode;
    }

    public void setParentItemCode(String parentItemCode) {
        this.parentItemCode = parentItemCode;
    }

    public String getComponentItemCode() {
        return componentItemCode;
    }

    public void setComponentItemCode(String componentItemCode) {
        this.componentItemCode = componentItemCode;
    }

    public String getComponentItemName() {
        return componentItemName;
    }

    public void setComponentItemName(String componentItemName) {
        this.componentItemName = componentItemName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldLabel() {
        return fieldLabel;
    }

    public void setFieldLabel(String fieldLabel) {
        this.fieldLabel = fieldLabel;
    }

    public String getBaseValue() {
        return baseValue;
    }

    public void setBaseValue(String baseValue) {
        this.baseValue = baseValue;
    }

    public String getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }
}
