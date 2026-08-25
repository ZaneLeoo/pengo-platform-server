package com.ruoyi.projectmanagement.issue.mapper;

import com.ruoyi.projectmanagement.issue.domain.ProjectIssue;
import com.ruoyi.projectmanagement.issue.domain.ProjectIssueActivity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectIssueMapper {
    List<ProjectIssue> selectList(ProjectIssue filter);
    ProjectIssue selectById(Long id);
    int insert(ProjectIssue issue);
    int update(ProjectIssue issue);
    int delete(Long[] ids);
    int countByProject(Long projectId);
    int updateStatus(ProjectIssue issue);
    int insertActivity(ProjectIssueActivity activity);
    List<ProjectIssueActivity> selectActivities(Long issueId);
}
