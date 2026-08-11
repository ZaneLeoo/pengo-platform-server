package com.ruoyi.projectmanagement.person.mapper;

import com.ruoyi.projectmanagement.person.domain.ProjectPerson;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 项目人员档案数据访问。 */
@Mapper
public interface ProjectPersonMapper {
    List<ProjectPerson> selectProjectPersonList(ProjectPerson person);

    List<ProjectPerson> selectEnabledPersonOptions(@Param("keyword") String keyword);

    ProjectPerson selectProjectPersonById(Long personId);

    ProjectPerson selectProjectPersonByCode(String personCode);

    int insertProjectPerson(ProjectPerson person);

    int updateProjectPerson(ProjectPerson person);

    int deleteProjectPersonByIds(Long[] personIds);
}
