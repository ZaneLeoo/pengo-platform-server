package com.ruoyi.mes;

/**
 * MES模块标识。
 *
 * @author ruoyi
 */
public final class MesModule {

    private static final String MODULE_NAME = "ruoyi-mes";

    private MesModule() {
    }

    /**
     * 获取模块名称。
     *
     * @return 模块名称
     */
    public static String moduleName() {
        return MODULE_NAME;
    }
}
