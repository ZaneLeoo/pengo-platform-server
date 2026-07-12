package com.ruoyi.mes.base.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 供应商档案。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Supplier extends BaseEntity {

    private Long id;

    @NotBlank(message = "供应商编码不能为空")
    private String supplierCode;

    @NotBlank(message = "供应商名称不能为空")
    private String supplierName;

    private String contactPerson;

    private String contactPhone;

    private String address;

    private String currency;

    private BigDecimal taxRate;

    @NotBlank(message = "状态不能为空")
    private String status;
}
