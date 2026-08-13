package com.ruoyi.projectmanagement.wbs.service;
import com.ruoyi.projectmanagement.wbs.domain.ProjectWbsNode;
import java.util.List;
public interface IProjectWbsService {
    List<ProjectWbsNode> list(ProjectWbsNode filter);
    ProjectWbsNode get(Long id);
    int add(ProjectWbsNode node,String operator);
    int edit(ProjectWbsNode node,String operator);
    int remove(Long id,String operator);
    void refreshProject(Long projectId);
    boolean allWorkPackagesCompleted(Long projectId);
}
