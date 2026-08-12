package com.ruoyi.projectmanagement.execution.mapper;

import com.ruoyi.projectmanagement.execution.domain.ProjectWorkItem;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

/** 项目执行项数据访问。 */
@Mapper
public interface ProjectWorkItemMapper {
    List<ProjectWorkItem> selectList(ProjectWorkItem item);
    ProjectWorkItem selectById(Long itemId);
    ProjectWorkItem selectByCode(String itemCode);
    int insert(ProjectWorkItem item);
    int update(ProjectWorkItem item);
    int deleteByIds(Long[] itemIds);
    int countDeliverablesByTaskId(Long taskId);
    List<Map<String, Object>> selectOverview();
}
