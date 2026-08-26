package com.ruoyi.mes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MesModuleTest {

    @Test
    void shouldExposeMesModuleName() {
        assertEquals("ruoyi-mes", MesModule.moduleName());
    }
}
