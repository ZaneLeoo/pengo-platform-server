package com.ruoyi.projectmanagement.professionalrole.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.projectmanagement.professionalrole.domain.ProfessionalRole;
import com.ruoyi.projectmanagement.professionalrole.mapper.ProfessionalRoleMapper;
import com.ruoyi.projectmanagement.professionalrole.service.IProfessionalRoleService;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 专业角色服务实现。 */
@Service
public class ProfessionalRoleServiceImpl implements IProfessionalRoleService {

    private final ProfessionalRoleMapper mapper;

    public ProfessionalRoleServiceImpl(ProfessionalRoleMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ProfessionalRole> list(ProfessionalRole filter) {
        return mapper.selectList(filter);
    }

    @Override
    public List<ProfessionalRole> options() {
        ProfessionalRole filter = new ProfessionalRole();
        filter.setStatus("0");
        return mapper.selectList(filter);
    }

    @Override
    public ProfessionalRole get(Long id) {
        ProfessionalRole role = mapper.selectById(id);
        if (role == null) {
            throw new ServiceException("专业角色不存在");
        }
        return role;
    }

    @Override
    @Transactional
    public int add(ProfessionalRole role, String operator) {
        normalize(role);
        if (mapper.selectByCode(role.getRoleCode()) != null) {
            throw new ServiceException("专业角色编码已存在");
        }
        role.setSystemFlag("0");
        role.setCreateBy(operator);
        return mapper.insert(role);
    }

    @Override
    @Transactional
    public int edit(ProfessionalRole role, String operator) {
        ProfessionalRole old = get(role.getProfessionalRoleId());
        if ("1".equals(old.getSystemFlag())) {
            throw new ServiceException("系统预置专业角色不可修改");
        }
        normalize(role);
        ProfessionalRole duplicate = mapper.selectByCode(role.getRoleCode());
        if (duplicate != null
                && !duplicate.getProfessionalRoleId().equals(role.getProfessionalRoleId())) {
            throw new ServiceException("专业角色编码已存在");
        }
        if ("1".equals(role.getStatus())
                && mapper.countActiveMembers(role.getProfessionalRoleId()) > 0) {
            throw new ServiceException("该专业角色仍被在组成员使用，不能停用");
        }
        role.setSystemFlag(old.getSystemFlag());
        role.setUpdateBy(operator);
        return mapper.update(role);
    }

    @Override
    @Transactional
    public int remove(Long id) {
        ProfessionalRole role = get(id);
        if ("1".equals(role.getSystemFlag())) {
            throw new ServiceException("系统预置专业角色不可删除");
        }
        if (mapper.countActiveMembers(id) > 0) {
            throw new ServiceException("该专业角色仍被在组成员使用，不能删除");
        }
        return mapper.deleteById(id);
    }

    private void normalize(ProfessionalRole role) {
        role.setRoleCode(role.getRoleCode().trim().toUpperCase(Locale.ROOT));
        role.setRoleName(role.getRoleName().trim());
        if (role.getStatus() == null) {
            role.setStatus("0");
        }
        if (role.getSortOrder() == null) {
            role.setSortOrder(0);
        }
    }
}
