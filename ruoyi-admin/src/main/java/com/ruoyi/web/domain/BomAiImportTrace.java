package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.web.domain.dto.BomAiImportedBom;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** BOM AI 图纸识别与导入追溯记录。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BomAiImportTrace extends BaseEntity {
    private Long id;
    private String importNo;
    private String status;
    private Integer fileCount;
    private Integer recognizedBomCount;

    /** 从提交识别到返回预览的总耗时（毫秒），包含文件上传和 Dify 工作流调用。 */
    private Long recognitionDurationMs;

    private String sourceFingerprint;
    private String sourceFiles;
    private String rawDifyOutputs;
    private String previewPayload;
    private String errorMessage;
    private String importedBomMasterIds;
    private String importedBomVersionIds;
    private Date recognizedTime;
    private Date confirmedTime;
    private Date cancelledTime;
    private Date expiredTime;

    /** 详情接口动态组装，不落库。 */
    private List<BomAiImportedBom> importedBoms;
}
