package com.ruoyi.projectmanagement.change.service;

import com.ruoyi.projectmanagement.change.domain.ProjectPlanBaseline;
import com.ruoyi.projectmanagement.change.domain.ProjectPlanChange;
import com.ruoyi.projectmanagement.change.domain.ProjectPlanChangeAttachment;
import com.ruoyi.projectmanagement.change.domain.ProjectPlanChangeProjectCapability;
import com.ruoyi.projectmanagement.team.domain.ProjectMember;
import java.util.List;
import java.util.Map;

public interface IProjectPlanChangeService {
    void createInitialBaseline(Long projectId, String operator);

    List<ProjectPlanBaseline> baselines(Long projectId, Long userId);

    Map<String, Object> compare(
            Long projectId, Long fromBaselineId, Long toBaselineId, Long userId);

    List<ProjectPlanChange> list(Long projectId, Long userId);

    ProjectPlanChangeProjectCapability capability(Long projectId, Long userId);

    List<ProjectMember> memberCandidates(Long projectId, String keyword, Long userId);

    ProjectPlanChange detail(Long changeId, Long userId);

    ProjectPlanChangeAttachment attachment(Long changeId, Long attachmentId, Long userId);

    Long save(ProjectPlanChange change, String operator, Long userId);

    void delete(Long changeId, String operator, Long userId);

    void submit(Long changeId, String operator, Long userId);

    void withdraw(Long changeId, String operator, Long userId);

    void apply(Long changeId, String operator, Long userId);
}
