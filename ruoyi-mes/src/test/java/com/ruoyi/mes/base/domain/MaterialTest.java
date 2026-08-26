package com.ruoyi.mes.base.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ruoyi.mes.common.enums.MaterialType;
import org.junit.jupiter.api.Test;

class MaterialTest {

    @Test
    void shouldExposeFinishedGoodsTypeCode() {
        assertEquals("FINISHED", MaterialType.FINISHED.getCode());
    }
}
