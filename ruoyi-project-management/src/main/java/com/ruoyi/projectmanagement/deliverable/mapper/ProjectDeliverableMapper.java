package com.ruoyi.projectmanagement.deliverable.mapper;

import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverable;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverableSubmission;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目交付物数据访问接口。
 */
@Mapper
public interface ProjectDeliverableMapper {

    /** 查询交付物列表。 */
    List<ProjectDeliverable> selectList(ProjectDeliverable entity);

    /** 根据ID查询交付物。 */
    ProjectDeliverable selectById(Long deliverableId);

    /** 新增交付物。 */
    int insert(ProjectDeliverable entity);

    /** 修改交付物。 */
    int update(ProjectDeliverable entity);

    /** 批量删除交付物。 */
    int deleteByIds(Long[] ids);

    /** 更新交付物状态。 */
    int updateStatus(ProjectDeliverable entity);

    /** 统计工作包未完成的必交交付物数量。 */
    int countUnsatisfiedRequiredByWorkPackageId(Long workPackageId);

    /** 统计工作包的必交正式交付物数量。 */
    int countRequiredByWorkPackageId(Long workPackageId);

    /** 查询交付物提交历史。 */
    List<ProjectDeliverableSubmission> selectSubmissions(Long deliverableId);

    /** 新增提交记录。 */
    int insertSubmission(ProjectDeliverableSubmission entity);

    /** 更新提交记录的审核结果。 */
    int updateSubmissionReview(ProjectDeliverableSubmission entity);

    /** 查询交付物下一个版本号。 */
    Integer selectNextVersion(Long deliverableId);
}
