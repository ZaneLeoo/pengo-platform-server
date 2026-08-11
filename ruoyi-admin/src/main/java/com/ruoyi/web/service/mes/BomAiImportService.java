package com.ruoyi.web.service.mes;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.agent.application.DifyAppConfigService;
import com.ruoyi.agent.infrastructure.dify.DifyClientSettings;
import com.ruoyi.agent.infrastructure.dify.DifyWorkflowClient;
import com.ruoyi.agent.infrastructure.dify.model.DifyFileUploadRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyFileUploadResult;
import com.ruoyi.agent.infrastructure.dify.model.DifyWorkflowRunRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyWorkflowRunResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.base.domain.BomItem;
import com.ruoyi.mes.base.domain.BomMaster;
import com.ruoyi.mes.base.domain.BomVersion;
import com.ruoyi.mes.base.domain.Material;
import com.ruoyi.mes.base.mapper.BomMasterMapper;
import com.ruoyi.mes.base.service.IBomItemService;
import com.ruoyi.mes.base.service.IBomMasterService;
import com.ruoyi.mes.base.service.IBomVersionService;
import com.ruoyi.mes.base.service.IMaterialService;
import com.ruoyi.mes.common.enums.BomMasterStatus;
import com.ruoyi.mes.common.enums.BomType;
import com.ruoyi.web.domain.BomAiImportTrace;
import com.ruoyi.web.domain.dto.BomAiConfirmResult;
import com.ruoyi.web.domain.dto.BomAiDocument;
import com.ruoyi.web.domain.dto.BomAiImportConfirmRequest;
import com.ruoyi.web.domain.dto.BomAiImportHeader;
import com.ruoyi.web.domain.dto.BomAiImportItem;
import com.ruoyi.web.domain.dto.BomAiImportedBom;
import com.ruoyi.web.domain.dto.BomAiPreviewResult;
import com.ruoyi.web.domain.enums.BomAiImportTraceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BOM AI 图纸导入核心服务。
 */
@Service
public class BomAiImportService {

    private static final Logger log = LoggerFactory.getLogger(BomAiImportService.class);
    private static final String DIFY_APP_CODE = "BOM_OCR";

    @Autowired
    private DifyWorkflowClient difyWorkflowClient;
    @Autowired
    private DifyAppConfigService difyAppConfigService;
    @Autowired
    private IMaterialService materialService;
    @Autowired
    private IBomMasterService bomMasterService;
    @Autowired
    private IBomVersionService bomVersionService;
    @Autowired
    private IBomItemService bomItemService;
    @Autowired
    private BomMasterMapper bomMasterMapper;
    @Autowired
    private BomAiImportTraceService bomAiImportTraceService;

    /**
     * 上传图纸 → Dify 识别 → 物料匹配 → 返回预览数据。
     */
    public BomAiPreviewResult recognize(MultipartFile[] files) {
        return recognize(files, "admin");
    }

    /**
     * 上传图纸、调用 Dify 并保存本次识别的可追溯信息。
     */
    public BomAiPreviewResult recognize(MultipartFile[] files, String operator) {
        BomAiPreviewResult result = new BomAiPreviewResult();
        BomAiImportTrace trace = null;
        try {
            if (files == null || files.length == 0) {
                result.setSuccess(false);
                result.setError("请上传至少一张图纸");
                return result;
            }

            trace = bomAiImportTraceService.start(files, operator);
            result.setTraceId(trace.getId());
            result.setImportNo(trace.getImportNo());

            // 1. 获取 Dify 配置
            DifyClientSettings settings = difyAppConfigService.requireSettings(DIFY_APP_CODE);

            // 2. 逐个上传图纸，并按 BOM_OCR 工作流的输入变量分组。
            List<Map<String, Object>> difyImages = new ArrayList<>();
            Map<String, Object> difyPdf = null;
            List<String> difyFileIds = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                log.info("Uploading drawing {} to Dify", file.getOriginalFilename());
                DifyFileUploadResult upload = difyWorkflowClient.uploadFile(settings,
                        new DifyFileUploadRequest(
                                file.getOriginalFilename(),
                                file.getContentType(),
                                file.getBytes(),
                                DIFY_APP_CODE));
                if (StringUtils.isBlank(upload.getId())) {
                    throw new ServiceException("Dify 文件上传未返回文件 ID：" + file.getOriginalFilename());
                }
                difyFileIds.add(upload.getId());
                Map<String, Object> difyFile = new LinkedHashMap<>();
                difyFile.put("type", difyFileType(file));
                difyFile.put("transfer_method", "local_file");
                difyFile.put("upload_file_id", upload.getId());
                if (isPdf(file)) {
                    if (difyPdf != null) {
                        throw new ServiceException("一次仅支持上传一个 PDF 图纸");
                    }
                    difyPdf = difyFile;
                } else {
                    difyImages.add(difyFile);
                }
            }
            if (difyImages.isEmpty() && difyPdf == null) {
                result.setSuccess(false);
                result.setError("请上传至少一张有效图纸");
                return result;
            }

            // 3. 执行 BOM_OCR 工作流
            Map<String, Object> inputs = new HashMap<>();
            if (!difyImages.isEmpty()) {
                inputs.put("bom_images", difyImages);
            }
            if (difyPdf != null) {
                inputs.put("bom_pdf", List.of(difyPdf));
            }
            DifyWorkflowRunResult workflow = difyWorkflowClient.runBlocking(settings,
                    new DifyWorkflowRunRequest(inputs, DIFY_APP_CODE));

            if (!"succeeded".equals(workflow.getStatus())) {
                result.setSuccess(false);
                result.setError("AI 识别失败：" + (workflow.getError() != null ? workflow.getError() : "工作流未成功返回"));
                return result;
            }

            // 4. 解析工作流输出
            Map<String, Object> outputs = workflow.getOutputs();
            if (outputs == null || outputs.isEmpty()) {
                result.setSuccess(false);
                result.setError("AI 识别未返回有效数据");
                return result;
            }

            parseAndMatch(result, outputs);
            BomAiImportTrace duplicate = bomAiImportTraceService.findImportedDuplicate(trace);
            if (duplicate != null) {
                result.setDuplicateImportedTraceId(duplicate.getId());
                result.setDuplicateImportedImportNo(duplicate.getImportNo());
            }
            result.setSuccess(true);
            bomAiImportTraceService.markRecognized(trace, difyFileIds, outputs, result, operator);

        } catch (Exception e) {
            log.error("BOM AI 识别异常", e);
            if (trace != null) {
                bomAiImportTraceService.markFailed(trace, e.getMessage(), operator);
            }
            result.setSuccess(false);
            String msg = e.getMessage();
            if (msg != null && msg.contains("请先配置 Dify 应用")) {
                result.setError("请在\"系统管理 > Dify 应用配置\"中先配置 BOM_OCR 应用");
            } else {
                result.setError("识别失败：" + (msg != null ? msg : e.getClass().getSimpleName()));
            }
        }
        return result;
    }

    /**
     * 确认导入 → 一次事务写入多个独立的 bom_master + bom_version + bom_item。
     */
    @Transactional(rollbackFor = Exception.class)
    public BomAiConfirmResult confirm(BomAiImportConfirmRequest request) {
        return confirm(request, "admin");
    }

    /**
     * 确认导入并将生成的 BOM 回写至识别追溯记录。
     */
    @Transactional(rollbackFor = Exception.class)
    public BomAiConfirmResult confirm(BomAiImportConfirmRequest request, String operator) {
        BomAiConfirmResult result = new BomAiConfirmResult();
        if (request == null) {
            result.setSuccess(false);
            result.setError("导入数据不能为空");
            return result;
        }

        List<BomAiDocument> documents = request.getDocuments();
        if (documents == null || documents.isEmpty()) {
            // 兼容旧版单 BOM 请求。
            if (request.getHeader() != null) {
                BomAiDocument document = new BomAiDocument();
                document.setPageNo(1);
                document.setHeader(request.getHeader());
                document.setItems(request.getItems());
                documents = List.of(document);
            } else {
                result.setSuccess(false);
                result.setError("至少需要一份 BOM 识别结果");
                return result;
            }
        }

        for (int i = 0; i < documents.size(); i++) {
            String validationError = validateDocument(documents.get(i), i);
            if (validationError != null) {
                result.setSuccess(false);
                result.setError(validationError);
                return result;
            }
            String materialBindingError = bindMaterialsByCode(documents.get(i), i);
            if (materialBindingError != null) {
                result.setSuccess(false);
                result.setError(materialBindingError);
                return result;
            }
        }

        BomAiImportTrace trace = null;
        if (request.getTraceId() != null) {
            trace = bomAiImportTraceService.acquireForImport(request.getTraceId(), operator);
            if (BomAiImportTraceStatus.IMPORTED.name().equals(trace.getStatus())) {
                return importedResult(trace);
            }
            BomAiImportTrace duplicate = bomAiImportTraceService.findImportedDuplicate(trace);
            if (duplicate != null && !request.isForceNewVersion()) {
                throw new ServiceException("相同原始图纸已通过批次 " + duplicate.getImportNo()
                        + " 导入，请查看已有 BOM；如确认内容有调整，请选择作为新版本导入");
            }
            if (duplicate != null && StringUtils.isBlank(request.getReimportReason())) {
                throw new ServiceException("作为新版本重复导入时必须填写原因");
            }
        }

        try {
            List<BomAiImportedBom> importedBoms = new ArrayList<>();
            for (BomAiDocument document : documents) {
                importedBoms.add(persistDocument(document));
            }
            bomAiImportTraceService.markImported(request.getTraceId(), importedBoms, operator,
                    request.getReimportReason());
            result.setSuccess(true);
            result.setBoms(importedBoms);
            if (!importedBoms.isEmpty()) {
                BomAiImportedBom first = importedBoms.get(0);
                result.setBomMasterId(first.getBomMasterId());
                result.setBomVersionId(first.getBomVersionId());
                result.setBomCode(first.getBomCode());
            }
        } catch (Exception e) {
            log.error("BOM AI 批量导入确认异常", e);
            throw new ServiceException("导入失败：" + (e.getMessage() == null ? "数据库写入失败" : e.getMessage()));
        }
        return result;
    }

    private String validateDocument(BomAiDocument document, int index) {
        String prefix = "第 " + (index + 1) + " 个 BOM";
        if (document == null || document.getHeader() == null) {
            return prefix + "缺少母件信息";
        }
        BomAiImportHeader header = document.getHeader();
        if (StringUtils.isBlank(firstNonBlank(header.getFinalParentItemCode(), header.getParentItemCode()))) {
            return prefix + "母件编码不能为空";
        }
        if (StringUtils.isBlank(firstNonBlank(header.getFinalParentItemName(), header.getParentItemName()))) {
            return prefix + "母件名称不能为空";
        }
        List<BomAiImportItem> items = document.getItems();
        if (items == null || items.isEmpty()) {
            return prefix + "子件明细不能为空";
        }
        for (int i = 0; i < items.size(); i++) {
            BomAiImportItem item = items.get(i);
            if (item == null) {
                return prefix + "第 " + (i + 1) + " 行为空";
            }
            if (StringUtils.isBlank(firstNonBlank(item.getFinalItemCode(), item.getComponentCode()))) {
                return prefix + "第 " + (i + 1) + " 行子件编码不能为空";
            }
            if (StringUtils.isBlank(firstNonBlank(item.getFinalItemName(), item.getItemName()))) {
                return prefix + "第 " + (i + 1) + " 行子件名称不能为空";
            }
            BigDecimal quantity = item.getFinalQuantity() != null ? item.getFinalQuantity() : item.getQuantity();
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                return prefix + "第 " + (i + 1) + " 行子件数量必须大于 0";
            }
        }
        return null;
    }

    /**
     * 以物料编码精确查询物料库并重新绑定，避免客户端提交的匹配结果失真。
     */
    private String bindMaterialsByCode(BomAiDocument document, int index) {
        String prefix = "第 " + (index + 1) + " 个 BOM";
        BomAiImportHeader header = document.getHeader();
        String parentCode = firstNonBlank(header.getFinalParentItemCode(), header.getParentItemCode());
        Material parentMaterial = materialService.selectMaterialByCode(parentCode);
        if (!isEnabled(parentMaterial)) {
            return prefix + "母件物料编码未匹配或已停用：" + parentCode;
        }
        applyParentMatch(header, parentMaterial);

        List<BomAiImportItem> items = document.getItems();
        for (int i = 0; i < items.size(); i++) {
            BomAiImportItem item = items.get(i);
            String componentCode = firstNonBlank(item.getFinalItemCode(), item.getComponentCode());
            Material componentMaterial = materialService.selectMaterialByCode(componentCode);
            if (!isEnabled(componentMaterial)) {
                return prefix + "第 " + (i + 1) + " 行子件物料编码未匹配或已停用：" + componentCode;
            }
            applyItemMatch(item, componentMaterial);
        }
        return null;
    }

    private BomAiImportedBom persistDocument(BomAiDocument document) {
        BomAiImportHeader header = document.getHeader();
        String parentCode = firstNonBlank(header.getFinalParentItemCode(), header.getParentItemCode());
        String parentName = firstNonBlank(header.getFinalParentItemName(), header.getParentItemName());
        String parentSpec = firstNonBlank(header.getFinalParentItemSpec(), header.getParentItemSpec());

        Long parentMaterialId = header.getFinalParentMaterialId() != null
                ? header.getFinalParentMaterialId() : header.getMatchedMaterialId();
        BomMaster master = bomMasterMapper.selectBomMasterByParentItem(parentMaterialId, BomType.MANUFACTURING.getCode());
        if (master == null) {
            String bomCode = generateBomCode(parentCode);
            master = new BomMaster();
            master.setBomCode(bomCode);
            master.setParentItemCode(parentCode);
            master.setParentItemName(parentName);
            master.setParentItemSpec(parentSpec);
            master.setParentItemId(parentMaterialId);
            master.setBomType(BomType.MANUFACTURING.getCode());
            master.setStatus(BomMasterStatus.ENABLED.getCode());
            master.setSourceSystem("AI_IMPORT");
            master.setCreateBy("admin");
            bomMasterService.insertBomMaster(master);
        }
        String bomCode = master.getBomCode();

        BigDecimal baseQty = header.getFinalBaseQty() != null ? header.getFinalBaseQty() : header.getBaseQty();
        BomVersion version = new BomVersion();
        version.setBomMasterId(master.getId());
        version.setVersionCode(nextVersionCode(master.getId()));
        version.setVersionName("AI导入版本");
        version.setBaseQty(baseQty != null ? baseQty : BigDecimal.ONE);
        version.setUsageType("GENERAL");
        version.setStatus("DRAFT");
        version.setApproveStatus("PENDING");
        // AI 导入版本先保持草稿，审核生效后再由业务操作设为默认版本。
        version.setDefaultFlag(0);
        version.setSourceSystem("AI_IMPORT");
        version.setCreateBy("admin");
        bomVersionService.insertBomVersion(version);

        List<BomAiImportItem> items = document.getItems();
        for (int i = 0; i < items.size(); i++) {
            BomAiImportItem aiItem = items.get(i);
            BomItem bomItem = new BomItem();
            bomItem.setBomVersionId(version.getId());
            bomItem.setLineNo(aiItem.getLineNo() != null ? aiItem.getLineNo() : (i + 1) * 10);
            bomItem.setParentItemCode(parentCode);

            String code = firstNonBlank(aiItem.getFinalItemCode(), aiItem.getComponentCode());
            String name = firstNonBlank(aiItem.getFinalItemName(), aiItem.getItemName());
            String spec = firstNonBlank(aiItem.getFinalSpec(), aiItem.getSpec());
            String unit = firstNonBlank(aiItem.getFinalUnit(), aiItem.getUnit());
            BigDecimal qty = aiItem.getFinalQuantity() != null ? aiItem.getFinalQuantity() : aiItem.getQuantity();

            bomItem.setComponentItemCode(code);
            bomItem.setComponentItemName(name);
            bomItem.setComponentItemSpec(spec);
            bomItem.setComponentItemUnit(unit);
            bomItem.setComponentQty(qty);
            if (aiItem.getMatchedMaterialId() != null) {
                bomItem.setComponentItemId(aiItem.getMatchedMaterialId());
            }
            bomItem.setSupplyType("PICK");
            bomItem.setIsVirtual(0);
            bomItem.setMrpExpandFlag(1);
            bomItem.setSourceSystem("AI_IMPORT");
            bomItem.setRemark(aiItem.getRemark());
            bomItem.setCreateBy("admin");
            // 不设置 componentBomVersionId，子件 BOM 通过物料编码软引用。
            bomItemService.insertBomItem(bomItem);
        }

        BomAiImportedBom imported = new BomAiImportedBom();
        imported.setBomMasterId(master.getId());
        imported.setBomVersionId(version.getId());
        imported.setBomCode(bomCode);
        imported.setVersionCode(version.getVersionCode());
        imported.setParentItemCode(parentCode);
        imported.setParentItemName(parentName);
        return imported;
    }

    private String nextVersionCode(Long bomMasterId) {
        BomVersion query = new BomVersion();
        query.setBomMasterId(bomMasterId);
        int maxMinor = -1;
        for (BomVersion version : bomVersionService.selectBomVersionList(query)) {
            String code = version.getVersionCode();
            if (code != null && code.matches("V1\\.\\d+")) {
                maxMinor = Math.max(maxMinor, Integer.parseInt(code.substring(3)));
            }
        }
        return "V1." + (maxMinor + 1);
    }

    private BomAiConfirmResult importedResult(BomAiImportTrace trace) {
        BomAiConfirmResult result = new BomAiConfirmResult();
        List<Long> masterIds = JSON.parseArray(trace.getImportedBomMasterIds(), Long.class);
        List<Long> versionIds = JSON.parseArray(trace.getImportedBomVersionIds(), Long.class);
        List<BomAiImportedBom> imported = new ArrayList<>();
        int count = Math.min(masterIds == null ? 0 : masterIds.size(), versionIds == null ? 0 : versionIds.size());
        for (int i = 0; i < count; i++) {
            BomAiImportedBom item = new BomAiImportedBom();
            item.setBomMasterId(masterIds.get(i));
            item.setBomVersionId(versionIds.get(i));
            imported.add(item);
        }
        result.setSuccess(true);
        result.setBoms(imported);
        if (!imported.isEmpty()) {
            result.setBomMasterId(imported.get(0).getBomMasterId());
            result.setBomVersionId(imported.get(0).getBomVersionId());
        }
        return result;
    }

    // ── 私有方法 ──

    @SuppressWarnings("unchecked")
    private void parseAndMatch(BomAiPreviewResult result, Map<String, Object> outputs) {
        Object payload = findDocumentPayload(outputs);
        List<BomAiDocument> documents = parseDocuments(payload);

        // 兼容旧版工作流直接返回 document + items 的格式。
        if (documents.isEmpty() && (outputs.get("document") != null || outputs.get("items") != null)) {
            Map<String, Object> legacy = new LinkedHashMap<>();
            legacy.put("pageNo", 1);
            legacy.put("document", outputs.get("document"));
            legacy.put("items", outputs.get("items"));
            documents.add(parseDocument(legacy));
        }
        if (documents.isEmpty()) {
            throw new ServiceException("AI 识别结果中没有 documents 数据");
        }
        assertNoDocumentErrors(documents);

        // 物料匹配只查询一次，逐个文档应用匹配结果。
        Material query = new Material();
        query.setStatus("0");
        List<Material> allMaterials = materialService.selectMaterialList(query);

        for (BomAiDocument document : documents) {
            BomAiImportHeader header = document.getHeader();
            if (header == null) {
                header = new BomAiImportHeader();
                document.setHeader(header);
            }
            normalizeHeader(header);
            List<BomAiImportItem> items = document.getItems();
            if (items == null) {
                items = new ArrayList<>();
                document.setItems(items);
            }
            for (BomAiImportItem item : items) {
                normalizeItem(item);
            }
            matchParent(header, allMaterials);
            for (BomAiImportItem item : items) {
                matchItem(item, allMaterials);
            }
        }

        result.setDocuments(documents);
    }

    @SuppressWarnings("unchecked")
    private Object findDocumentPayload(Map<String, Object> outputs) {
        Object direct = parseJsonValue(outputs.get("documents"));
        if (direct != null) {
            return direct;
        }
        for (String key : List.of("result", "output", "text", "answer")) {
            Object candidate = parseJsonValue(outputs.get(key));
            if (candidate instanceof Map && ((Map<String, Object>) candidate).get("documents") != null) {
                return candidate;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<BomAiDocument> parseDocuments(Object payload) {
        List<BomAiDocument> documents = new ArrayList<>();
        Object normalized = parseJsonValue(payload);
        if (normalized instanceof Map) {
            Object nested = ((Map<String, Object>) normalized).get("documents");
            if (nested != null) {
                normalized = parseJsonValue(nested);
            }
        }
        if (normalized instanceof List) {
            for (Object raw : (List<Object>) normalized) {
                if (raw instanceof Map) {
                    documents.add(parseDocument((Map<String, Object>) raw));
                }
            }
        }
        return documents;
    }

    @SuppressWarnings("unchecked")
    private BomAiDocument parseDocument(Map<String, Object> raw) {
        BomAiDocument document = new BomAiDocument();
        document.setPageNo(intObj(raw.get("pageNo")));
        document.setError(str(raw.get("error")));

        Object headerObj = raw.get("document");
        if (headerObj == null) {
            headerObj = raw.get("header");
        }
        if (headerObj == null) {
            headerObj = raw;
        }
        document.setHeader(parseHeader(headerObj));
        document.setItems(parseItems(raw.get("items")));
        return document;
    }

    /** 工作流声明某页不是 BOM 时，不能将它伪装成空 BOM 继续导入。 */
    private void assertNoDocumentErrors(List<BomAiDocument> documents) {
        for (int i = 0; i < documents.size(); i++) {
            BomAiDocument document = documents.get(i);
            if (StringUtils.isNotBlank(document.getError())) {
                int pageNo = document.getPageNo() == null ? i + 1 : document.getPageNo();
                throw new ServiceException("第 " + pageNo + " 页：" + document.getError());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private BomAiImportHeader parseHeader(Object raw) {
        BomAiImportHeader header = new BomAiImportHeader();
        Object normalized = parseJsonValue(raw);
        if (normalized instanceof Map) {
            Map<String, Object> doc = (Map<String, Object>) normalized;
            header.setParentItemCode(str(doc.get("parentItemCode")));
            header.setParentItemName(str(doc.get("parentItemName")));
            header.setParentItemSpec(str(doc.get("parentItemSpec")));
            header.setDrawingNo(str(doc.get("drawingNo")));
            header.setRevision(str(doc.get("revision")));
            header.setBaseQty(dec(doc.get("baseQty")));
        }
        return header;
    }

    @SuppressWarnings("unchecked")
    private List<BomAiImportItem> parseItems(Object raw) {
        List<BomAiImportItem> items = new ArrayList<>();
        Object normalized = parseJsonValue(raw);
        if (!(normalized instanceof List)) {
            return items;
        }
        for (Object value : (List<Object>) normalized) {
            if (!(value instanceof Map)) {
                continue;
            }
            Map<String, Object> row = (Map<String, Object>) value;
            BomAiImportItem item = new BomAiImportItem();
            item.setLineNo(intObj(row.get("lineNo")));
            item.setComponentCode(str(row.get("componentCode")));
            item.setDrawingNo(str(row.get("drawingNo")));
            item.setItemName(str(row.get("itemName")));
            item.setQuantity(dec(row.get("quantity")));
            item.setSpec(str(row.get("spec")));
            item.setUnit(str(row.get("unit")));
            item.setRemark(str(row.get("remark")));
            items.add(item);
        }
        return items;
    }

    private void normalizeHeader(BomAiImportHeader header) {
        header.setFinalParentItemCode(header.getParentItemCode());
        header.setFinalParentItemName(header.getParentItemName());
        header.setFinalParentItemSpec(header.getParentItemSpec());
        if (header.getBaseQty() == null) {
            header.setBaseQty(BigDecimal.ONE);
        }
        header.setFinalBaseQty(header.getBaseQty());
    }

    private void normalizeItem(BomAiImportItem item) {
        if (item == null) {
            return;
        }
        item.setFinalItemCode(item.getComponentCode());
        item.setFinalItemName(item.getItemName());
        item.setFinalQuantity(item.getQuantity());
        item.setFinalSpec(item.getSpec());
        item.setFinalUnit(item.getUnit());
    }

    private Object parseJsonValue(Object value) {
        if (!(value instanceof String)) {
            return value;
        }
        String text = ((String) value).trim();
        if (text.startsWith("```")) {
            int firstLineEnd = text.indexOf('\n');
            int lastFence = text.lastIndexOf("```");
            if (firstLineEnd >= 0 && lastFence > firstLineEnd) {
                text = text.substring(firstLineEnd + 1, lastFence).trim();
            }
        }
        if (!text.startsWith("{") && !text.startsWith("[")) {
            return value;
        }
        try {
            return JSON.parse(text);
        } catch (Exception ignored) {
            return value;
        }
    }

    private void matchParent(BomAiImportHeader header, List<Material> materials) {
        if (header == null || StringUtils.isBlank(header.getParentItemCode())) {
            return;
        }
        String code = header.getParentItemCode();
        if (StringUtils.isNotBlank(code)) {
            for (Material m : materials) {
                if (code.equals(m.getMaterialCode())) {
                    applyParentMatch(header, m);
                    return;
                }
            }
        }
    }

    private void applyParentMatch(BomAiImportHeader header, Material m) {
        header.setMatched(true);
        header.setMatchedMaterialId(m.getMaterialId());
        header.setMatchedMaterialCode(m.getMaterialCode());
        header.setMatchedMaterialName(m.getMaterialName());
        header.setFinalParentItemCode(m.getMaterialCode());
        header.setFinalParentItemName(m.getMaterialName());
        header.setFinalParentMaterialId(m.getMaterialId());
    }

    private void matchItem(BomAiImportItem item, List<Material> materials) {
        if (item == null || StringUtils.isBlank(item.getComponentCode())) {
            return;
        }
        String code = item.getComponentCode();
        if (StringUtils.isNotBlank(code)) {
            for (Material m : materials) {
                if (code.equals(m.getMaterialCode())) {
                    applyItemMatch(item, m);
                    return;
                }
            }
        }
    }

    private boolean isEnabled(Material material) {
        return material != null && "0".equals(material.getStatus());
    }

    private void applyItemMatch(BomAiImportItem item, Material m) {
        item.setMatched(true);
        item.setMatchedMaterialId(m.getMaterialId());
        item.setMatchedMaterialCode(m.getMaterialCode());
        item.setMatchedMaterialName(m.getMaterialName());
        item.setMatchedMaterialSpec(m.getSpec());
        item.setMatchedMaterialUnit(m.getUnit());
        item.setFinalItemCode(m.getMaterialCode());
        item.setFinalItemName(m.getMaterialName());
        item.setFinalSpec(m.getSpec());
        item.setFinalUnit(m.getUnit());
    }

    private String generateBomCode(String parentCode) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseCode = "BOM-" + parentCode + "-" + dateStr;
        if (bomMasterMapper.selectBomMasterByCode(baseCode) == null) {
            return baseCode;
        }
        for (int i = 1; i <= 999; i++) {
            String code = baseCode + "-" + String.format("%04d", i);
            if (bomMasterMapper.selectBomMasterByCode(code) == null) {
                return code;
            }
        }
        throw new RuntimeException("无法生成唯一 BOM 编码，请稍后重试");
    }

    private String difyFileType(MultipartFile file) {
        return isPdf(file) ? "document" : "image";
    }

    private boolean isPdf(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && "application/pdf".equalsIgnoreCase(contentType.trim())) {
            return true;
        }
        String filename = file.getOriginalFilename();
        return filename != null && filename.toLowerCase().endsWith(".pdf");
    }

    private String firstNonBlank(String preferred, String fallback) {
        return StringUtils.isNotBlank(preferred) ? preferred : fallback;
    }

    // ── 类型转换工具 ──

    private static String str(Object v) {
        return v == null ? null : v.toString().trim();
    }

    private static BigDecimal dec(Object v) {
        if (v == null) return null;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        if (v instanceof Number) return BigDecimal.valueOf(((Number) v).doubleValue());
        try { return new BigDecimal(v.toString().trim()); } catch (Exception e) { return null; }
    }

    private static Integer intObj(Object v) {
        if (v == null) return null;
        if (v instanceof Integer) return (Integer) v;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString().trim()); } catch (Exception e) { return null; }
    }
}
