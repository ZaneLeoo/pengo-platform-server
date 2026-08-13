package com.ruoyi.projectmanagement.issue.service;

import com.ruoyi.projectmanagement.issue.domain.ProjectIssue;
import java.util.List;

public interface IProjectIssueService {
    List<ProjectIssue> list(ProjectIssue filter);
    ProjectIssue get(Long id);
    int add(ProjectIssue issue, String operator);
    int edit(ProjectIssue issue, String operator);
    int remove(Long[] ids, String operator);
}
