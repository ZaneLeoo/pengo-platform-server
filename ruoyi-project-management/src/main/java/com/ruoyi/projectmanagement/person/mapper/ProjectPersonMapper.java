package com.ruoyi.projectmanagement.person.mapper;

import com.ruoyi.projectmanagement.person.domain.ProjectPerson;
import com.ruoyi.projectmanagement.person.domain.ProjectUserOption;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 项目人员档案数据访问接口。
 */
@Mapper
public interface ProjectPersonMapper {

    /** 查询人员档案列表。 */
    List<ProjectPerson> selectProjectPersonList(ProjectPerson person);

    /** 查询启用状态的人员选项。 */
    List<ProjectPerson> selectEnabledPersonOptions(@Param("keyword") String keyword);

    /** 根据ID查询人员档案。 */
    ProjectPerson selectProjectPersonById(Long personId);

    /** 根据编码查询人员档案。 */
    ProjectPerson selectProjectPersonByCode(String personCode);

    /** 查询未被其他人员档案绑定的启用系统账号。 */
    List<ProjectUserOption> selectAvailableUserOptions(@Param("personId") Long personId,
            @Param("keyword") String keyword);

    /** 查询已经绑定到其他人员档案的记录。 */
    ProjectPerson selectProjectPersonByUserId(Long userId);

    /** 新增人员档案。 */
    int insertProjectPerson(ProjectPerson person);

    /** 修改人员档案。 */
    int updateProjectPerson(ProjectPerson person);

    /** 批量删除人员档案。 */
    int deleteProjectPersonByIds(Long[] personIds);
}
