package com.ruoyi.mes.base.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 计量单位换算公式配置 unit_conversion_formula。
 *
 * @author ruoyi
 * @date 2026-07-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UnitConversionFormula extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 公式编码 */
    @NotBlank(message = "公式编码不能为空")
    @Size(max = 64)
    @Excel(name = "公式编码")
    private String formulaCode;

    /** 公式名称 */
    @NotBlank(message = "公式名称不能为空")
    @Size(max = 128)
    @Excel(name = "公式名称")
    private String formulaName;

    /** 公式类型: FIXED_RATE/AREA_TIMES_YARDS/WEIGHT_FROM_DIMS/CARTON_PRICE/CARDBOARD_PRICE/CUSTOM */
    @Excel(name = "公式类型")
    private String formulaType;

    /** 表达式模板: ${length} * ${width} * ${yards} */
    @NotBlank(message = "表达式不能为空")
    @Size(max = 512)
    @Excel(name = "表达式")
    private String expression;

    /** 所属计量单位组ID */
    private Long unitGroupId;

    /** 源单位编码 */
    @NotBlank(message = "源单位不能为空")
    private String inputUnit;

    /** 目标单位编码 */
    @NotBlank(message = "目标单位不能为空")
    private String outputUnit;

    /** 作用域: UNIT_GROUP/CLASSIFICATION/MATERIAL */
    @Excel(name = "作用域类型")
    private String scopeType;

    /** 作用域ID（分类ID 或 物料ID） */
    private Long scopeId;

    /** 反向模式: DIVIDE/MULTIPLY/CUSTOM */
    @Excel(name = "反向模式")
    private String reverseMode;

    /** 公式输出结果的小数位数，范围0-6 */
    @Excel(name = "小数位数")
    private Integer decimalScale;

    /** 公式输出结果的舍入模式: HALF_UP/DOWN/UP */
    @Excel(name = "舍入模式")
    private String roundingMode;

    /** 反向公式（仅CUSTOM时使用） */
    @Size(max = 512)
    private String reverseExpression;

    /** 参数1变量名 */
    private String param1Name;
    /** 参数1映射字段: length/width/height/weight/yards/standard_weight_per_sqm */
    private String param1Field;
    /** 参数1默认常量（field为空时使用） */
    private java.math.BigDecimal param1Default;

    private String param2Name;
    private String param2Field;
    private java.math.BigDecimal param2Default;
    private String param3Name;
    private String param3Field;
    private java.math.BigDecimal param3Default;
    private String param4Name;
    private String param4Field;
    private java.math.BigDecimal param4Default;
    private String param5Name;
    private String param5Field;
    private java.math.BigDecimal param5Default;

    /** 是否启用(Y/N) */
    @Excel(name = "是否启用", readConverterExp = "Y=是,N=否")
    private String isActive;

    /** 排序号 */
    private Integer sortOrder;

    /** 状态（0正常 1停用） */
    private String status;
}
