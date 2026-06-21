package com.ruoyi.agent.domain.enums;

/** 结构化产物类型。 */
public enum ArtifactType
{
    CHART("chart"),
    TABLE("table");

    private final String code;

    ArtifactType(String code)
    {
        this.code = code;
    }

    /** 返回对外协议值。 */
    public String getCode()
    {
        return code;
    }

    /** 判断协议值是否受支持。 */
    public static boolean supports(String code)
    {
        for (ArtifactType type : values())
        {
            if (type.code.equals(code))
            {
                return true;
            }
        }
        return false;
    }
}
