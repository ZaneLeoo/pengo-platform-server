package com.ruoyi.web.domain.dto;

import lombok.Data;

/**
 * BOM AI 导入 — 已建档或复用的物料结果。
 */
@Data
public class BomAiProvisionedMaterial {

    /** 物料主键 ID。 */
    private Long materialId;

    /** 物料编码。 */
    private String materialCode;

    /** 物料名称。 */
    private String materialName;

    /** 物料类型。 */
    private String materialType;

    /** 来源类型。 */
    private String sourceType;

    /** 分类 ID。 */
    private Long categoryId;

    /** 规格。 */
    private String spec;

    /** 库存基准单位。 */
    private String unit;

    /** 计量单位组编码。 */
    private String unitGroupCode;

    /** true 表示本次新建，false 表示复用系统中已存在的启用物料。 */
    private boolean created;
}
