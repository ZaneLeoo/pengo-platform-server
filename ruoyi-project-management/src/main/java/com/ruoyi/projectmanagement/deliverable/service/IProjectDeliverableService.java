package com.ruoyi.projectmanagement.deliverable.service;

import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverable;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverableSubmission;
import java.util.List;

public interface IProjectDeliverableService {
    List<ProjectDeliverable> selectList(ProjectDeliverable entity);
    ProjectDeliverable selectById(Long id);
    int insert(ProjectDeliverable entity);
    int update(ProjectDeliverable entity);
    int deleteByIds(Long[] ids);
    void submit(Long id, ProjectDeliverableSubmission submission, String username);
    void review(Long id, boolean approved, String comment, String username);
    List<ProjectDeliverableSubmission> selectSubmissions(Long id);
}
