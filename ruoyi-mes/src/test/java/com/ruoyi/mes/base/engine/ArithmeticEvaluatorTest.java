package com.ruoyi.mes.base.engine;

import com.ruoyi.mes.base.domain.UnitConversionFormula;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArithmeticEvaluatorTest {

    private final ArithmeticEvaluator evaluator = new ArithmeticEvaluator();
    private final FormulaEngine formulaEngine = new FormulaEngine();

    @Test
    void divisionUsesInternalPrecisionInsteadOfFixedTenDigits() {
        BigDecimal result = evaluator.evaluate("1 / 3");

        assertTrue(result.scale() > 10);
        assertEquals(0, result.compareTo(new BigDecimal("0.3333333333333333333333333333333333")));
    }

    @Test
    void formulaOutputUsesConfiguredRoundingMode() {
        UnitConversionFormula formula = new UnitConversionFormula();
        formula.setDecimalScale(4);
        formula.setRoundingMode("HALF_UP");
        assertEquals(new BigDecimal("78.7393"),
                formulaEngine.roundResult(new BigDecimal("78.73925"), formula));

        formula.setRoundingMode("HALF_DOWN");
        assertEquals(new BigDecimal("78.7392"),
                formulaEngine.roundResult(new BigDecimal("78.73925"), formula));
    }
}
