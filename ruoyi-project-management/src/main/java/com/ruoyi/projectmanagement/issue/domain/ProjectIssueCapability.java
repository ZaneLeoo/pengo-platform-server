package com.ruoyi.projectmanagement.issue.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 当前登录人在指定项目中的问题创建能力。 */
@Data
@AllArgsConstructor
public class ProjectIssueCapability {
    /** 是否允许创建问题。 */
    private boolean canCreate;

    /** 不允许创建时的可读原因。 */
    private String readOnlyReason;
}
