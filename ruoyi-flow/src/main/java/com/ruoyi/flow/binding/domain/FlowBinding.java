package com.ruoyi.flow.binding.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程与业务模块绑定：一个业务类型绑定一个启用流程。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FlowBinding extends BaseEntity {

    /** 绑定ID。 */
    private Long bindingId;

    /** 业务类型编码，如 PROJECT_INITIATION。 */
    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    /** 绑定的流程定义ID。 */
    @NotNull(message = "流程不能为空")
    private Long flowId;
}
