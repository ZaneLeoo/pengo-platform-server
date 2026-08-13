package com.ruoyi.projectmanagement.person.service.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.projectmanagement.person.domain.ProjectPerson;
import com.ruoyi.projectmanagement.person.mapper.ProjectPersonMapper;
import com.ruoyi.projectmanagement.person.service.IProjectPersonService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 项目人员档案业务实现。
 */
@Service
public class ProjectPersonServiceImpl implements IProjectPersonService {
    private final ProjectPersonMapper personMapper;

    public ProjectPersonServiceImpl(ProjectPersonMapper personMapper) {
        this.personMapper = personMapper;
    }

    @Override
    public List<ProjectPerson> selectProjectPersonList(ProjectPerson person) {
        return personMapper.selectProjectPersonList(person);
    }

    @Override
    public List<ProjectPerson> selectEnabledPersonOptions(String keyword) {
        return personMapper.selectEnabledPersonOptions(keyword);
    }

    @Override
    public ProjectPerson selectProjectPersonById(Long personId) {
        return personMapper.selectProjectPersonById(personId);
    }

    @Override
    public boolean checkPersonCodeUnique(ProjectPerson person) {
        Long personId = StringUtils.isNull(person.getPersonId()) ? -1L : person.getPersonId();
        ProjectPerson existing = personMapper.selectProjectPersonByCode(person.getPersonCode());
        return StringUtils.isNull(existing) || existing.getPersonId().longValue() == personId.longValue();
    }

    @Override
    public int insertProjectPerson(ProjectPerson person) {
        return personMapper.insertProjectPerson(person);
    }

    @Override
    public int updateProjectPerson(ProjectPerson person) {
        return personMapper.updateProjectPerson(person);
    }

    @Override
    public int deleteProjectPersonByIds(Long[] personIds) {
        return personMapper.deleteProjectPersonByIds(personIds);
    }
}
