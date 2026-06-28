package com.ruoyi.mes.base.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * BOM版本对比结果。
 *
 * @author ruoyi
 */
public class BomVersionCompareResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long baseVersionId;
    private String baseVersionCode;
    private Long targetVersionId;
    private String targetVersionCode;
    private List<BomVersionCompareItem> differences = new ArrayList<>();

    public Long getBaseVersionId() {
        return baseVersionId;
    }

    public void setBaseVersionId(Long baseVersionId) {
        this.baseVersionId = baseVersionId;
    }

    public String getBaseVersionCode() {
        return baseVersionCode;
    }

    public void setBaseVersionCode(String baseVersionCode) {
        this.baseVersionCode = baseVersionCode;
    }

    public Long getTargetVersionId() {
        return targetVersionId;
    }

    public void setTargetVersionId(Long targetVersionId) {
        this.targetVersionId = targetVersionId;
    }

    public String getTargetVersionCode() {
        return targetVersionCode;
    }

    public void setTargetVersionCode(String targetVersionCode) {
        this.targetVersionCode = targetVersionCode;
    }

    public List<BomVersionCompareItem> getDifferences() {
        return differences;
    }

    public void setDifferences(List<BomVersionCompareItem> differences) {
        this.differences = differences;
    }

    public void addDifference(BomVersionCompareItem item) {
        this.differences.add(item);
    }
}
