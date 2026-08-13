package com.ruoyi.projectmanagement.wbs.mapper;

import com.ruoyi.projectmanagement.wbs.domain.ProjectWbsNode;
import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectWbsMapper {
    List<ProjectWbsNode> selectList(ProjectWbsNode filter);
    ProjectWbsNode selectById(Long id);
    int insert(ProjectWbsNode node);
    int update(ProjectWbsNode node);
    int deleteById(Long id);
    int countChildren(Long id);
    int countTasks(Long id);
    int countDeliverables(Long id);
    int updateCode(@Param("id") Long id,@Param("code") String code);
    int updateAggregate(@Param("id") Long id,@Param("start") LocalDate start,@Param("end") LocalDate end,@Param("status") String status,@Param("progress") Integer progress);
    List<ProjectWbsNode> selectChildren(@Param("projectId") Long projectId,@Param("parentId") Long parentId);
}
