package com.ruoyi.mes.base.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 计量单位主档 unit。
 *
 * @author ruoyi
 * @date 2026-07-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Unit extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 单位编码 */
    @NotBlank(message = "单位编码不能为空")
    @Size(max = 64, message = "单位编码长度不能超过64")
    @Excel(name = "单位编码")
    private String unitCode;

    /** 单位名称 */
    @NotBlank(message = "单位名称不能为空")
    @Size(max = 64, message = "单位名称长度不能超过64")
    @Excel(name = "单位名称")
    private String unitName;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;
}
