package com.ruoyi.projectmanagement.issue.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.projectmanagement.issue.domain.ProjectIssue;
import com.ruoyi.projectmanagement.issue.mapper.ProjectIssueMapper;
import com.ruoyi.projectmanagement.issue.service.IProjectIssueService;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.task.domain.ProjectTask;
import com.ruoyi.projectmanagement.task.mapper.ProjectTaskMapper;
import com.ruoyi.projectmanagement.team.service.IProjectTeamService;
import com.ruoyi.projectmanagement.wbs.domain.ProjectWbsNode;
import com.ruoyi.projectmanagement.wbs.mapper.ProjectWbsMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProjectIssueServiceImpl implements IProjectIssueService {
    private final ProjectIssueMapper mapper; private final ProjectInfoMapper projectMapper; private final ProjectWbsMapper wbsMapper; private final ProjectTaskMapper taskMapper; private final IProjectTeamService teamService;
    public ProjectIssueServiceImpl(ProjectIssueMapper mapper, ProjectInfoMapper projectMapper, ProjectWbsMapper wbsMapper, ProjectTaskMapper taskMapper, IProjectTeamService teamService) { this.mapper=mapper; this.projectMapper=projectMapper; this.wbsMapper=wbsMapper; this.taskMapper=taskMapper; this.teamService=teamService; }
    @Override public List<ProjectIssue> list(ProjectIssue f) { return mapper.selectList(f); }
    @Override public ProjectIssue get(Long id) { ProjectIssue i=mapper.selectById(id); if(i==null) throw new ServiceException("问题不存在"); return i; }
    @Override public int add(ProjectIssue i,String op) { validate(i); i.setIssueCode(nextCode(i.getProjectId())); if(StringUtils.isBlank(i.getSeverity()))i.setSeverity("MEDIUM"); if(StringUtils.isBlank(i.getStatus()))i.setStatus("OPEN"); i.setCreateBy(op); return mapper.insert(i); }
    @Override public int edit(ProjectIssue i,String op) { ProjectIssue old=get(i.getIssueId()); i.setProjectId(old.getProjectId()); i.setIssueCode(old.getIssueCode()); validate(i); i.setUpdateBy(op); return mapper.update(i); }
    @Override public int remove(Long[] ids,String op) { for(Long id:ids)get(id); return mapper.delete(ids); }
    private String nextCode(Long projectId){return "ISS-"+String.format("%03d",mapper.countByProject(projectId)+1);}
    private void validate(ProjectIssue i){ProjectInfo p=projectMapper.selectProjectInfoById(i.getProjectId());if(p==null)throw new ServiceException("项目不存在");ProjectWbsNode wp=null;if(i.getWorkPackageId()!=null){wp=wbsMapper.selectById(i.getWorkPackageId());if(wp==null||!i.getProjectId().equals(wp.getProjectId()))throw new ServiceException("关联工作包不属于当前项目");}if(i.getTaskId()!=null){ProjectTask t=taskMapper.selectById(i.getTaskId());if(t==null||!i.getProjectId().equals(t.getProjectId()))throw new ServiceException("关联任务不属于当前项目");if(wp!=null&&!wp.getWbsId().equals(t.getWorkPackageId()))throw new ServiceException("关联任务不属于所选工作包");i.setWorkPackageId(t.getWorkPackageId());}if(i.getOwnerId()!=null&&!teamService.isActiveMember(i.getProjectId(),i.getOwnerId()))throw new ServiceException("问题负责人必须是当前项目在组成员");}
}
