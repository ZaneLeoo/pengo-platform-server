package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;
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
    private String sourceFiles;
    private String rawDifyOutputs;
    private String previewPayload;
    private String errorMessage;
    private String importedBomMasterIds;
    private String importedBomVersionIds;
    private Date recognizedTime;
    private Date confirmedTime;
}
