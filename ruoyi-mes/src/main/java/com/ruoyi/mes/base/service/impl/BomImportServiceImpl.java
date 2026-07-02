package com.ruoyi.mes.base.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.base.domain.BomImportItem;
import com.ruoyi.mes.base.domain.BomImportTask;
import com.ruoyi.mes.base.domain.dto.BomImportCreateRequest;
import com.ruoyi.mes.base.domain.dto.BomImportDraft;
import com.ruoyi.mes.base.domain.dto.BomImportDraftItem;
import com.ruoyi.mes.base.domain.ocr.BomOcrDocument;
import com.ruoyi.mes.base.domain.ocr.BomOcrIssue;
import com.ruoyi.mes.base.domain.ocr.BomOcrItem;
import com.ruoyi.mes.base.domain.ocr.BomOcrResult;
import com.ruoyi.mes.base.mapper.BomImportItemMapper;
import com.ruoyi.mes.base.mapper.BomImportTaskMapper;
import com.ruoyi.mes.base.service.IBomImportService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** BOM OCR 导入草稿服务实现。 */
@Service
public class BomImportServiceImpl implements IBomImportService
{
    private static final String STATUS_PROCESSING = "processing";
    private static final String STATUS_RECOGNIZED = "recognized";
    private static final String STATUS_VALIDATED = "validated";
    private static final String STATUS_FAILED = "failed";
    private static final String RISK_OK = "ok";
    private static final String RISK_WARNING = "warning";
    private static final String RISK_ERROR = "error";
    private static final String MATCH_MISSING = "missing";

    @Autowired
    private BomImportTaskMapper taskMapper;

    @Autowired
    private BomImportItemMapper itemMapper;

    @Autowired
    private BomImportDraftValidator validator;

    @Override
    public List<BomImportTask> selectBomImportTaskList(BomImportTask task)
    {
        return taskMapper.selectBomImportTaskList(task);
    }

    @Override
    public BomImportDraft selectBomImportDraftById(Long id)
    {
        BomImportTask task = requireTask(id);
        return toDraft(task, loadItems(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BomImportDraft createProcessingDraft(BomImportCreateRequest request, String username)
    {
        if (request == null)
        {
            throw new ServiceException("识别任务不能为空");
        }
        BomImportTask task = new BomImportTask();
        task.setFileName(request.getFileName());
        task.setFileUrl(request.getFileUrl());
        task.setFileType(request.getFileType());
        task.setStatus(STATUS_PROCESSING);
        task.setCreateBy(username);
        taskMapper.insertBomImportTask(task);
        return selectBomImportDraftById(task.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BomImportDraft createDraft(BomImportCreateRequest request, String username)
    {
        if (request == null || request.getResult() == null)
        {
            throw new ServiceException("识别结果不能为空");
        }
        BomOcrResult result = request.getResult();
        List<BomOcrIssue> issues = mergeIssues(result.getIssues(), validator.validate(result));

        BomImportTask task = toTask(request, result, issues, username);
        taskMapper.insertBomImportTask(task);
        for (BomOcrItem source : safeItems(result.getItems()))
        {
            BomImportItem item = toImportItem(task.getId(), source, username);
            itemMapper.insertBomImportItem(item);
        }
        return selectBomImportDraftById(task.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BomImportDraft completeRecognition(Long id, BomImportCreateRequest request, String username)
    {
        requireTask(id);
        if (request == null || request.getResult() == null)
        {
            throw new ServiceException("识别结果不能为空");
        }
        BomOcrResult result = request.getResult();
        List<BomOcrIssue> issues = mergeIssues(result.getIssues(), validator.validate(result));

        BomImportTask task = toTask(id, request, result, issues, username);
        taskMapper.updateBomImportTask(task);
        itemMapper.deleteBomImportItemByImportIds(new Long[] {id});
        for (BomOcrItem source : safeItems(result.getItems()))
        {
            BomImportItem item = toImportItem(id, source, username);
            itemMapper.insertBomImportItem(item);
        }
        return selectBomImportDraftById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRecognitionFailed(Long id, String errorMessage, String username)
    {
        requireTask(id);
        BomImportTask task = new BomImportTask();
        task.setId(id);
        task.setStatus(STATUS_FAILED);
        task.setErrorMessage(StringUtils.isBlank(errorMessage) ? "BOM OCR 识别失败" : errorMessage);
        task.setUpdateBy(username);
        taskMapper.updateBomImportTask(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BomImportDraft updateDraft(Long id, BomImportDraft draft, String username)
    {
        requireTask(id);
        BomOcrResult result = toOcrResult(draft);
        List<BomOcrIssue> issues = validator.validate(result);

        BomImportTask task = toTask(id, draft, issues, username);
        taskMapper.updateBomImportTask(task);
        itemMapper.deleteBomImportItemByImportIds(new Long[] {id});
        for (BomImportDraftItem source : safeDraftItems(draft.getItems()))
        {
            BomImportItem item = toImportItem(id, source, username);
            itemMapper.insertBomImportItem(item);
        }
        return selectBomImportDraftById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BomOcrIssue> validateDraft(Long id, String username)
    {
        BomImportDraft draft = selectBomImportDraftById(id);
        List<BomOcrIssue> issues = validator.validate(toOcrResult(draft));
        BomImportTask task = new BomImportTask();
        task.setId(id);
        task.setStatus(STATUS_VALIDATED);
        task.setIssuesJson(JSON.toJSONString(issues));
        task.setUpdateBy(username);
        taskMapper.updateBomImportTask(task);
        return issues;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBomImportTaskByIds(Long[] ids)
    {
        itemMapper.deleteBomImportItemByImportIds(ids);
        return taskMapper.deleteBomImportTaskByIds(ids);
    }

    private BomImportTask requireTask(Long id)
    {
        BomImportTask task = taskMapper.selectBomImportTaskById(id);
        if (task == null)
        {
            throw new ServiceException("未找到 BOM 导入草稿");
        }
        return task;
    }

    private List<BomImportItem> loadItems(Long importId)
    {
        BomImportItem query = new BomImportItem();
        query.setImportId(importId);
        return itemMapper.selectBomImportItemList(query);
    }

    private BomImportTask toTask(BomImportCreateRequest request, BomOcrResult result, List<BomOcrIssue> issues, String username)
    {
        BomImportTask task = fillTaskDocument(new BomImportTask(), result.getDocument());
        task.setFileName(request.getFileName());
        task.setFileUrl(request.getFileUrl());
        task.setFileType(request.getFileType());
        task.setStatus(STATUS_RECOGNIZED);
        task.setRawResultJson(JSON.toJSONString(result));
        task.setIssuesJson(JSON.toJSONString(issues));
        task.setCreateBy(username);
        return task;
    }

    private BomImportTask toTask(Long id, BomImportCreateRequest request, BomOcrResult result,
        List<BomOcrIssue> issues, String username)
    {
        BomImportTask task = fillTaskDocument(new BomImportTask(), result.getDocument());
        task.setId(id);
        task.setFileName(request.getFileName());
        task.setFileUrl(request.getFileUrl());
        task.setFileType(request.getFileType());
        task.setStatus(STATUS_RECOGNIZED);
        task.setRawResultJson(JSON.toJSONString(result));
        task.setIssuesJson(JSON.toJSONString(issues));
        task.setErrorMessage("");
        task.setUpdateBy(username);
        return task;
    }

    private BomImportTask toTask(Long id, BomImportDraft draft, List<BomOcrIssue> issues, String username)
    {
        BomImportTask task = fillTaskDocument(new BomImportTask(), draft.getDocument());
        task.setId(id);
        task.setFileName(draft.getFileName());
        task.setFileUrl(draft.getFileUrl());
        task.setFileType(draft.getFileType());
        task.setStatus(StringUtils.isBlank(draft.getStatus()) ? STATUS_RECOGNIZED : draft.getStatus());
        task.setIssuesJson(JSON.toJSONString(issues));
        task.setUpdateBy(username);
        return task;
    }

    private BomImportTask fillTaskDocument(BomImportTask task, BomOcrDocument document)
    {
        if (document == null) return task;
        task.setTitle(document.getTitle());
        task.setParentNameCandidate(document.getParentNameCandidate());
        task.setParentCodeCandidate(document.getParentCodeCandidate());
        task.setProductModel(document.getProductModel());
        task.setDrawingNo(document.getDrawingNo());
        task.setRevision(document.getRevision());
        task.setBaseQtyCandidate(document.getBaseQtyCandidate());
        task.setTotalRows(document.getTotalRows());
        task.setUnitWeight(document.getUnitWeight());
        task.setExtraFieldsJson(JSON.toJSONString(document.getExtraFields()));
        return task;
    }

    private BomImportItem toImportItem(Long importId, BomOcrItem source, String username)
    {
        BomImportItem item = new BomImportItem();
        item.setImportId(importId);
        item.setLineNo(source.getLineNo());
        item.setComponentCodeCandidate(source.getComponentCodeCandidate());
        item.setDrawingNo(source.getDrawingNo());
        item.setItemName(source.getItemName());
        item.setQuantity(source.getQuantity());
        item.setSpec(source.getSpec());
        item.setUnit(source.getUnit());
        item.setUnitWeight(source.getUnitWeight());
        item.setTotalWeight(source.getTotalWeight());
        item.setRemark(source.getRemark());
        item.setRawText(source.getRawText());
        item.setConfidence(source.getConfidence());
        item.setMatchStatus(MATCH_MISSING);
        item.setRiskLevel(resolveRiskLevel(source));
        item.setIssueMessage(resolveIssueMessage(source));
        item.setCreateBy(username);
        return item;
    }

    private BomImportItem toImportItem(Long importId, BomImportDraftItem source, String username)
    {
        BomImportItem item = new BomImportItem();
        item.setImportId(importId);
        item.setLineNo(source.getLineNo());
        item.setComponentCodeCandidate(source.getComponentCodeCandidate());
        item.setDrawingNo(source.getDrawingNo());
        item.setItemName(source.getItemName());
        item.setQuantity(source.getQuantity());
        item.setSpec(source.getSpec());
        item.setUnit(source.getUnit());
        item.setUnitWeight(source.getUnitWeight());
        item.setTotalWeight(source.getTotalWeight());
        item.setRemark(source.getRemark());
        item.setRawText(source.getRawText());
        item.setConfidence(source.getConfidence());
        item.setMatchedMaterialId(source.getMatchedMaterialId());
        item.setMatchedMaterialCode(source.getMatchedMaterialCode());
        item.setMatchedMaterialName(source.getMatchedMaterialName());
        item.setMatchStatus(source.getMatchStatus());
        item.setRiskLevel(source.getRiskLevel());
        item.setIssueMessage(source.getIssueMessage());
        item.setCreateBy(username);
        return item;
    }

    private BomImportDraft toDraft(BomImportTask task, List<BomImportItem> items)
    {
        BomImportDraft draft = new BomImportDraft();
        draft.setId(task.getId());
        draft.setStatus(task.getStatus());
        draft.setFileName(task.getFileName());
        draft.setFileUrl(task.getFileUrl());
        draft.setFileType(task.getFileType());
        draft.setErrorMessage(task.getErrorMessage());
        draft.setDocument(toDocument(task));
        draft.setItems(items.stream().map(this::toDraftItem).toList());
        draft.setIssues(parseIssues(task.getIssuesJson()));
        draft.setCreateTime(task.getCreateTime());
        draft.setUpdateTime(task.getUpdateTime());
        return draft;
    }

    private BomOcrDocument toDocument(BomImportTask task)
    {
        BomOcrDocument document = new BomOcrDocument();
        document.setTitle(task.getTitle());
        document.setParentNameCandidate(task.getParentNameCandidate());
        document.setParentCodeCandidate(task.getParentCodeCandidate());
        document.setProductModel(task.getProductModel());
        document.setDrawingNo(task.getDrawingNo());
        document.setRevision(task.getRevision());
        document.setBaseQtyCandidate(task.getBaseQtyCandidate());
        document.setTotalRows(task.getTotalRows());
        document.setUnitWeight(task.getUnitWeight());
        document.setExtraFields(parseExtraFields(task.getExtraFieldsJson()));
        return document;
    }

    private BomImportDraftItem toDraftItem(BomImportItem source)
    {
        BomImportDraftItem item = new BomImportDraftItem();
        item.setId(source.getId());
        item.setLineNo(source.getLineNo());
        item.setComponentCodeCandidate(source.getComponentCodeCandidate());
        item.setDrawingNo(source.getDrawingNo());
        item.setItemName(source.getItemName());
        item.setQuantity(source.getQuantity());
        item.setSpec(source.getSpec());
        item.setUnit(source.getUnit());
        item.setUnitWeight(source.getUnitWeight());
        item.setTotalWeight(source.getTotalWeight());
        item.setRemark(source.getRemark());
        item.setRawText(source.getRawText());
        item.setConfidence(source.getConfidence());
        item.setMatchedMaterialId(source.getMatchedMaterialId());
        item.setMatchedMaterialCode(source.getMatchedMaterialCode());
        item.setMatchedMaterialName(source.getMatchedMaterialName());
        item.setMatchStatus(source.getMatchStatus());
        item.setRiskLevel(source.getRiskLevel());
        item.setIssueMessage(source.getIssueMessage());
        return item;
    }

    private BomOcrResult toOcrResult(BomImportDraft draft)
    {
        BomOcrResult result = new BomOcrResult();
        result.setVersion("1.0");
        result.setDocument(draft.getDocument());
        List<BomOcrItem> items = new ArrayList<>();
        for (BomImportDraftItem draftItem : safeDraftItems(draft.getItems()))
        {
            BomOcrItem item = new BomOcrItem();
            item.setLineNo(draftItem.getLineNo());
            item.setComponentCodeCandidate(draftItem.getComponentCodeCandidate());
            item.setDrawingNo(draftItem.getDrawingNo());
            item.setItemName(draftItem.getItemName());
            item.setQuantity(draftItem.getQuantity());
            item.setSpec(draftItem.getSpec());
            item.setUnit(draftItem.getUnit());
            item.setUnitWeight(draftItem.getUnitWeight());
            item.setTotalWeight(draftItem.getTotalWeight());
            item.setRemark(draftItem.getRemark());
            item.setRawText(draftItem.getRawText());
            item.setConfidence(draftItem.getConfidence());
            items.add(item);
        }
        result.setItems(items);
        return result;
    }

    private List<BomOcrIssue> mergeIssues(List<BomOcrIssue> rawIssues, List<BomOcrIssue> validationIssues)
    {
        List<BomOcrIssue> merged = new ArrayList<>();
        if (rawIssues != null) merged.addAll(rawIssues);
        if (validationIssues != null) merged.addAll(validationIssues);
        return merged;
    }

    private List<BomOcrIssue> parseIssues(String value)
    {
        if (StringUtils.isBlank(value)) return new ArrayList<>();
        return JSON.parseArray(value, BomOcrIssue.class);
    }

    private Map<String, Object> parseExtraFields(String value)
    {
        if (StringUtils.isBlank(value)) return new LinkedHashMap<>();
        return JSON.parseObject(value, new TypeReference<Map<String, Object>>() {});
    }

    private List<BomOcrItem> safeItems(List<BomOcrItem> items)
    {
        return items == null ? new ArrayList<>() : items;
    }

    private List<BomImportDraftItem> safeDraftItems(List<BomImportDraftItem> items)
    {
        return items == null ? new ArrayList<>() : items;
    }

    private String resolveRiskLevel(BomOcrItem item)
    {
        if (StringUtils.isBlank(item.getItemName()) || item.getQuantity() == null)
        {
            return RISK_ERROR;
        }
        if (StringUtils.isBlank(item.getComponentCodeCandidate()) && StringUtils.isBlank(item.getDrawingNo()))
        {
            return RISK_WARNING;
        }
        return RISK_OK;
    }

    private String resolveIssueMessage(BomOcrItem item)
    {
        if (StringUtils.isBlank(item.getItemName())) return "缺少子件名称";
        if (item.getQuantity() == null) return "缺少子件数量";
        if (StringUtils.isBlank(item.getComponentCodeCandidate()) && StringUtils.isBlank(item.getDrawingNo()))
        {
            return "缺少子件编码候选和图号";
        }
        return "";
    }
}
