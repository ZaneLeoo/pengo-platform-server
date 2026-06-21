package com.ruoyi.agent.domain.enums;

/** Agent 系统参数键。 */
public enum AgentConfigKey
{
    DIFY_API_BASE_URL("agent.dify.api_base_url"), DIFY_API_KEY("agent.dify.api_key");
    private final String key;
    AgentConfigKey(String key) { this.key = key; }
    /** 返回 sys_config 参数键。 */
    public String getKey() { return key; }
}
