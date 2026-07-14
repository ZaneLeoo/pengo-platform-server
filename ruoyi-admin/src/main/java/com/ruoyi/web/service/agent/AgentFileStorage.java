package com.ruoyi.web.service.agent;

import java.io.IOException;
import java.nio.file.Path;

/** Agent 文件内容存储抽象。 */
public interface AgentFileStorage {
    /** 创建下载过程使用的临时文件。 */
    Path createTemporary(String resourceId) throws IOException;

    /** 将临时文件持久化，并返回相对存储路径。 */
    String persist(Path temporary, String resourceId, String extension) throws IOException;

    /** 将相对路径解析为受控的本地文件。 */
    Path resolve(String relativePath);

    /** 删除一个已持久化文件。 */
    void delete(String relativePath) throws IOException;
}
