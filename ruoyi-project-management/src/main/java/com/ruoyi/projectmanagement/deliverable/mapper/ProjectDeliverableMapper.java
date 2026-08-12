package com.ruoyi.projectmanagement.deliverable.mapper;

import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverable;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverableSubmission;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectDeliverableMapper {
    List<ProjectDeliverable> selectList(ProjectDeliverable entity);
    ProjectDeliverable selectById(Long deliverableId);
    int insert(ProjectDeliverable entity);
    int update(ProjectDeliverable entity);
    int deleteByIds(Long[] ids);
    int updateStatus(ProjectDeliverable entity);
    int countUnsatisfiedRequiredByTaskId(Long taskId);
    int countByTaskId(Long taskId);
    List<ProjectDeliverableSubmission> selectSubmissions(Long deliverableId);
    int insertSubmission(ProjectDeliverableSubmission entity);
    int updateSubmissionReview(ProjectDeliverableSubmission entity);
    Integer selectNextVersion(Long deliverableId);
}
