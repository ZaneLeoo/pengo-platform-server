package com.ruoyi.projectmanagement.person.service;

import com.ruoyi.projectmanagement.person.domain.ProjectPerson;
import java.util.List;

/**
 * 项目人员档案业务接口。
 */
public interface IProjectPersonService {

    /** 查询人员档案列表。 */
    List<ProjectPerson> selectProjectPersonList(ProjectPerson person);

    /** 查询启用状态的人员选项。 */
    List<ProjectPerson> selectEnabledPersonOptions(String keyword);

    /** 根据ID查询人员档案。 */
    ProjectPerson selectProjectPersonById(Long personId);

    /** 校验人员编码是否唯一。 */
    boolean checkPersonCodeUnique(ProjectPerson person);

    /** 新增人员档案。 */
    int insertProjectPerson(ProjectPerson person);

    /** 修改人员档案。 */
    int updateProjectPerson(ProjectPerson person);

    /** 批量删除人员档案。 */
    int deleteProjectPersonByIds(Long[] personIds);
}
