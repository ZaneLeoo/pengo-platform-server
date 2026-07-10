package com.ruoyi.agent.domain.enums;

/** 图表白名单。 */
public enum ChartType
{
    LINE("line"),
    BAR("bar"),
    PIE("pie");

    private final String code;

    ChartType(String code)
    {
        this.code = code;
    }

    /** 返回图表协议编码。 */
    public String getCode()
    {
        return code;
    }

    /** 判断图表协议值是否受支持。 */
    public static boolean supports(String code)
    {
        for (ChartType type : values())
        {
            if (type.code.equals(code))
            {
                return true;
            }
        }
        return false;
    }
}
