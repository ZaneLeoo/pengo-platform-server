package com.ruoyi.agent.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** Agent 生成文件的持久化元数据。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentFile extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 文件主键。 */
    private Long id;

    /** 提供给客户端使用的不可猜测资源标识。 */
    private String resourceId;

    /** 文件所属用户ID。 */
    private Long userId;

    /** 关联的本地会话ID，尚未建立关联时为空。 */
    private Long conversationId;

    /** 关联的本地消息ID，尚未建立关联时为空。 */
    private Long messageId;

    /** Dify 会话ID。 */
    private String difyConversationId;

    /** Dify 原始文件ID。 */
    private String difyFileId;

    /** 生成文件的工具机器标识。 */
    private String toolName;

    /** 面向用户展示的文件名。 */
    private String fileName;

    /** 文件系统中的实际存储名。 */
    private String storageName;

    /** 相对于文件存储根目录的路径。 */
    private String relativePath;

    /** 不含点号的文件扩展名。 */
    private String extension;

    /** 文件 MIME 类型。 */
    private String mediaType;

    /** 文件类别，例如 document、spreadsheet、presentation、pdf。 */
    private String fileKind;

    /** 文件大小，单位为字节。 */
    private Long fileSize;

    /** 文件内容的 SHA-256 摘要。 */
    private String fileHash;

    /** 访问方式：BROWSER-浏览器预览，DOWNLOAD-下载。 */
    private String previewMode;

    /** 文件状态：AVAILABLE-可用，FAILED-失败，DELETED-已删除。 */
    private String status;
}
