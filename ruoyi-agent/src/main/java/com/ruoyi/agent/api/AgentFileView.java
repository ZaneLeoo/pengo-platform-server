package com.ruoyi.agent.api;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Agent 文件的客户端安全视图。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentFileView {
    private String resourceId;
    private String name;
    private String extension;
    private String mediaType;
    private String kind;
    private Long size;
    private String downloadUrl;
    private String preview;
    private Date createdAt;
}
