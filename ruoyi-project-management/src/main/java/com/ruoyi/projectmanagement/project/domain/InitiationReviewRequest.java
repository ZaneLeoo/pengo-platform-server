package com.ruoyi.projectmanagement.project.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 立项审批请求。
 */
@Data
public class InitiationReviewRequest {

    /** APPROVED 或 RETURNED。 */
    @NotBlank
    private String result;

    /** 退回时必须填写。 */
    private String comment;
}
