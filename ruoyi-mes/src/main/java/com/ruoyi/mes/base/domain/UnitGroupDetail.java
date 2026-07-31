package com.ruoyi.mes.base.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 计量单位组明细 unit_group_detail。
 *
 * @author ruoyi
 * @date 2026-07-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UnitGroupDetail extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 单位组ID */
    private Long groupId;

    /** 单位编码 */
    @NotBlank(message = "单位编码不能为空")
    @Size(max = 64)
    @Excel(name = "单位编码")
    private String unitCode;

    /** 单位名称 */
    @NotBlank(message = "单位名称不能为空")
    @Size(max = 64)
    @Excel(name = "单位名称")
    private String unitName;

    /** 该单位作为源单位时绑定的换算公式，可为空 */
    private Long formulaId;

    /** 排序号 */
    private Integer sortOrder;
}
