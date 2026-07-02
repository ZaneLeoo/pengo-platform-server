package com.ruoyi.mes.base.service;

import com.ruoyi.mes.base.domain.BomImportTask;
import com.ruoyi.mes.base.domain.dto.BomImportApplyResult;
import com.ruoyi.mes.base.domain.dto.BomImportCreateRequest;
import com.ruoyi.mes.base.domain.dto.BomImportDraft;
import com.ruoyi.mes.base.domain.ocr.BomOcrIssue;
import java.util.List;

/** BOM OCR 导入草稿服务。 */
public interface IBomImportService
{
    List<BomImportTask> selectBomImportTaskList(BomImportTask task);
    BomImportDraft selectBomImportDraftById(Long id);
    BomImportDraft createProcessingDraft(BomImportCreateRequest request, String username);
    BomImportDraft createDraft(BomImportCreateRequest request, String username);
    BomImportDraft completeRecognition(Long id, BomImportCreateRequest request, String username);
    void markRecognitionFailed(Long id, String errorMessage, String username);
    BomImportDraft updateDraft(Long id, BomImportDraft draft, String username);
    List<BomOcrIssue> validateDraft(Long id, String username);
    BomImportApplyResult importToBomVersion(Long id, Long bomVersionId, String username);
    int deleteBomImportTaskByIds(Long[] ids);
}
