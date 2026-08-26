package com.ruoyi.mes.base.engine;

import com.ruoyi.mes.base.domain.Material;
import com.ruoyi.mes.base.domain.UnitConversionFormula;
import com.ruoyi.mes.base.mapper.UnitConversionFormulaMapper;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 公式求值引擎。 负责：变量替换 + 三级作用域公式查找 + 调用 ArithmeticEvaluator 求值。
 *
 * @author ruoyi
 * @date 2026-07-30
 */
@Component
public class FormulaEngine {

    private static final Logger log = LoggerFactory.getLogger(FormulaEngine.class);
    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{(\\w+)\\}");
    private static final int DEFAULT_DECIMAL_SCALE = 4;
    private static final MathContext INTERNAL_CONTEXT = MathContext.DECIMAL128;

    @Autowired private UnitConversionFormulaMapper formulaMapper;

    private final ArithmeticEvaluator evaluator = new ArithmeticEvaluator();

    /**
     * 三级作用域查找公式：物料级 > 分类级 > 单位组级。
     *
     * @param unitGroupId 单位组ID
     * @param material 物料（含 categoryId）
     * @param fromUnit 源单位编码
     * @param toUnit 目标单位编码
     * @return 匹配到的公式，null 表示无公式（应使用固定换算率）
     */
    public UnitConversionFormula findFormula(
            Long unitGroupId, Material material, String fromUnit, String toUnit) {
        return findFormula(unitGroupId, material, fromUnit, toUnit, null);
    }

    /**
     * 查找公式，并支持单位明细显式绑定的公式作为单位组级别的补充来源。
     *
     * <p>物料级和分类级仍然优先，避免显式绑定覆盖更具体的业务规则。
     */
    public UnitConversionFormula findFormula(
            Long unitGroupId,
            Material material,
            String fromUnit,
            String toUnit,
            Long preferredFormulaId) {
        // 1. 物料级别（最高优先）
        UnitConversionFormula f =
                formulaMapper.selectByScope(
                        unitGroupId, "MATERIAL", material.getMaterialId(), fromUnit, toUnit);
        if (f != null) return f;

        // 2. 分类级别
        if (material.getCategoryId() != null) {
            f =
                    formulaMapper.selectByScope(
                            unitGroupId,
                            "CLASSIFICATION",
                            material.getCategoryId(),
                            fromUnit,
                            toUnit);
            if (f != null) return f;
        }

        // 3. 单位明细显式绑定的公式。允许同一单位组内配置多个同类公式。
        if (preferredFormulaId != null) {
            f = formulaMapper.selectFormulaById(preferredFormulaId);
            if (isUsableFormula(f, unitGroupId, fromUnit, toUnit)) return f;
        }

        // 4. 单位组级别（默认兜底）
        return formulaMapper.selectByScope(unitGroupId, "UNIT_GROUP", null, fromUnit, toUnit);
    }

    private boolean isUsableFormula(
            UnitConversionFormula formula, Long unitGroupId, String fromUnit, String toUnit) {
        return formula != null
                && unitGroupId != null
                && unitGroupId.equals(formula.getUnitGroupId())
                && Objects.equals(fromUnit, formula.getInputUnit())
                && Objects.equals(toUnit, formula.getOutputUnit())
                && (formula.getIsActive() == null || "Y".equalsIgnoreCase(formula.getIsActive()))
                && (formula.getStatus() == null || "0".equals(formula.getStatus()));
    }

    /**
     * 计算公式的动态换算率（正向：从 inputUnit 换到 outputUnit）。 例如公式 "1卷=长*宽*码数 平方米" 返回 rate=60，表示 1卷=60平方米。
     *
     * @param formula 公式定义
     * @param material 物料实体（提供 length/width/yards 等值）
     * @param runtimeOverrides 运行时覆盖值（如实际称重数据），可选
     * @return 换算率（表示 1个inputUnit = rate个outputUnit）
     */
    public BigDecimal evaluate(
            UnitConversionFormula formula,
            Material material,
            Map<String, BigDecimal> runtimeOverrides) {
        if (formula == null || formula.getExpression() == null) {
            log.warn("公式或表达式为空");
            return BigDecimal.ZERO;
        }

        String expr = formula.getExpression();
        log.debug("公式求值: formula={}, expression={}", formula.getFormulaCode(), expr);

        // 替换 ${paramName} 为实际值
        String resolved = replaceVariables(expr, formula, material, runtimeOverrides);
        log.debug("变量替换后: expression={}", resolved);

        return evaluator.evaluate(resolved);
    }

    /** 正向换算并按公式配置舍入输出数量。 */
    public BigDecimal forwardEvaluate(
            UnitConversionFormula formula,
            Material material,
            BigDecimal inputQty,
            Map<String, BigDecimal> overrides) {
        BigDecimal rate = evaluate(formula, material, overrides);
        if (rate == null || rate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return roundResult(inputQty.multiply(rate, INTERNAL_CONTEXT), formula);
    }

    /**
     * 反向计算：已知 output 端数量，反推 input 端数量。
     *
     * <p>根据 reverse_mode:
     *
     * <ul>
     *   <li>DIVIDE: quantity / rate（最常用）
     *   <li>MULTIPLY: quantity * rate
     *   <li>CUSTOM: 使用 reverse_expression 单独求值
     * </ul>
     *
     * @param formula 公式定义
     * @param material 物料
     * @param outputQty 目标单位数量（即已知量）
     * @param overrides 运行时覆盖
     * @return 源单位数量
     */
    public BigDecimal reverseEvaluate(
            UnitConversionFormula formula,
            Material material,
            BigDecimal outputQty,
            Map<String, BigDecimal> overrides) {
        String reverseMode =
                formula.getReverseMode() == null
                        ? "DIVIDE"
                        : formula.getReverseMode().toUpperCase(Locale.ROOT);
        BigDecimal result;
        if ("MULTIPLY".equals(reverseMode)) {
            BigDecimal rate = evaluate(formula, material, overrides);
            result = outputQty.multiply(rate, INTERNAL_CONTEXT);
        } else if ("CUSTOM".equals(reverseMode) && formula.getReverseExpression() != null) {
            String expr =
                    replaceVariables(formula.getReverseExpression(), formula, material, overrides);
            result = evaluator.evaluate(expr);
        } else {
            BigDecimal rate = evaluate(formula, material, overrides);
            if (rate.compareTo(BigDecimal.ZERO) == 0) {
                log.warn("反向换算时公式 rate=0, formula={}", formula.getFormulaCode());
                return BigDecimal.ZERO;
            }
            result = outputQty.divide(rate, INTERNAL_CONTEXT);
        }
        return roundResult(result, formula);
    }

    /** 公式输出的业务精度只在结果边界应用，表达式内部保持高精度。 */
    public BigDecimal roundResult(BigDecimal value, UnitConversionFormula formula) {
        if (value == null) {
            return null;
        }
        int scale =
                formula == null || formula.getDecimalScale() == null
                        ? DEFAULT_DECIMAL_SCALE
                        : formula.getDecimalScale();
        String modeText = formula == null ? null : formula.getRoundingMode();
        RoundingMode mode;
        try {
            mode =
                    RoundingMode.valueOf(
                            modeText == null ? "HALF_UP" : modeText.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            mode = RoundingMode.HALF_UP;
        }
        return value.setScale(scale, mode);
    }

    // ---- 变量替换 ----

    private String replaceVariables(
            String expression,
            UnitConversionFormula formula,
            Material material,
            Map<String, BigDecimal> overrides) {
        Matcher m = VAR_PATTERN.matcher(expression);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String varName = m.group(1);
            BigDecimal val = resolveVar(varName, formula, material, overrides);
            m.appendReplacement(sb, val.toPlainString());
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private BigDecimal resolveVar(
            String varName,
            UnitConversionFormula formula,
            Material material,
            Map<String, BigDecimal> overrides) {
        // 1. runtime overrides 优先
        if (overrides != null && overrides.containsKey(varName)) {
            return overrides.get(varName);
        }

        // 2. 查找公式参数映射：有 field 就读 Material 字段，否则用 default 常量
        BigDecimal paramResult = resolveParam(formula, varName, material);
        if (paramResult != null) {
            return paramResult;
        }

        // 3. 尝试直接从 Material 读取（如果变量名和字段名一致）
        return getMaterialField(material, varName);
    }

    /**
     * 根据参数名匹配公式配置的 paramX，按优先级返回： paramX_field 不为空 → 读 Material 字段；否则用 paramX_default 常量。 返回 null
     * 表示未匹配或值为 null。
     */
    private BigDecimal resolveParam(UnitConversionFormula f, String varName, Material material) {
        if (varName.equals(f.getParam1Name()))
            return resolveFieldOrDefault(f.getParam1Field(), f.getParam1Default(), material);
        if (varName.equals(f.getParam2Name()))
            return resolveFieldOrDefault(f.getParam2Field(), f.getParam2Default(), material);
        if (varName.equals(f.getParam3Name()))
            return resolveFieldOrDefault(f.getParam3Field(), f.getParam3Default(), material);
        if (varName.equals(f.getParam4Name()))
            return resolveFieldOrDefault(f.getParam4Field(), f.getParam4Default(), material);
        if (varName.equals(f.getParam5Name()))
            return resolveFieldOrDefault(f.getParam5Field(), f.getParam5Default(), material);
        return null;
    }

    /** field 不为空 → 反射 Material；否则用 defaultValue */
    private BigDecimal resolveFieldOrDefault(
            String fieldName, BigDecimal defaultValue, Material material) {
        if (fieldName != null && !fieldName.isBlank()) {
            BigDecimal val = getMaterialField(material, fieldName);
            // Material 字段返回 0（即没有配置）→ 回退到 default
            if (val != null && val.compareTo(BigDecimal.ZERO) != 0) {
                return val;
            }
            if (defaultValue != null) {
                return defaultValue;
            }
            return val; // 返回 0 或 null
        }
        return defaultValue;
    }

    /** 反射从 Material 取值，null 返回 0 */
    private BigDecimal getMaterialField(Material material, String fieldName) {
        if (material == null || fieldName == null) return BigDecimal.ZERO;
        try {
            Field field = Material.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object val = field.get(material);
            if (val instanceof BigDecimal) return (BigDecimal) val;
            if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue());
            if (val instanceof String) {
                try {
                    return new BigDecimal((String) val);
                } catch (Exception e) {
                    /* ignore */
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.debug("无法从Material读取字段: {}", fieldName);
        }
        log.warn("Material字段 {} 为空，使用默认值0", fieldName);
        return BigDecimal.ZERO;
    }
}
