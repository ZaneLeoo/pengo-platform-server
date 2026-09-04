package com.ruoyi.mes.base.mapper;

import com.ruoyi.mes.base.domain.BomDrawingAttachment;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** BOM 图纸附件数据访问接口。 */
public interface BomDrawingAttachmentMapper {
    /** 查询 BOM 的全部图纸附件。 */
    List<BomDrawingAttachment> selectByBomMasterId(Long bomMasterId);

    /** 查询单个图纸附件。 */
    BomDrawingAttachment selectById(Long attachmentId);

    /** 新增图纸附件。 */
    int insert(BomDrawingAttachment attachment);

    /** 删除指定 BOM 下的图纸附件。 */
    int delete(@Param("bomMasterId") Long bomMasterId, @Param("attachmentId") Long attachmentId);
}
