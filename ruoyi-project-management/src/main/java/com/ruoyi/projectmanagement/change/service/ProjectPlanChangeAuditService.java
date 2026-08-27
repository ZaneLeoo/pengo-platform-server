package com.ruoyi.projectmanagement.change.service;

import com.ruoyi.projectmanagement.change.domain.ProjectPlanChangeAudit;
import com.ruoyi.projectmanagement.change.mapper.ProjectPlanChangeMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 记录不应随业务回滚丢失的变更失败审计。 */
@Service
public class ProjectPlanChangeAuditService {
    private final ProjectPlanChangeMapper mapper;

    public ProjectPlanChangeAuditService(ProjectPlanChangeMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordApplyFailure(Long changeId, Long userId, String operator, String detail) {
        ProjectPlanChangeAudit audit = new ProjectPlanChangeAudit();
        audit.setChangeId(changeId);
        audit.setAction("APPLY_FAILED");
        audit.setOperatorUserId(userId);
        audit.setOperator(operator);
        audit.setDetail(detail);
        mapper.insertAudit(audit);
    }
}
