package com.ruoyi.projectmanagement.deliverable.mapper;

import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverableType;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectDeliverableTypeMapper {
    List<ProjectDeliverableType> selectList(ProjectDeliverableType filter);

    ProjectDeliverableType selectById(Long id);

    ProjectDeliverableType selectByCode(String code);

    int insert(ProjectDeliverableType entity);

    int update(ProjectDeliverableType entity);

    int deleteById(Long id);

    /** 统计已引用该类型的正式交付物数量。 */
    int countDeliverableReferences(Long id);

    int deleteFormats(Long typeId);

    int insertFormat(@Param("typeId") Long typeId, @Param("extension") String extension);
}
