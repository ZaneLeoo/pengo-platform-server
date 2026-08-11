package com.ruoyi.projectmanagement.person.service;

import com.ruoyi.projectmanagement.person.domain.ProjectPerson;
import java.util.List;

/** 项目人员档案业务接口。 */
public interface IProjectPersonService {
    List<ProjectPerson> selectProjectPersonList(ProjectPerson person);

    List<ProjectPerson> selectEnabledPersonOptions(String keyword);

    ProjectPerson selectProjectPersonById(Long personId);

    boolean checkPersonCodeUnique(ProjectPerson person);

    int insertProjectPerson(ProjectPerson person);

    int updateProjectPerson(ProjectPerson person);

    int deleteProjectPersonByIds(Long[] personIds);
}
