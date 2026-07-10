package com.ruoyi.agent.domain.enums;

import java.util.Arrays;

/** Agent V2 可安全持久化和渲染的产物类型。 */
public enum AgentArtifactKind
{
    MARKDOWN,
    CODE,
    TABLE,
    CHART,
    FILE,
    DOCUMENT,
    IMAGE,
    CITATION,
    BUSINESS_CARD,
    APPROVAL;

    /** 判断外部类型是否在 V2 白名单中。 */
    public static boolean supports(String value)
    {
        return value != null && Arrays.stream(values()).anyMatch(kind -> kind.name().equalsIgnoreCase(value));
    }
}
