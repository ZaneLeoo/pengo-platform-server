package com.ruoyi.mes.base.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Date;

/**
 * BOM版本 bom_version。
 *
 * @author ruoyi
 */
public class BomVersion extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long bomMasterId;
    @Excel(name = "版本号")
    private String versionCode;
    @Excel(name = "版本名称")
    private String versionName;
    private String versionDesc;
    @Excel(name = "基准数量")
    private BigDecimal baseQty;
    @Excel(name = "用途")
    private String usageType;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date effectiveDate;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expireDate;
    @Excel(name = "版本状态")
    private String status;
    @Excel(name = "审批状态")
    private String approveStatus;
    @Excel(name = "默认版本")
    private Integer defaultFlag;
    private String defaultRoutingCode;
    private String defaultRoutingName;
    private Long defaultRoutingVersionId;
    private String sourceSystem;
    private String sourceId;
    private String checkBy;
    private Date checkTime;
    private String closeBy;
    private Date closeTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @NotNull(message = "BOM主表ID不能为空")
    public Long getBomMasterId() { return bomMasterId; }
    public void setBomMasterId(Long bomMasterId) { this.bomMasterId = bomMasterId; }

    @NotBlank(message = "版本号不能为空")
    public String getVersionCode() { return versionCode; }
    public void setVersionCode(String versionCode) { this.versionCode = versionCode; }

    public String getVersionName() { return versionName; }
    public void setVersionName(String versionName) { this.versionName = versionName; }

    public String getVersionDesc() { return versionDesc; }
    public void setVersionDesc(String versionDesc) { this.versionDesc = versionDesc; }

    @NotNull(message = "基准数量不能为空")
    public BigDecimal getBaseQty() { return baseQty; }
    public void setBaseQty(BigDecimal baseQty) { this.baseQty = baseQty; }

    @NotBlank(message = "用途不能为空")
    public String getUsageType() { return usageType; }
    public void setUsageType(String usageType) { this.usageType = usageType; }

    public Date getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(Date effectiveDate) { this.effectiveDate = effectiveDate; }

    public Date getExpireDate() { return expireDate; }
    public void setExpireDate(Date expireDate) { this.expireDate = expireDate; }

    @NotBlank(message = "版本状态不能为空")
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @NotBlank(message = "审批状态不能为空")
    public String getApproveStatus() { return approveStatus; }
    public void setApproveStatus(String approveStatus) { this.approveStatus = approveStatus; }

    @NotNull(message = "是否默认不能为空")
    public Integer getDefaultFlag() { return defaultFlag; }
    public void setDefaultFlag(Integer defaultFlag) { this.defaultFlag = defaultFlag; }

    public String getDefaultRoutingCode() { return defaultRoutingCode; }
    public void setDefaultRoutingCode(String defaultRoutingCode) { this.defaultRoutingCode = defaultRoutingCode; }
    public String getDefaultRoutingName() { return defaultRoutingName; }
    public void setDefaultRoutingName(String defaultRoutingName) { this.defaultRoutingName = defaultRoutingName; }
    public Long getDefaultRoutingVersionId() { return defaultRoutingVersionId; }
    public void setDefaultRoutingVersionId(Long defaultRoutingVersionId) { this.defaultRoutingVersionId = defaultRoutingVersionId; }
    public String getSourceSystem() { return sourceSystem; }
    public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public String getCheckBy() { return checkBy; }
    public void setCheckBy(String checkBy) { this.checkBy = checkBy; }
    public Date getCheckTime() { return checkTime; }
    public void setCheckTime(Date checkTime) { this.checkTime = checkTime; }
    public String getCloseBy() { return closeBy; }
    public void setCloseBy(String closeBy) { this.closeBy = closeBy; }
    public Date getCloseTime() { return closeTime; }
    public void setCloseTime(Date closeTime) { this.closeTime = closeTime; }
}
