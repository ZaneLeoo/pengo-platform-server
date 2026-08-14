package com.ruoyi.projectmanagement.deliverable.service;

import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverableType;
import java.util.List;

public interface IProjectDeliverableTypeService {
    List<ProjectDeliverableType> list(ProjectDeliverableType filter);
    ProjectDeliverableType get(Long id);
    int add(ProjectDeliverableType entity, String operator);
    int edit(ProjectDeliverableType entity, String operator);
    int remove(Long id);
}
