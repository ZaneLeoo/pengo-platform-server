package com.ruoyi.projectmanagement.issue.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 问题状态流转请求。 */
@Data
public class IssueTransitionRequest {

    /** 目标状态：PROCESSING、RESOLVED、CLOSED。 */
    @NotBlank(message = "目标状态不能为空")
    private String targetStatus;
    /** 解决方案；标记解决时必填。 */
    private String resolution;
    /** 操作原因；重新打开时必填。 */
    private String reason;
}
