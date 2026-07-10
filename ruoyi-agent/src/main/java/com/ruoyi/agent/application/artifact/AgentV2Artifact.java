package com.ruoyi.agent.application.artifact;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;

/** 工具返回和前端渲染共同使用的 Agent V2 产物描述。 */
@Data
public class AgentV2Artifact
{
    public static final String VERSION = "2.0";

    private Long artifactId;
    private String type;
    private String version = VERSION;
    private String title;
    private Long datasetId;
    private Map<String, Object> payload = Collections.emptyMap();
    private Long fileId;
    private String mimeType;
    private String previewUrl;
    private String downloadUrl;

    /** 防止调用方在事件发送后继续修改载荷。 */
    public void setPayload(Map<String, Object> payload)
    {
        this.payload = payload == null ? Collections.emptyMap()
            : Collections.unmodifiableMap(new LinkedHashMap<>(payload));
    }
}
