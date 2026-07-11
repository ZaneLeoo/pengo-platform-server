package com.ruoyi.mes.purchase.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

/** 送货单质检请求。 */
@Data
public class InspectionRequest {
    @Valid
    @NotEmpty(message = "请填写至少一条质检结果")
    private List<InspectionLineRequest> lines;
}
