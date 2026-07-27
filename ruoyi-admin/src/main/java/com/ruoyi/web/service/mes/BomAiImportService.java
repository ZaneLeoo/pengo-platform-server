package com.ruoyi.web.service.mes;

import com.ruoyi.agent.application.DifyAppConfigService;
import com.ruoyi.agent.infrastructure.dify.DifyClientSettings;
import com.ruoyi.agent.infrastructure.dify.DifyWorkflowClient;
import com.ruoyi.agent.infrastructure.dify.model.DifyFileUploadRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyFileUploadResult;
import com.ruoyi.agent.infrastructure.dify.model.DifyWorkflowRunRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyWorkflowRunResult;
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
import com.ruoyi.web.domain.dto.BomAiConfirmResult;
import com.ruoyi.web.domain.dto.BomAiImportConfirmRequest;
import com.ruoyi.web.domain.dto.BomAiImportHeader;
import com.ruoyi.web.domain.dto.BomAiImportItem;
import com.ruoyi.web.domain.dto.BomAiPreviewResult;
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

    /**
     * 上传图纸 → Dify 识别 → 物料匹配 → 返回预览数据。
     */
    public BomAiPreviewResult recognize(MultipartFile file) {
        BomAiPreviewResult result = new BomAiPreviewResult();
        try {
            // 1. 获取 Dify 配置
            DifyClientSettings settings = difyAppConfigService.requireSettings(DIFY_APP_CODE);

            // 2. 上传图纸到 Dify
            log.info("Uploading drawing {} to Dify", file.getOriginalFilename());
            DifyFileUploadResult upload = difyWorkflowClient.uploadFile(settings,
                    new DifyFileUploadRequest(
                            file.getOriginalFilename(),
                            file.getContentType(),
                            file.getBytes(),
                            DIFY_APP_CODE));

            // 3. 执行 BOM_OCR 工作流
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("file_id", upload.getId());
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
            result.setSuccess(true);

        } catch (Exception e) {
            log.error("BOM AI 识别异常", e);
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
     * 确认导入 → 事务写入 bom_master + bom_version + bom_item。
     */
    @Transactional(rollbackFor = Exception.class)
    public BomAiConfirmResult confirm(BomAiImportConfirmRequest request) {
        BomAiConfirmResult result = new BomAiConfirmResult();
        try {
            BomAiImportHeader header = request.getHeader();
            List<BomAiImportItem> items = request.getItems();

            if (header == null || header.getFinalParentItemCode() == null) {
                result.setSuccess(false);
                result.setError("母件编码不能为空");
                return result;
            }
            if (items == null || items.isEmpty()) {
                result.setSuccess(false);
                result.setError("子件明细不能为空");
                return result;
            }

            // -- 生成唯一 BOM 编码 --
            String bomCode = generateBomCode(header.getFinalParentItemCode());

            // -- 创建 bom_master --
            BomMaster master = new BomMaster();
            master.setBomCode(bomCode);
            master.setParentItemCode(header.getFinalParentItemCode());
            master.setParentItemName(header.getFinalParentItemName());
            master.setParentItemSpec(header.getFinalParentItemSpec());
            if (header.getFinalParentMaterialId() != null) {
                master.setParentItemId(header.getFinalParentMaterialId());
            }
            master.setBomType("SELF");
            master.setStatus("DRAFT");
            master.setSourceSystem("AI_IMPORT");
            master.setCreateBy("admin");
            bomMasterService.insertBomMaster(master);

            // -- 创建 bom_version --
            BigDecimal baseQty = header.getFinalBaseQty() != null ? header.getFinalBaseQty() : BigDecimal.ONE;
            BomVersion version = new BomVersion();
            version.setBomMasterId(master.getId());
            version.setVersionCode("V1.0");
            version.setVersionName("AI导入版本");
            version.setBaseQty(baseQty);
            version.setUsageType("GENERAL");
            version.setStatus("DRAFT");
            version.setApproveStatus("PENDING");
            version.setDefaultFlag(1);
            version.setSourceSystem("AI_IMPORT");
            version.setCreateBy("admin");
            bomVersionService.insertBomVersion(version);

            // -- 创建 bom_item 明细 --
            for (int i = 0; i < items.size(); i++) {
                BomAiImportItem aiItem = items.get(i);
                BomItem bomItem = new BomItem();
                bomItem.setBomVersionId(version.getId());
                bomItem.setLineNo(aiItem.getLineNo() != null ? aiItem.getLineNo() : (i + 1) * 10);

                String code = aiItem.getFinalItemCode() != null ? aiItem.getFinalItemCode() : aiItem.getComponentCode();
                String name = aiItem.getFinalItemName() != null ? aiItem.getFinalItemName() : aiItem.getItemName();
                String spec = aiItem.getFinalSpec() != null ? aiItem.getFinalSpec() : aiItem.getSpec();
                String unit = aiItem.getFinalUnit() != null ? aiItem.getFinalUnit() : aiItem.getUnit();
                BigDecimal qty = aiItem.getFinalQuantity() != null ? aiItem.getFinalQuantity() : aiItem.getQuantity();

                bomItem.setComponentItemCode(code);
                bomItem.setComponentItemName(name);
                bomItem.setComponentItemSpec(spec);
                bomItem.setComponentItemUnit(unit);
                bomItem.setComponentQty(qty != null && qty.compareTo(BigDecimal.ZERO) > 0 ? qty : BigDecimal.ONE);

                if (aiItem.getMatchedMaterialId() != null) {
                    bomItem.setComponentItemId(aiItem.getMatchedMaterialId());
                }
                bomItem.setSupplyType("PICK");
                bomItem.setIsVirtual(0);
                bomItem.setMrpExpandFlag(1);
                bomItem.setSourceSystem("AI_IMPORT");
                bomItem.setRemark(aiItem.getRemark());
                bomItem.setCreateBy("admin");
                bomItemService.insertBomItem(bomItem);
            }

            result.setSuccess(true);
            result.setBomMasterId(master.getId());
            result.setBomVersionId(version.getId());
            result.setBomCode(bomCode);

        } catch (Exception e) {
            log.error("BOM AI 导入确认异常", e);
            result.setSuccess(false);
            result.setError("导入失败：" + e.getMessage());
        }
        return result;
    }

    // ── 私有方法 ──

    @SuppressWarnings("unchecked")
    private void parseAndMatch(BomAiPreviewResult result, Map<String, Object> outputs) {
        // 解析母件头
        BomAiImportHeader header = new BomAiImportHeader();
        Object docObj = outputs.get("document");
        if (docObj instanceof Map) {
            Map<String, Object> doc = (Map<String, Object>) docObj;
            header.setParentItemCode(str(doc.get("parentItemCode")));
            header.setParentItemName(str(doc.get("parentItemName")));
            header.setParentItemSpec(str(doc.get("parentItemSpec")));
            header.setDrawingNo(str(doc.get("drawingNo")));
            header.setRevision(str(doc.get("revision")));
            header.setBaseQty(dec(doc.get("baseQty")));
        }
        // 默认值
        header.setFinalParentItemCode(header.getParentItemCode());
        header.setFinalParentItemName(header.getParentItemName());
        header.setFinalParentItemSpec(header.getParentItemSpec());
        if (header.getBaseQty() == null) {
            header.setBaseQty(BigDecimal.ONE);
        }
        header.setFinalBaseQty(header.getBaseQty());
        result.setHeader(header);

        // 解析子件明细
        List<BomAiImportItem> items = new ArrayList<>();
        Object itemsObj = outputs.get("items");
        if (itemsObj instanceof List) {
            for (Object raw : (List<Object>) itemsObj) {
                if (!(raw instanceof Map)) continue;
                Map<String, Object> row = (Map<String, Object>) raw;
                BomAiImportItem item = new BomAiImportItem();
                item.setLineNo(intObj(row.get("lineNo")));
                item.setComponentCode(str(row.get("componentCode")));
                item.setDrawingNo(str(row.get("drawingNo")));
                item.setItemName(str(row.get("itemName")));
                item.setQuantity(dec(row.get("quantity")));
                item.setSpec(str(row.get("spec")));
                item.setUnit(str(row.get("unit")));
                item.setRemark(str(row.get("remark")));

                item.setFinalItemCode(item.getComponentCode());
                item.setFinalItemName(item.getItemName());
                item.setFinalQuantity(item.getQuantity());
                item.setFinalSpec(item.getSpec());
                item.setFinalUnit(item.getUnit());
                items.add(item);
            }
        }
        result.setItems(items);

        // 物料三阶段匹配
        Material query = new Material();
        query.setStatus("0");
        List<Material> allMaterials = materialService.selectMaterialList(query);

        matchParent(header, allMaterials);
        for (BomAiImportItem item : items) {
            matchItem(item, allMaterials);
        }
    }

    private void matchParent(BomAiImportHeader header, List<Material> materials) {
        if (header == null) return;
        String code = header.getParentItemCode();
        String drawingNo = header.getDrawingNo();
        String name = header.getParentItemName();

        // 精确编码匹配
        if (StringUtils.isNotBlank(code)) {
            for (Material m : materials) {
                if (code.equals(m.getMaterialCode())) {
                    applyParentMatch(header, m); return;
                }
            }
        }
        // 图号模糊匹配
        if (StringUtils.isNotBlank(drawingNo)) {
            for (Material m : materials) {
                if (StringUtils.isNotBlank(m.getDrawingNo()) && m.getDrawingNo().contains(drawingNo)) {
                    applyParentMatch(header, m); return;
                }
            }
        }
        // 名称模糊匹配
        if (StringUtils.isNotBlank(name)) {
            for (Material m : materials) {
                if (StringUtils.isNotBlank(m.getMaterialName()) && m.getMaterialName().contains(name)) {
                    applyParentMatch(header, m); return;
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
        String code = item.getComponentCode();
        String drawingNo = item.getDrawingNo();
        String name = item.getItemName();
        String spec = item.getSpec();

        // Stage 1: 编码精确匹配
        if (StringUtils.isNotBlank(code)) {
            for (Material m : materials) {
                if (code.equals(m.getMaterialCode())) {
                    applyItemMatch(item, m); return;
                }
            }
        }
        // Stage 2: 图号模糊匹配
        if (StringUtils.isNotBlank(drawingNo)) {
            for (Material m : materials) {
                if (StringUtils.isNotBlank(m.getDrawingNo()) && m.getDrawingNo().contains(drawingNo)) {
                    applyItemMatch(item, m); return;
                }
            }
        }
        // Stage 3: 名称+规格模糊匹配
        if (StringUtils.isNotBlank(name)) {
            for (Material m : materials) {
                if (StringUtils.isNotBlank(m.getMaterialName()) && m.getMaterialName().contains(name)) {
                    if (StringUtils.isNotBlank(spec) && StringUtils.isNotBlank(m.getSpec())) {
                        if (m.getSpec().contains(spec) || spec.contains(m.getSpec())) {
                            applyItemMatch(item, m); return;
                        }
                    } else {
                        applyItemMatch(item, m); return;
                    }
                }
            }
        }
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
