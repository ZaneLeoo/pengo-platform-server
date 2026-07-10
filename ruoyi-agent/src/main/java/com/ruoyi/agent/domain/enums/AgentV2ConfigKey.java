package com.ruoyi.agent.domain.enums;

/** Agent V2 系统配置键。 */
public enum AgentV2ConfigKey
{
    TOOL_GATEWAY_KEY("agent.v2.tool_gateway_key"),
    TOOL_DISPLAY_ALIASES("agent.v2.tool_display_aliases");

    private final String key;

    AgentV2ConfigKey(String key)
    {
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }
}
