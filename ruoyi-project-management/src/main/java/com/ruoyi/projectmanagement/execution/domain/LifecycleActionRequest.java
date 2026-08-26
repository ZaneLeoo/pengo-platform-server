package com.ruoyi.projectmanagement.execution.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 项目或WBS任务的生命周期动作请求。 */
@Data
public class LifecycleActionRequest {
    /** START、PAUSE、RESUME、COMPLETE。 */
    @NotBlank(message = "生命周期动作不能为空")
    private String action;

    /** 暂停时必填；其余动作可为空。 */
    private String reason;
}
