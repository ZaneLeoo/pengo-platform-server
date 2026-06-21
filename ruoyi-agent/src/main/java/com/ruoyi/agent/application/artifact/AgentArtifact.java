package com.ruoyi.agent.application.artifact;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** 通过安全校验后可发送给前端的结构化产物。 */
public class AgentArtifact
{
    private final String type;
    private final String version;
    private final String title;
    private final Map<String, Object> payload;

    public AgentArtifact(String type, String version, String title, Map<String, Object> payload)
    {
        this.type = type;
        this.version = version;
        this.title = title;
        this.payload = Collections.unmodifiableMap(new LinkedHashMap<>(payload));
    }

    /** 返回产物类型。 */
    public String getType()
    {
        return type;
    }

    /** 返回协议版本。 */
    public String getVersion()
    {
        return version;
    }

    /** 返回展示标题。 */
    public String getTitle()
    {
        return title;
    }

    /** 返回经过校验的负载。 */
    public Map<String, Object> getPayload()
    {
        return payload;
    }
}
