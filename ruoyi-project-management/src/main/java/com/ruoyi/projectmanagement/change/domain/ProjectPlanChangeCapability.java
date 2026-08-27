package com.ruoyi.projectmanagement.change.domain;

import java.util.List;
import lombok.Data;

@Data
public class ProjectPlanChangeCapability {
    private boolean canEdit;
    private boolean canDelete;
    private boolean canSubmit;
    private boolean canWithdraw;
    private boolean canApply;
    private String readonlyReason;
    private List<String> allowedActions;
}
