package com.ruoyi.projectmanagement.wbs.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.projectmanagement.common.enums.ProjectStatus;
import com.ruoyi.projectmanagement.common.enums.WbsNodeType;
import com.ruoyi.projectmanagement.common.enums.WbsStatus;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import com.ruoyi.projectmanagement.team.service.IProjectTeamService;
import com.ruoyi.projectmanagement.wbs.domain.ProjectWbsNode;
import com.ruoyi.projectmanagement.wbs.mapper.ProjectWbsMapper;
import com.ruoyi.projectmanagement.wbs.service.IProjectWbsService;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectWbsServiceImpl implements IProjectWbsService {
    private final ProjectWbsMapper mapper; private final ProjectInfoMapper projectMapper; private final IProjectTeamService teamService;
    public ProjectWbsServiceImpl(ProjectWbsMapper mapper,ProjectInfoMapper projectMapper,IProjectTeamService teamService){this.mapper=mapper;this.projectMapper=projectMapper;this.teamService=teamService;}
    public List<ProjectWbsNode> list(ProjectWbsNode filter){return mapper.selectList(filter);}
    public ProjectWbsNode get(Long id){return required(id);}
    @Transactional public int add(ProjectWbsNode node,String op){assertStructureMutable(node.getProjectId());normalize(node);validate(node,null);node.setWbsCode(nextCode(node.getProjectId(),node.getParentId()));node.setStatus(WbsStatus.NOT_STARTED.name());node.setProgress(0);node.setCreateBy(op);return mapper.insert(node);}
    @Transactional public int edit(ProjectWbsNode node,String op){ProjectWbsNode old=required(node.getWbsId());assertStructureMutable(old.getProjectId());node.setProjectId(old.getProjectId());normalize(node);validate(node,old);boolean moved=!node.getParentId().equals(old.getParentId());node.setUpdateBy(op);int rows=mapper.update(node);if(moved){recodeChildren(old.getProjectId(),old.getParentId(),"");recodeChildren(old.getProjectId(),node.getParentId(),"");}refreshProject(old.getProjectId());return rows;}
    @Transactional public int remove(Long id,String op){ProjectWbsNode n=required(id);assertStructureMutable(n.getProjectId());if(mapper.countChildren(id)>0||mapper.countTasks(id)>0||mapper.countDeliverables(id)>0)throw new ServiceException("WBS节点非空，不能删除");int rows=mapper.deleteById(id);recodeChildren(n.getProjectId(),n.getParentId(),"");refreshProject(n.getProjectId());return rows;}
    public boolean allWorkPackagesCompleted(Long projectId){ProjectWbsNode f=new ProjectWbsNode();f.setProjectId(projectId);f.setNodeType(WbsNodeType.WORK_PACKAGE.name());List<ProjectWbsNode> p=mapper.selectList(f);return !p.isEmpty()&&p.stream().allMatch(x->WbsStatus.COMPLETED.matches(x.getStatus()));}
    @Transactional public void refreshProject(Long projectId){List<ProjectWbsNode> all=listForProject(projectId);all.stream().sorted(Comparator.comparingInt(x->-depth(x,all))).forEach(x->refreshNode(x,all));ProjectInfo p=projectMapper.selectProjectInfoById(projectId);if(p!=null){List<ProjectWbsNode> roots=all.stream().filter(x->x.getParentId()==0).toList();p.setProgress(roots.isEmpty()?0:(int)Math.round(roots.stream().mapToInt(x->x.getProgress()==null?0:x.getProgress()).average().orElse(0)));p.setUpdateBy("system");projectMapper.updateLifecycle(p);}}
    private void refreshNode(ProjectWbsNode n,List<ProjectWbsNode> all){if(WbsNodeType.WORK_PACKAGE.matches(n.getNodeType()))return;List<ProjectWbsNode> c=all.stream().filter(x->n.getWbsId().equals(x.getParentId())).toList();if(c.isEmpty()){mapper.updateAggregate(n.getWbsId(),null,null,WbsStatus.NOT_STARTED.name(),0);return;}LocalDate start=c.stream().map(ProjectWbsNode::getPlanStartDate).filter(x->x!=null).min(LocalDate::compareTo).orElse(null);LocalDate end=c.stream().map(ProjectWbsNode::getPlanEndDate).filter(x->x!=null).max(LocalDate::compareTo).orElse(null);int progress=(int)Math.round(c.stream().mapToInt(x->x.getProgress()==null?0:x.getProgress()).average().orElse(0));String status=progress==100?WbsStatus.COMPLETED.name():progress>0?WbsStatus.ACTIVE.name():WbsStatus.NOT_STARTED.name();mapper.updateAggregate(n.getWbsId(),start,end,status,progress);n.setPlanStartDate(start);n.setPlanEndDate(end);n.setStatus(status);n.setProgress(progress);}
    private void validate(ProjectWbsNode n,ProjectWbsNode old){ProjectInfo p=projectMapper.selectProjectInfoById(n.getProjectId());if(p==null)throw new ServiceException("项目不存在");if(n.getParentId()!=0){ProjectWbsNode parent=required(n.getParentId());if(!n.getProjectId().equals(parent.getProjectId()))throw new ServiceException("上级WBS不属于当前项目");if(WbsNodeType.WORK_PACKAGE.matches(parent.getNodeType()))throw new ServiceException("工作包不能包含下级WBS");if(old!=null&&isDescendant(n.getWbsId(),parent))throw new ServiceException("不能移动到自己的下级节点");}if(WbsNodeType.WORK_PACKAGE.matches(n.getNodeType())){if(n.getOwnerId()==null||!teamService.isActiveMember(n.getProjectId(),n.getOwnerId()))throw new ServiceException("工作包负责人必须是当前项目在组成员");if(n.getPlanStartDate()==null||n.getPlanEndDate()==null)throw new ServiceException("请填写工作包计划日期");if(n.getPlanEndDate().isBefore(n.getPlanStartDate()))throw new ServiceException("工作包结束日期不能早于开始日期");if(n.getPlanStartDate().isBefore(p.getStartDate())||n.getPlanEndDate().isAfter(p.getEndDate()))throw new ServiceException("工作包计划日期必须在项目周期内（"+p.getStartDate()+" ~ "+p.getEndDate()+"）");if(n.getAcceptanceCriteria()==null||n.getAcceptanceCriteria().isBlank()||n.getDefinitionOfDone()==null||n.getDefinitionOfDone().isBlank())throw new ServiceException("工作包必须填写验收标准和完成定义");if(old!=null&&WbsNodeType.SUMMARY.matches(old.getNodeType())&&mapper.countChildren(old.getWbsId())>0)throw new ServiceException("汇总WBS仍有下级，不能转为工作包");}else if(old!=null&&WbsNodeType.WORK_PACKAGE.matches(old.getNodeType())&&(mapper.countTasks(old.getWbsId())>0||mapper.countDeliverables(old.getWbsId())>0))throw new ServiceException("工作包已有任务或交付物，不能转为汇总WBS");}
    private boolean isDescendant(Long id,ProjectWbsNode candidate){Long p=candidate.getParentId();while(p!=null&&p!=0){if(id.equals(p))return true;p=required(p).getParentId();}return false;}
    private void normalize(ProjectWbsNode n){if(n.getParentId()==null)n.setParentId(0L);if(n.getSortOrder()==null)n.setSortOrder(0);}
    private String nextCode(Long projectId,Long parentId){List<ProjectWbsNode> siblings=mapper.selectChildren(projectId,parentId);String prefix=parentId==0?"":required(parentId).getWbsCode()+".";return prefix+(siblings.size()+1);}
    private void recodeChildren(Long projectId,Long parentId,String ignored){List<ProjectWbsNode> children=mapper.selectChildren(projectId,parentId);String prefix=parentId==0?"":required(parentId).getWbsCode()+".";for(int i=0;i<children.size();i++){ProjectWbsNode c=children.get(i);String code=prefix+(i+1);mapper.updateCode(c.getWbsId(),code);c.setWbsCode(code);recodeChildren(projectId,c.getWbsId(),code);}}
    private int depth(ProjectWbsNode n,List<ProjectWbsNode> all){int d=0;Long p=n.getParentId();while(p!=null&&p!=0){d++;Long id=p;p=all.stream().filter(x->x.getWbsId().equals(id)).map(ProjectWbsNode::getParentId).findFirst().orElse(0L);}return d;}
    private List<ProjectWbsNode> listForProject(Long id){ProjectWbsNode f=new ProjectWbsNode();f.setProjectId(id);return mapper.selectList(f);}
    private ProjectWbsNode required(Long id){ProjectWbsNode n=mapper.selectById(id);if(n==null)throw new ServiceException("WBS节点不存在");return n;}
    private void assertStructureMutable(Long id){ProjectInfo p=projectMapper.selectProjectInfoById(id);if(p==null)throw new ServiceException("项目不存在");if(!ProjectStatus.APPROVED.matches(p.getStatus()))throw new ServiceException("只有已立项待启动项目可以调整计划结构");}
}
