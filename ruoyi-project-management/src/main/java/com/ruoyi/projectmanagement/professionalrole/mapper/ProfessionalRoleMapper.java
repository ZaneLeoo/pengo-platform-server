package com.ruoyi.projectmanagement.professionalrole.mapper;

import com.ruoyi.projectmanagement.professionalrole.domain.ProfessionalRole;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/** 专业角色数据访问。 */
@Mapper
public interface ProfessionalRoleMapper {

    List<ProfessionalRole> selectList(ProfessionalRole filter);

    ProfessionalRole selectById(Long id);

    ProfessionalRole selectByCode(String roleCode);

    int insert(ProfessionalRole role);

    int update(ProfessionalRole role);

    int deleteById(Long id);

    int countActiveMembers(Long id);
}
