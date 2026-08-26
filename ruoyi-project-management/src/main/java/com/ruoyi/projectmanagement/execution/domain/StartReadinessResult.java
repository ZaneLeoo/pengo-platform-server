package com.ruoyi.projectmanagement.execution.domain;

import java.util.List;

/** 项目启动前检查结果。 */
public class StartReadinessResult {

    /** 是否全部满足启动条件。 */
    private boolean passed;

    /** 未满足条件的检查项说明列表，通过时为空。 */
    private List<String> issues;

    public StartReadinessResult() {}

    public StartReadinessResult(boolean passed, List<String> issues) {
        this.passed = passed;
        this.issues = issues;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public List<String> getIssues() {
        return issues;
    }

    public void setIssues(List<String> issues) {
        this.issues = issues;
    }
}
