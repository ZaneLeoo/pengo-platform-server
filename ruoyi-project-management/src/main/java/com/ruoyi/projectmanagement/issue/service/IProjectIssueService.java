package com.ruoyi.projectmanagement.issue.service;

import com.ruoyi.projectmanagement.issue.domain.IssueTransitionRequest;
import com.ruoyi.projectmanagement.issue.domain.ProjectIssue;
import com.ruoyi.projectmanagement.issue.domain.ProjectIssueActivity;
import com.ruoyi.projectmanagement.issue.domain.ProjectIssueCapability;
import java.util.List;

public interface IProjectIssueService {
    List<ProjectIssue> list(ProjectIssue filter, Long userId);

    ProjectIssue get(Long id, Long userId);

    ProjectIssueCapability capability(Long projectId, Long userId);

    int add(ProjectIssue issue, String operator, Long userId);

    int edit(ProjectIssue issue, String operator, Long userId);

    int remove(Long[] ids, String operator, Long userId);

    int transition(Long id, IssueTransitionRequest request, String operator, Long userId);

    int addActivity(Long id, ProjectIssueActivity activity, String operator, Long userId);

    List<ProjectIssueActivity> activities(Long id, Long userId);
}
