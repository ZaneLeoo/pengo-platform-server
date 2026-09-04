package com.ruoyi.mes.base.service;

import com.ruoyi.mes.base.domain.BomDrawingAttachment;
import java.util.List;

/** BOM 图纸附件业务接口。 */
public interface IBomDrawingAttachmentService {
    /** 查询 BOM 图纸附件。 */
    List<BomDrawingAttachment> list(Long bomMasterId);

    /** 登记已上传的 BOM 图纸附件。 */
    BomDrawingAttachment add(Long bomMasterId, BomDrawingAttachment attachment, String operator);

    /** 删除 BOM 图纸附件关系。 */
    void delete(Long bomMasterId, Long attachmentId);
}
