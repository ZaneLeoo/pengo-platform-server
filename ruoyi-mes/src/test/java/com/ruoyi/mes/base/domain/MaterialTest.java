package com.ruoyi.mes.base.domain;

import com.ruoyi.mes.common.enums.MaterialType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaterialTest {

    @Test
    void shouldExposeFinishedGoodsTypeCode() {
        assertEquals("FINISHED", MaterialType.FINISHED.getCode());
    }
}
