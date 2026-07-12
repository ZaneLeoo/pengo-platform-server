package com.ruoyi.mes.base.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 仓库档案。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Warehouse extends BaseEntity {

    private Long id;

    @NotBlank(message = "仓库编码不能为空")
    private String warehouseCode;

    @NotBlank(message = "仓库名称不能为空")
    private String warehouseName;

    private String address;

    private String manager;

    @NotBlank(message = "状态不能为空")
    private String status;
}
