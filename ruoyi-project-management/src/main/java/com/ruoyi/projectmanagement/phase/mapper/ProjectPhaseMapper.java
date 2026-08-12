package com.ruoyi.projectmanagement.phase.mapper;
import com.ruoyi.projectmanagement.phase.domain.ProjectPhase;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper public interface ProjectPhaseMapper {
 List<ProjectPhase> selectList(ProjectPhase phase); ProjectPhase selectById(Long id); int insert(ProjectPhase phase); int update(ProjectPhase phase); int deleteById(Long id);
 int countTasks(Long id); int countIncompleteLeafTasks(Long id); int updateLifecycle(ProjectPhase phase);
 int insertLifecycleLog(@Param("phaseId")Long phaseId,@Param("projectId")Long projectId,@Param("action")String action,@Param("fromStatus")String fromStatus,@Param("toStatus")String toStatus,@Param("operator")String operator);
 int countIncompleteByProject(Long projectId); int countByProject(Long projectId);
}
