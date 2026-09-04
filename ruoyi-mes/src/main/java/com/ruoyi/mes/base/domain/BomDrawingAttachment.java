package com.ruoyi.mes.base.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.Data;

/** BOM 图纸附件。 */
@Data
public class BomDrawingAttachment {
    /** 附件主键。 */
    private Long attachmentId;

    /** 所属 BOM 主表 ID。 */
    private Long bomMasterId;

    /** 用户上传时的原始文件名。 */
    @NotBlank(message = "附件名称不能为空")
    @Size(max = 255, message = "附件名称不能超过255个字符")
    private String fileName;

    /** 通用文件上传接口返回的资源地址。 */
    @NotBlank(message = "附件地址不能为空")
    @Size(max = 1000, message = "附件地址不能超过1000个字符")
    private String fileUrl;

    /** 文件大小，单位为字节。 */
    private Long fileSize;

    /** 小写文件扩展名。 */
    @Size(max = 32, message = "文件扩展名不能超过32个字符")
    private String fileExt;

    /** 文件 MIME 类型。 */
    @Size(max = 128, message = "文件类型不能超过128个字符")
    private String mimeType;

    /** 图纸附件说明。 */
    @Size(max = 500, message = "附件说明不能超过500个字符")
    private String description;

    /** 上传人账号。 */
    private String uploadBy;

    /** 上传时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Date uploadTime;
}
