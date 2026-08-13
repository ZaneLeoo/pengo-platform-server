package com.ruoyi.projectmanagement.phase.service;

import com.ruoyi.projectmanagement.phase.domain.ProjectPhase;
import java.util.List;

/**
 * 项目阶段业务接口。
 */
public interface IProjectPhaseService {

    /** 查询项目阶段列表。 */
    List<ProjectPhase> list(ProjectPhase phase);

    /** 根据ID查询阶段。 */
    ProjectPhase get(Long id);

    /** 新增阶段。 */
    int add(ProjectPhase phase, String operator);

    /** 修改阶段。 */
    int edit(ProjectPhase phase, String operator);

    /** 删除阶段。 */
    int remove(Long id, String operator);

    /** 阶段生命周期动作（开始/暂停/恢复/完成）。 */
    int lifecycle(Long id, String action, String operator);

    /** 判断项目全部阶段是否已完成。 */
    boolean allCompleted(Long projectId);
}
