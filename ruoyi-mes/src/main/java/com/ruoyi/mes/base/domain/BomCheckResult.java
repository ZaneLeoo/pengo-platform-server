package com.ruoyi.mes.base.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * BOM完整性检查结果。
 *
 * @author ruoyi
 */
public class BomCheckResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long bomVersionId;
    private String versionCode;
    private String status;
    private String approveStatus;
    private List<BomCheckIssue> issues = new ArrayList<>();

    public Long getBomVersionId() {
        return bomVersionId;
    }

    public void setBomVersionId(Long bomVersionId) {
        this.bomVersionId = bomVersionId;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApproveStatus() {
        return approveStatus;
    }

    public void setApproveStatus(String approveStatus) {
        this.approveStatus = approveStatus;
    }

    public List<BomCheckIssue> getIssues() {
        return issues;
    }

    public void setIssues(List<BomCheckIssue> issues) {
        this.issues = issues;
    }

    public void addIssue(BomCheckIssue issue) {
        this.issues.add(issue);
    }
}
