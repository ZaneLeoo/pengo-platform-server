package com.ruoyi.agent.business.automation.purchaseflow.application;

import com.ruoyi.agent.business.automation.domain.AutomationAction;
import com.ruoyi.agent.business.automation.mapper.AutomationActionMapper;
import com.ruoyi.agent.business.automation.purchaseflow.domain.AutomationDocumentResult;
import com.ruoyi.agent.business.automation.purchaseflow.domain.ConfirmedActionRequest;
import com.ruoyi.agent.business.automation.purchaseflow.domain.InboundDraft;
import com.ruoyi.agent.business.automation.purchaseflow.domain.InboundDraftLineRequest;
import com.ruoyi.agent.business.automation.purchaseflow.domain.InboundDraftRequest;
import com.ruoyi.agent.business.automation.purchaseflow.domain.InspectionDraft;
import com.ruoyi.agent.business.automation.purchaseflow.domain.InspectionDraftRequest;
import com.ruoyi.agent.business.automation.purchaseflow.domain.ReceiptDraft;
import com.ruoyi.agent.business.automation.purchaseflow.domain.ReceiptDraftLineRequest;
import com.ruoyi.agent.business.automation.purchaseflow.domain.ReceiptDraftRequest;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.common.enums.PurchaseDocumentStatus;
import com.ruoyi.mes.common.enums.PurchaseInboundBillType;
import com.ruoyi.mes.common.enums.PurchaseReceiptBillType;
import com.ruoyi.mes.common.enums.ReceiptInspectionStatus;
import com.ruoyi.mes.purchase.domain.PurchaseInbound;
import com.ruoyi.mes.purchase.domain.PurchaseInboundLine;
import com.ruoyi.mes.purchase.domain.PurchaseReceipt;
import com.ruoyi.mes.purchase.domain.PurchaseReceiptLine;
import com.ruoyi.mes.purchase.domain.dto.InboundReferenceLine;
import com.ruoyi.mes.purchase.domain.dto.InspectionRequest;
import com.ruoyi.mes.purchase.domain.dto.ReceiptReferenceLine;
import com.ruoyi.mes.purchase.mapper.PurchaseReceiptMapper;
import com.ruoyi.mes.purchase.service.IPurchaseFlowService;
import com.ruoyi.mes.purchase.service.PurchaseDocumentDraftService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 采购到货、质检、入库的 AI 自动化编排服务。 */
@Service
public class PurchaseFlowAutomationService {
    private static final String COMPLETED = "COMPLETED";
    private static final String PROCESSING = "PROCESSING";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final IPurchaseFlowService flowService;
    private final PurchaseDocumentDraftService draftService;
    private final PurchaseReceiptMapper receiptMapper;
    private final AutomationActionMapper actionMapper;

    public PurchaseFlowAutomationService(IPurchaseFlowService flowService, PurchaseDocumentDraftService draftService,
            PurchaseReceiptMapper receiptMapper, AutomationActionMapper actionMapper) {
        this.flowService = flowService;
        this.draftService = draftService;
        this.receiptMapper = receiptMapper;
        this.actionMapper = actionMapper;
    }

    /** 查询仍可到货的已审核采购订单明细。 */
    public List<ReceiptReferenceLine> receiptCandidates(String orderCode, String supplierName, String materialCode) {
        return flowService.selectReceiptReferenceLines(orderCode, supplierName, materialCode);
    }

    /** 根据来源订单行准备到货草稿，不写入业务单据。 */
    public ReceiptDraft prepareReceipt(ReceiptDraftRequest request) {
        if (request == null || request.getLines() == null || request.getLines().isEmpty())
            throw new ServiceException("缺少到货明细 lines");
        String receiptDate = defaultDate(request.getReceiptDate(), "receiptDate");
        Map<Long, ReceiptReferenceLine> candidates = new HashMap<>();
        receiptCandidates(null, null, null).forEach(item -> candidates.put(item.getSourceOrderLineId(), item));
        List<PurchaseReceiptLine> lines = new ArrayList<>();
        String supplierCode = null;
        String supplierName = null;
        BigDecimal total = BigDecimal.ZERO;
        for (int index = 0; index < request.getLines().size(); index++) {
            ReceiptDraftLineRequest input = request.getLines().get(index);
            if (input == null || input.getSourceOrderLineId() == null)
                throw new ServiceException("第 " + (index + 1) + " 行缺少 sourceOrderLineId");
            ReceiptReferenceLine source = candidates.get(input.getSourceOrderLineId());
            if (source == null)
                throw new ServiceException("采购订单行 " + input.getSourceOrderLineId() + " 不存在或已无可到货数量");
            requirePositive(input.getReceivedQuantity(), "第 " + (index + 1) + " 行 receivedQuantity");
            if (input.getReceivedQuantity().compareTo(source.getRemainingQuantity()) > 0)
                throw new ServiceException("物料 " + source.getMaterialCode() + " 到货数量不能超过剩余可到货数量 "
                        + source.getRemainingQuantity());
            if (supplierCode != null && !supplierCode.equals(source.getSupplierCode()))
                throw new ServiceException("一张到货单只能包含同一供应商的采购订单明细");
            supplierCode = source.getSupplierCode();
            supplierName = source.getSupplierName();
            lines.add(toReceiptLine(index + 1, input, source, request));
            total = total.add(input.getReceivedQuantity());
        }
        ReceiptDraft draft = new ReceiptDraft();
        draft.setReceiptDate(receiptDate);
        draft.setSupplierCode(supplierCode);
        draft.setSupplierName(supplierName);
        draft.setRemark(request.getRemark());
        draft.setTotalQuantity(total);
        draft.setLines(lines);
        return draft;
    }

    /** 根据已审核到货单准备质检结果，不写入业务单据。 */
    public InspectionDraft prepareInspection(InspectionDraftRequest request) {
        if (request == null || (request.getReceiptId() == null && StringUtils.isBlank(request.getReceiptCode())))
            throw new ServiceException("缺少 receiptId 或 receiptCode");
        PurchaseReceipt receipt = findReceipt(request.getReceiptId(), request.getReceiptCode());
        if (!PurchaseDocumentStatus.APPROVED.getCode().equals(receipt.getStatus()))
            throw new ServiceException("到货单必须先审核后才能质检");
        if (!ReceiptInspectionStatus.PENDING.getCode().equals(receipt.getInspectionStatus()))
            throw new ServiceException("到货单当前检验状态不允许再次质检");
        if (request.getLines() == null || request.getLines().isEmpty())
            throw new ServiceException("缺少质检明细 lines");
        InspectionDraft draft = new InspectionDraft();
        draft.setReceiptId(receipt.getId());
        draft.setReceiptCode(receipt.getReceiptCode());
        draft.setSupplierName(receipt.getSupplierName());
        draft.setLines(request.getLines());
        validateInspectionDraft(draft);
        return draft;
    }

    /** 查询已质检且仍可入库的到货明细。 */
    public List<InboundReferenceLine> inboundCandidates(String receiptCode, String warehouseCode,
            String materialCode) {
        return flowService.selectInboundReferenceLines(receiptCode, warehouseCode, materialCode);
    }

    /** 根据来源到货行准备入库草稿，不写入业务单据。 */
    public InboundDraft prepareInbound(InboundDraftRequest request) {
        if (request == null || request.getLines() == null || request.getLines().isEmpty())
            throw new ServiceException("缺少入库明细 lines");
        String inboundDate = defaultDate(request.getInboundDate(), "inboundDate");
        Map<Long, InboundReferenceLine> candidates = new HashMap<>();
        inboundCandidates(null, null, null).forEach(item -> candidates.put(item.getSourceReceiptLineId(), item));
        List<PurchaseInboundLine> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (int index = 0; index < request.getLines().size(); index++) {
            InboundDraftLineRequest input = request.getLines().get(index);
            if (input == null || input.getSourceReceiptLineId() == null)
                throw new ServiceException("第 " + (index + 1) + " 行缺少 sourceReceiptLineId");
            InboundReferenceLine source = candidates.get(input.getSourceReceiptLineId());
            if (source == null)
                throw new ServiceException("到货行 " + input.getSourceReceiptLineId() + " 不存在或已无可入库数量");
            requirePositive(input.getInboundQuantity(), "第 " + (index + 1) + " 行 inboundQuantity");
            if (input.getInboundQuantity().compareTo(source.getRemainingQuantity()) > 0)
                throw new ServiceException("物料 " + source.getMaterialCode() + " 入库数量不能超过剩余可入库数量 "
                        + source.getRemainingQuantity());
            PurchaseInboundLine line = toInboundLine(index + 1, input, source);
            if (StringUtils.isBlank(line.getWarehouseCode()))
                throw new ServiceException("第 " + (index + 1)
                        + " 行缺少入库仓库 warehouseCode，请先调用 queryWarehouses 获取真实仓库编码");
            lines.add(line);
            total = total.add(input.getInboundQuantity());
        }
        InboundDraft draft = new InboundDraft();
        draft.setInboundDate(inboundDate);
        draft.setRemark(request.getRemark());
        draft.setTotalQuantity(total);
        draft.setLines(lines);
        return draft;
    }

    /** 用户确认后创建到货草稿。 */
    @Transactional(rollbackFor = Exception.class)
    public AutomationDocumentResult createReceipt(ConfirmedActionRequest<ReceiptDraft> request, Long userId,
            String username) {
        requireConfirmedRequest(request);
        String key = actionKey("RC", request.getRequestId());
        return executeOnce(key, "PURCHASE_RECEIPT_CREATE", userId, () -> {
            ReceiptDraft prepared = prepareReceipt(toReceiptRequest(request.getDraft()));
            PurchaseReceipt receipt = toReceipt(prepared, key);
            draftService.createReceiptDraft(receipt, username);
            return new AutomationDocumentResult(receipt.getId(), receipt.getReceiptCode(),
                    PurchaseDocumentStatus.DRAFT.getCode(), false);
        });
    }

    /** 用户确认后提交到货质检。 */
    @Transactional(rollbackFor = Exception.class)
    public AutomationDocumentResult inspectReceipt(ConfirmedActionRequest<InspectionDraft> request, Long userId,
            String username) {
        requireConfirmedRequest(request);
        validateInspectionDraft(request.getDraft());
        String key = actionKey("QI", request.getRequestId());
        return executeOnce(key, "PURCHASE_RECEIPT_INSPECT", userId, () -> {
            InspectionRequest inspection = new InspectionRequest();
            inspection.setLines(request.getDraft().getLines());
            flowService.inspectReceipt(request.getDraft().getReceiptId(), inspection, username);
            PurchaseReceipt receipt = requiredReceipt(request.getDraft().getReceiptId());
            return new AutomationDocumentResult(receipt.getId(), receipt.getReceiptCode(),
                    receipt.getInspectionStatus(), false);
        });
    }

    /** 用户确认后创建入库草稿。 */
    @Transactional(rollbackFor = Exception.class)
    public AutomationDocumentResult createInbound(ConfirmedActionRequest<InboundDraft> request, Long userId,
            String username) {
        requireConfirmedRequest(request);
        String key = actionKey("IC", request.getRequestId());
        return executeOnce(key, "PURCHASE_INBOUND_CREATE", userId, () -> {
            InboundDraft prepared = prepareInbound(toInboundRequest(request.getDraft()));
            PurchaseInbound inbound = toInbound(prepared, key);
            draftService.createInboundDraft(inbound, username);
            return new AutomationDocumentResult(inbound.getId(), inbound.getInboundCode(),
                    PurchaseDocumentStatus.DRAFT.getCode(), false);
        });
    }

    private PurchaseReceiptLine toReceiptLine(int lineNo, ReceiptDraftLineRequest input, ReceiptReferenceLine source,
            ReceiptDraftRequest request) {
        PurchaseReceiptLine line = new PurchaseReceiptLine();
        line.setLineNo(lineNo);
        line.setSourceOrderId(source.getSourceOrderId());
        line.setSourceOrderCode(source.getSourceOrderCode());
        line.setSourceOrderLineId(source.getSourceOrderLineId());
        line.setSourceOrderLineNo(source.getSourceOrderLineNo());
        line.setMaterialId(source.getMaterialId());
        line.setMaterialCode(source.getMaterialCode());
        line.setMaterialName(source.getMaterialName());
        line.setSpec(source.getSpec());
        line.setUnit(source.getUnit());
        line.setReceivedQuantity(input.getReceivedQuantity());
        line.setQualifiedQuantity(BigDecimal.ZERO);
        line.setRejectedQuantity(BigDecimal.ZERO);
        line.setPendingQuantity(input.getReceivedQuantity());
        line.setInboundQuantity(BigDecimal.ZERO);
        line.setLotNo(input.getLotNo());
        line.setProductionDate(input.getProductionDate());
        line.setExpiryDate(input.getExpiryDate());
        line.setWarehouseCode(request.getWarehouseCode());
        line.setWarehouseName(request.getWarehouseName());
        line.setLocationCode(input.getLocationCode());
        line.setLocationName(input.getLocationName());
        return line;
    }

    private PurchaseInboundLine toInboundLine(int lineNo, InboundDraftLineRequest input, InboundReferenceLine source) {
        PurchaseInboundLine line = new PurchaseInboundLine();
        line.setLineNo(lineNo);
        line.setSourceReceiptId(source.getSourceReceiptId());
        line.setSourceReceiptCode(source.getSourceReceiptCode());
        line.setSourceReceiptLineId(source.getSourceReceiptLineId());
        line.setSourceReceiptLineNo(source.getSourceReceiptLineNo());
        line.setSourceOrderId(source.getSourceOrderId());
        line.setSourceOrderCode(source.getSourceOrderCode());
        line.setSourceOrderLineId(source.getSourceOrderLineId());
        line.setSourceOrderLineNo(source.getSourceOrderLineNo());
        line.setMaterialId(source.getMaterialId());
        line.setMaterialCode(source.getMaterialCode());
        line.setMaterialName(source.getMaterialName());
        line.setSpec(source.getSpec());
        line.setUnit(source.getUnit());
        line.setInboundQuantity(input.getInboundQuantity());
        line.setLotNo(source.getLotNo());
        line.setProductionDate(source.getProductionDate());
        line.setExpiryDate(source.getExpiryDate());
        line.setWarehouseCode(firstNotBlank(input.getWarehouseCode(), source.getWarehouseCode()));
        line.setWarehouseName(firstNotBlank(input.getWarehouseName(), source.getWarehouseName()));
        line.setLocationCode(firstNotBlank(input.getLocationCode(), source.getLocationCode()));
        line.setLocationName(firstNotBlank(input.getLocationName(), source.getLocationName()));
        return line;
    }

    private PurchaseReceipt toReceipt(ReceiptDraft draft, String key) {
        PurchaseReceipt receipt = new PurchaseReceipt();
        receipt.setReceiptCode(generateCode("AI-RC", key));
        receipt.setSupplierCode(draft.getSupplierCode());
        receipt.setSupplierName(draft.getSupplierName());
        receipt.setReceiptDate(draft.getReceiptDate());
        receipt.setInspectionStatus(ReceiptInspectionStatus.PENDING.getCode());
        receipt.setBillType(PurchaseReceiptBillType.PURCHASE_ORDER.getCode());
        receipt.setTotalQuantity(draft.getTotalQuantity());
        receipt.setRemark(draft.getRemark());
        receipt.setLines(draft.getLines());
        return receipt;
    }

    private PurchaseInbound toInbound(InboundDraft draft, String key) {
        PurchaseInbound inbound = new PurchaseInbound();
        inbound.setInboundCode(generateCode("AI-IN", key));
        inbound.setInboundDate(draft.getInboundDate());
        inbound.setBillType(PurchaseInboundBillType.RECEIPT.getCode());
        inbound.setTotalQuantity(draft.getTotalQuantity());
        inbound.setRemark(draft.getRemark());
        inbound.setLines(draft.getLines());
        return inbound;
    }

    private ReceiptDraftRequest toReceiptRequest(ReceiptDraft draft) {
        ReceiptDraftRequest request = new ReceiptDraftRequest();
        request.setReceiptDate(draft.getReceiptDate());
        request.setRemark(draft.getRemark());
        List<ReceiptDraftLineRequest> lines = draft.getLines().stream().map(line -> {
            ReceiptDraftLineRequest item = new ReceiptDraftLineRequest();
            item.setSourceOrderLineId(line.getSourceOrderLineId());
            item.setReceivedQuantity(line.getReceivedQuantity());
            item.setLotNo(line.getLotNo());
            item.setProductionDate(line.getProductionDate());
            item.setExpiryDate(line.getExpiryDate());
            item.setLocationCode(line.getLocationCode());
            item.setLocationName(line.getLocationName());
            return item;
        }).toList();
        request.setLines(lines);
        PurchaseReceiptLine first = draft.getLines().get(0);
        request.setWarehouseCode(first.getWarehouseCode());
        request.setWarehouseName(first.getWarehouseName());
        return request;
    }

    private InboundDraftRequest toInboundRequest(InboundDraft draft) {
        InboundDraftRequest request = new InboundDraftRequest();
        request.setInboundDate(draft.getInboundDate());
        request.setRemark(draft.getRemark());
        request.setLines(draft.getLines().stream().map(line -> {
            InboundDraftLineRequest item = new InboundDraftLineRequest();
            item.setSourceReceiptLineId(line.getSourceReceiptLineId());
            item.setInboundQuantity(line.getInboundQuantity());
            item.setWarehouseCode(line.getWarehouseCode());
            item.setWarehouseName(line.getWarehouseName());
            item.setLocationCode(line.getLocationCode());
            item.setLocationName(line.getLocationName());
            return item;
        }).toList());
        return request;
    }

    private PurchaseReceipt findReceipt(Long id, String code) {
        if (id != null)
            return requiredReceipt(id);
        PurchaseReceipt query = new PurchaseReceipt();
        query.setReceiptCode(code);
        List<PurchaseReceipt> matches = receiptMapper.selectList(query).stream()
                .filter(item -> code.equals(item.getReceiptCode())).toList();
        if (matches.size() != 1)
            throw new ServiceException(matches.isEmpty() ? "未找到到货单：" + code : "到货单编号不唯一：" + code);
        return matches.get(0);
    }

    private PurchaseReceipt requiredReceipt(Long id) {
        PurchaseReceipt receipt = receiptMapper.selectById(id);
        if (receipt == null)
            throw new ServiceException("到货单不存在");
        receipt.setLines(receiptMapper.selectLines(id));
        return receipt;
    }

    private void validateInspectionDraft(InspectionDraft draft) {
        if (draft == null || draft.getReceiptId() == null || draft.getLines() == null || draft.getLines().isEmpty())
            throw new ServiceException("确认质检时缺少到货单或质检明细");
        Map<Long, PurchaseReceiptLine> source = new HashMap<>();
        requiredReceipt(draft.getReceiptId()).getLines().forEach(line -> source.put(line.getId(), line));
        if (draft.getLines().size() != source.size())
            throw new ServiceException("质检必须提交到货单的全部明细行");
        draft.getLines().forEach(line -> {
            PurchaseReceiptLine receiptLine = source.get(line.getReceiptLineId());
            if (receiptLine == null)
                throw new ServiceException("质检明细不属于当前到货单");
            if (line.getQualifiedQuantity() == null || line.getRejectedQuantity() == null
                    || line.getQualifiedQuantity().compareTo(BigDecimal.ZERO) < 0
                    || line.getRejectedQuantity().compareTo(BigDecimal.ZERO) < 0)
                throw new ServiceException("质检数量不能为空或小于 0");
            if (line.getQualifiedQuantity().add(line.getRejectedQuantity())
                    .compareTo(receiptLine.getReceivedQuantity()) != 0)
                throw new ServiceException("物料 " + receiptLine.getMaterialCode() + " 的合格与不合格数量之和必须等于到货数量");
        });
    }

    private <T> void requireConfirmedRequest(ConfirmedActionRequest<T> request) {
        if (request == null || StringUtils.isBlank(request.getRequestId()) || request.getDraft() == null)
            throw new ServiceException("确认执行时缺少 requestId 或 draft");
    }

    private String defaultDate(String value, String field) {
        String date = StringUtils.isBlank(value) ? LocalDate.now().toString() : value.trim();
        try {
            LocalDate.parse(date, DATE_FORMATTER);
            return date;
        } catch (DateTimeParseException exception) {
            throw new ServiceException(field + " 格式必须为 yyyy-MM-dd");
        }
    }

    private void requirePositive(BigDecimal value, String field) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0)
            throw new ServiceException(field + " 必须是大于 0 的数值");
    }

    private String firstNotBlank(String preferred, String fallback) {
        return StringUtils.isNotBlank(preferred) ? preferred : fallback;
    }

    private String actionKey(String prefix, String requestId) {
        String value = requestId == null ? "" : requestId.trim();
        if (value.isBlank() || value.length() > 48)
            throw new ServiceException("requestId 不能为空且长度不能超过 48 位");
        return prefix + ':' + value;
    }

    private String generateCode(String prefix, String key) {
        String suffix = key.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        if (suffix.length() > 20)
            suffix = suffix.substring(suffix.length() - 20);
        if (suffix.isBlank())
            suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase(Locale.ROOT);
        return prefix + '-' + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + '-' + suffix;
    }

    private AutomationDocumentResult executeOnce(String key, String type, Long userId, Action action) {
        synchronized ((type + ':' + key).intern()) {
            AutomationAction existed = actionMapper.selectByActionKey(key);
            if (existed != null) {
                if (!userId.equals(existed.getUserId()))
                    throw new ServiceException("该自动化请求不属于当前用户");
                if (COMPLETED.equals(existed.getStatus()))
                    return new AutomationDocumentResult(existed.getTargetId(), existed.getTargetCode(), COMPLETED, true);
                throw new ServiceException("该自动化动作正在执行，请勿重复提交");
            }
            AutomationAction record = new AutomationAction();
            record.setActionKey(key);
            record.setActionType(type);
            record.setUserId(userId);
            record.setStatus(PROCESSING);
            actionMapper.insert(record);
            AutomationDocumentResult result = action.execute();
            actionMapper.complete(key, result.getDocumentId(), result.getDocumentCode());
            return result;
        }
    }

    @FunctionalInterface
    private interface Action {
        AutomationDocumentResult execute();
    }
}
