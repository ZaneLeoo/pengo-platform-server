package com.ruoyi.mes.base.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 库位档案。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Location extends BaseEntity {

    private Long id;

    @NotBlank(message = "库位编码不能为空")
    private String locationCode;

    @NotBlank(message = "库位名称不能为空")
    private String locationName;

    @NotNull(message = "所属仓库不能为空")
    private Long warehouseId;

    @NotBlank(message = "所属仓库编码不能为空")
    private String warehouseCode;

    @NotBlank(message = "所属仓库名称不能为空")
    private String warehouseName;

    @NotBlank(message = "状态不能为空")
    private String status;
}
