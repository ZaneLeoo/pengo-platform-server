package com.ruoyi.mes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MesModuleTest {

    @Test
    void shouldExposeMesModuleName() {
        assertEquals("ruoyi-mes", MesModule.moduleName());
    }
}
