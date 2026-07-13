package com.ruoyi.mes.purchase.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 供应商报价主表。报价只有审核后才能参与 AI 推荐。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseSupplierQuote extends BaseEntity {

    private Long id;

    @NotBlank(message = "报价单号不能为空")
    private String quoteCode;

    @NotNull(message = "供应商不能为空")
    private Long supplierId;

    @NotBlank(message = "供应商编码不能为空")
    private String supplierCode;

    @NotBlank(message = "供应商名称不能为空")
    private String supplierName;

    @NotNull(message = "报价日期不能为空")
    private LocalDate quoteDate;

    @NotBlank(message = "币种不能为空")
    private String currency;

    /** Y 表示报价单价已经含税，N 表示单价未税。 */
    @NotBlank(message = "含税标识不能为空")
    private String taxIncluded;

    @NotBlank(message = "报价状态不能为空")
    /** 查询条件为空时保持 null，新增时由业务服务显式设置为 DRAFT。 */
    private String status;

    @NotNull(message = "生效日期不能为空")
    private LocalDate effectiveDate;

    private LocalDate expireDate;

    @NotBlank(message = "报价来源不能为空")
    private String sourceType;

    private String sourceReference;

    private String approvedBy;

    private java.util.Date approvedTime;

    @Valid
    @NotEmpty(message = "报价单至少需要一条明细")
    private List<PurchaseSupplierQuoteLine> lines;
}
