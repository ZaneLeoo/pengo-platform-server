package com.ruoyi.projectmanagement.project.mapper;

import com.ruoyi.projectmanagement.project.domain.ProjectInitiationAttachment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 立项申请附件数据访问接口。 */
@Mapper
public interface ProjectInitiationAttachmentMapper {

    /** 查询当前草稿附件。 */
    List<ProjectInitiationAttachment> selectDraft(@Param("projectId") Long projectId,
            @Param("sectionCode") String sectionCode);

    /** 查询审批版本附件。 */
    List<ProjectInitiationAttachment> selectByApproval(@Param("approvalId") Long approvalId,
            @Param("sectionCode") String sectionCode);

    /** 查询项目最新审批版本附件。 */
    List<ProjectInitiationAttachment> selectLatestApproved(@Param("projectId") Long projectId,
            @Param("sectionCode") String sectionCode);

    /** 新增附件。 */
    int insert(ProjectInitiationAttachment attachment);

    /** 将草稿附件绑定到审批版本。 */
    int bindDraft(@Param("projectId") Long projectId, @Param("approvalId") Long approvalId,
            @Param("versionNo") Integer versionNo);

    /** 将退回版本附件复制为新的草稿附件。 */
    int copyToDraft(@Param("approvalId") Long approvalId, @Param("versionNo") Integer versionNo,
            @Param("createBy") String createBy);

    /** 删除当前草稿附件。 */
    int deleteDraft(@Param("projectId") Long projectId, @Param("attachmentId") Long attachmentId);
}
