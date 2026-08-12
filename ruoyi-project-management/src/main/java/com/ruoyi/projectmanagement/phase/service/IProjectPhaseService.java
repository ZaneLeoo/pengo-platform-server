package com.ruoyi.projectmanagement.phase.service;
import com.ruoyi.projectmanagement.phase.domain.ProjectPhase;import java.util.List;
public interface IProjectPhaseService { List<ProjectPhase> list(ProjectPhase phase); ProjectPhase get(Long id); int add(ProjectPhase phase,String operator); int edit(ProjectPhase phase,String operator); int remove(Long id,String operator); int lifecycle(Long id,String action,String operator); boolean allCompleted(Long projectId); }
