package com.ruoyi.projectmanagement.deliverable.service;

import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverable;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverableSubmission;
import java.util.List;

/**
 * 项目交付物业务接口。
 */
public interface IProjectDeliverableService {

    /** 查询交付物列表。 */
    List<ProjectDeliverable> selectList(ProjectDeliverable entity);

    /** 根据ID查询交付物。 */
    ProjectDeliverable selectById(Long id);

    /** 新增交付物。 */
    int insert(ProjectDeliverable entity);

    /** 修改交付物。 */
    int update(ProjectDeliverable entity);

    /** 批量删除交付物。 */
    int deleteByIds(Long[] ids);

    /** 提交交付物（含文件或外部链接）。 */
    void submit(Long id, ProjectDeliverableSubmission submission, String username);

    /** 审核交付物提交（approve 或驳回）。 */
    void review(Long id, ProjectDeliverableSubmission submission, String username);

    /** 查询交付物提交与审核历史。 */
    List<ProjectDeliverableSubmission> selectSubmissions(Long id);
}
