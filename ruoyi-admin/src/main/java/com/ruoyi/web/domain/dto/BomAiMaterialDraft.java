package com.ruoyi.web.domain.dto;

import lombok.Data;

/**
 * BOM AI 导入 — 待建档物料草稿。
 */
@Data
public class BomAiMaterialDraft {

    /** 物料编码（必填）。 */
    private String materialCode;

    /** 物料名称（必填）。 */
    private String materialName;

    /** 物料类型：FINISHED / SEMI_FINISHED / RAW / PACKAGE。 */
    private String materialType;

    /** 来源类型：MAKE / PURCHASE / OUTSOURCE。 */
    private String sourceType;

    /** 物料分类 ID（可空，未传时按物料类型尝试自动匹配）。 */
    private Long categoryId;

    /** 规格。 */
    private String spec;

    /** 型号。 */
    private String model;

    /** 库存基准单位（未传时默认“个”）。 */
    private String unit;

    /** 计量单位组编码（未传时自动使用通用计数单位组 AI_GENERAL）。 */
    private String unitGroupCode;

    /** 图号。 */
    private String drawingNo;

    /** 物料版本/版次。 */
    private String materialVersion;

    /** 备注。 */
    private String remark;

    /** 在图纸中的角色：PARENT（母件）/ COMPONENT（子件），用于推导默认类型。 */
    private String role;
}
