package com.ruoyi.agent.application.source;

import lombok.Data;

/** Agent V2 知识和实时信息来源。 */
@Data
public class AgentV2Citation
{
    private String id;
    private String title;
    private String content;
    private String sourceUrl;
    private Double score;
}
