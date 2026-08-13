package com.ruoyi.projectmanagement.wbs.service;

import com.ruoyi.projectmanagement.wbs.domain.ProjectWbsNode;
import java.util.List;

/**
 * WBS范围树与工作包业务。
 */
public interface IProjectWbsService {

    /** 查询WBS节点列表。 */
    List<ProjectWbsNode> list(ProjectWbsNode filter);

    /** 查询WBS节点详细。 */
    ProjectWbsNode get(Long id);

    /** 新增WBS节点，返回新节点ID。 */
    Long add(ProjectWbsNode node, String operator);

    /** 修改WBS节点。 */
    int edit(ProjectWbsNode node, String operator);

    /** 删除WBS节点。 */
    int remove(Long id, String operator);

    /** 刷新项目WBS汇总状态与进度。 */
    void refreshProject(Long projectId);

    /** 判断项目是否所有工作包均已完成。 */
    boolean allWorkPackagesCompleted(Long projectId);
}
