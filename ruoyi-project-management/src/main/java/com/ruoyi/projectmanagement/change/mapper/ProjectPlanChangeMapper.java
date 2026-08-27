package com.ruoyi.projectmanagement.change.mapper;

import com.ruoyi.projectmanagement.change.domain.ProjectPlanBaseline;
import com.ruoyi.projectmanagement.change.domain.ProjectPlanChange;
import com.ruoyi.projectmanagement.change.domain.ProjectPlanChangeAttachment;
import com.ruoyi.projectmanagement.change.domain.ProjectPlanChangeAudit;
import com.ruoyi.projectmanagement.change.domain.ProjectPlanChangeItem;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectPlanChangeMapper {
    ProjectPlanBaseline selectCurrentBaseline(Long projectId);

    ProjectPlanBaseline selectCurrentBaselineForUpdate(Long projectId);

    List<ProjectPlanBaseline> selectBaselines(Long projectId);

    ProjectPlanBaseline selectBaseline(Long baselineId);

    int insertBaseline(ProjectPlanBaseline baseline);

    ProjectPlanChange selectChange(Long changeId);

    ProjectPlanChange selectChangeForUpdate(Long changeId);

    List<ProjectPlanChange> selectChanges(@Param("projectId") Long projectId);

    int insertChange(ProjectPlanChange change);

    int updateChange(ProjectPlanChange change);

    int deleteItems(Long changeId);

    int insertItem(ProjectPlanChangeItem item);

    List<ProjectPlanChangeItem> selectItems(Long changeId);

    int deleteAttachments(Long changeId);

    int insertAttachment(ProjectPlanChangeAttachment attachment);

    List<ProjectPlanChangeAttachment> selectAttachments(Long changeId);

    int bindWorkflow(
            @Param("changeId") Long changeId, @Param("workflowInstanceId") Long workflowInstanceId);

    int deleteChange(Long changeId);

    int insertAudit(ProjectPlanChangeAudit audit);

    List<ProjectPlanChangeAudit> selectAudits(Long changeId);
}
