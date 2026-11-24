package com.ruoyi.web.domain.dto;

import java.util.List;
import lombok.Data;

/**
 * BOM AI 导入 — 批量一键建档请求。
 */
@Data
public class BomAiQuickCreateMaterialsRequest {

    /** 待建档物料列表。 */
    private List<BomAiMaterialDraft> materials;
}
