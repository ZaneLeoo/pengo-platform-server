package com.ruoyi.projectmanagement.project.mapper;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo; import java.util.List; import org.apache.ibatis.annotations.Mapper;
/** 项目主档数据访问。 */ @Mapper public interface ProjectInfoMapper { List<ProjectInfo> selectProjectInfoList(ProjectInfo project); ProjectInfo selectProjectInfoById(Long projectId); ProjectInfo selectProjectInfoByCode(String projectCode); int insertProjectInfo(ProjectInfo project); int updateProjectInfo(ProjectInfo project); int deleteProjectInfoByIds(Long[] projectIds); }
