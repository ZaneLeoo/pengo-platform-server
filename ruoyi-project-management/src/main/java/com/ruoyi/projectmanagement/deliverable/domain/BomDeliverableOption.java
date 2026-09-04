package com.ruoyi.projectmanagement.deliverable.domain;

import lombok.Data;

/** 可作为项目交付对象的 BOM 版本。 */
@Data
public class BomDeliverableOption {

    /** BOM 主数据ID。 */
    private Long bomMasterId;

    /** BOM 编码。 */
    private String bomCode;

    /** 母件编码。 */
    private String parentItemCode;

    /** 母件名称。 */
    private String parentItemName;

    /** BOM 版本ID，作为交付关联业务ID。 */
    private Long bomVersionId;

    /** BOM 版本号。 */
    private String versionCode;

    /** BOM 版本名称。 */
    private String versionName;

    /** BOM 版本状态。 */
    private String status;

    /** BOM 版本审批状态。 */
    private String approveStatus;
}
