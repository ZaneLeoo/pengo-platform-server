package com.ruoyi.projectmanagement.project.service.impl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.projectmanagement.category.mapper.ProjectCategoryMapper;
import com.ruoyi.projectmanagement.person.mapper.ProjectPersonMapper;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.project.service.IProjectInfoService;
import java.util.List;
import org.springframework.stereotype.Service;
/** 项目主档业务实现。 */
@Service
public class ProjectInfoServiceImpl implements IProjectInfoService {
    private final ProjectInfoMapper projectMapper;
    private final ProjectCategoryMapper categoryMapper;
    private final ProjectPersonMapper personMapper;
    public ProjectInfoServiceImpl(ProjectInfoMapper p, ProjectCategoryMapper c, ProjectPersonMapper m) {
        projectMapper = p;
        categoryMapper = c;
        personMapper = m;
    }
    @Override
    public List<ProjectInfo> selectProjectInfoList(ProjectInfo project) {
        return projectMapper.selectProjectInfoList(project);
    }
    @Override
    public ProjectInfo selectProjectInfoById(Long id) {
        return projectMapper.selectProjectInfoById(id);
    }
    @Override
    public boolean checkProjectCodeUnique(ProjectInfo p) {
        Long id = StringUtils.isNull(p.getProjectId()) ? -1L : p.getProjectId();
        ProjectInfo e = projectMapper.selectProjectInfoByCode(p.getProjectCode());
        return e == null || e.getProjectId().longValue() == id.longValue();
    }
    @Override
    public int insertProjectInfo(ProjectInfo p) {
        validate(p);
        return projectMapper.insertProjectInfo(p);
    }
    @Override
    public int updateProjectInfo(ProjectInfo p) {
        validate(p);
        return projectMapper.updateProjectInfo(p);
    }
    @Override
    public int deleteProjectInfoByIds(Long[] ids) {
        return projectMapper.deleteProjectInfoByIds(ids);
    }
    private void validate(ProjectInfo p) {
        if (p.getEndDate().isBefore(p.getStartDate()))
            throw new ServiceException("计划结束日期不能早于开始日期");
        if (categoryMapper.selectProjectCategoryById(p.getCategoryId()) == null)
            throw new ServiceException("项目分类不存在");
        var manager = personMapper.selectProjectPersonById(p.getManagerId());
        if (manager == null || !"0".equals(manager.getStatus()))
            throw new ServiceException("请选择启用状态的项目负责人");
        if (p.getProgress() == null)
            p.setProgress(0);
        if (StringUtils.isBlank(p.getStatus()))
            p.setStatus("DRAFT");
    }
}
