package com.ruoyi.projectmanagement.professionalrole.service;

import com.ruoyi.projectmanagement.professionalrole.domain.ProfessionalRole;
import java.util.List;

/** 专业角色服务。 */
public interface IProfessionalRoleService {

    List<ProfessionalRole> list(ProfessionalRole filter);

    List<ProfessionalRole> options();

    ProfessionalRole get(Long id);

    int add(ProfessionalRole role, String operator);

    int edit(ProfessionalRole role, String operator);

    int remove(Long id);
}
