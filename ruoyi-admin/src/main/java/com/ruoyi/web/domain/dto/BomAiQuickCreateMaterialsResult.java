package com.ruoyi.web.domain.dto;

import java.util.List;
import lombok.Data;

/**
 * BOM AI 导入 — 批量一键建档结果。
 */
@Data
public class BomAiQuickCreateMaterialsResult {

    /** 本次新建的物料数量。 */
    private int createdCount;

    /** 复用已有启用物料的数量。 */
    private int reusedCount;

    /** 建档/复用后的物料明细列表。 */
    private List<BomAiProvisionedMaterial> materials;
}
