package com.ruoyi.agent.business.automation.application;

import com.ruoyi.agent.business.automation.domain.AutomationAction;
import com.ruoyi.agent.business.automation.domain.AutomationCandidate;
import com.ruoyi.agent.business.automation.domain.AutomationCandidateOption;
import com.ruoyi.agent.business.automation.domain.AutomationPreparationStatus;
import com.ruoyi.agent.business.automation.domain.CreatePurchaseOrderDraftRequest;
import com.ruoyi.agent.business.automation.domain.CreatePurchaseOrderDraftResult;
import com.ruoyi.agent.business.automation.domain.PurchaseOrderDraft;
import com.ruoyi.agent.business.automation.domain.PurchaseOrderDraftLine;
import com.ruoyi.agent.business.automation.domain.PurchaseOrderDraftLineRequest;
import com.ruoyi.agent.business.automation.domain.PurchaseOrderDraftRequest;
import com.ruoyi.agent.business.automation.domain.PurchaseOrderPreparationResult;
import com.ruoyi.agent.business.automation.mapper.AutomationActionMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.base.domain.Material;
import com.ruoyi.mes.base.domain.Supplier;
import com.ruoyi.mes.base.service.IMaterialService;
import com.ruoyi.mes.base.service.ISupplierService;
import com.ruoyi.mes.common.enums.PurchaseDocumentStatus;
import com.ruoyi.mes.common.enums.PurchaseOrderBillType;
import com.ruoyi.mes.purchase.domain.PurchaseOrder;
import com.ruoyi.mes.purchase.domain.PurchaseOrderLine;
import com.ruoyi.mes.purchase.service.IPurchaseOrderService;
import com.ruoyi.mes.purchase.service.IPurchaseSupplierQuoteService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 采购订单 AI 自动化编排服务。
 *
 * <p>
 * 准备方法始终只读；创建方法仅接收已由用户确认的草稿，并在服务端重新解析主数据。
 * </p>
 */
@Service
public class PurchaseOrderAutomationService {
    private static final String ACTIVE_SUPPLIER_STATUS = "NORMAL";
    private static final String ACTIVE_MATERIAL_STATUS = "0";
    private static final String ACTION_TYPE = "PURCHASE_ORDER_DRAFT";
    private static final String ACTION_STATUS_COMPLETED = "COMPLETED";
    private static final String ACTION_STATUS_PROCESSING = "PROCESSING";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ISupplierService supplierService;
    private final IMaterialService materialService;
    private final IPurchaseOrderService purchaseOrderService;
    private final AutomationActionMapper actionMapper;
    private final IPurchaseSupplierQuoteService quoteService;

    public PurchaseOrderAutomationService(ISupplierService supplierService, IMaterialService materialService,
            IPurchaseOrderService purchaseOrderService,
            AutomationActionMapper actionMapper,
            IPurchaseSupplierQuoteService quoteService) {
        this.supplierService = supplierService;
        this.materialService = materialService;
        this.purchaseOrderService = purchaseOrderService;
        this.actionMapper = actionMapper;
        this.quoteService = quoteService;
    }

    /** 根据不完整自然语言提取结果准备可确认的采购订单草稿。 */
    public PurchaseOrderPreparationResult prepare(PurchaseOrderDraftRequest request) {
        PurchaseOrderDraftRequest source = request == null
                ? new PurchaseOrderDraftRequest(null, null, null, null, List.of())
                : request;
        source = applyDefaults(source);
        List<String> missing = collectMissingFields(source);
        if (!missing.isEmpty()) {
            return result(AutomationPreparationStatus.NEED_INPUT, "还需要补充采购订单信息", missing, List.of(), null);
        }
        if (!isDate(source.getOrderDate())
                || (StringUtils.isNotBlank(source.getExpectedDate()) && !isDate(source.getExpectedDate()))) {
            return result(AutomationPreparationStatus.INVALID, "订单日期或预计到货日期格式应为 yyyy-MM-dd", List.of(), List.of(),
                    null);
        }
        if (StringUtils.isNotBlank(source.getExpectedDate())
                && LocalDate.parse(source.getExpectedDate(), DATE_FORMATTER)
                        .isBefore(LocalDate.parse(source.getOrderDate(), DATE_FORMATTER))) {
            return result(AutomationPreparationStatus.INVALID, "预计到货日期不能早于订单日期", List.of(), List.of(), null);
        }

        List<AutomationCandidate> candidates = new ArrayList<>();
        Supplier supplier = resolveSupplier(source.getSupplierKeyword(), candidates);
        List<Material> materials = new ArrayList<>();
        for (PurchaseOrderDraftLineRequest line : source.getLines()) {
            Material material = resolveMaterial(line.getMaterialKeyword(), candidates);
            if (material != null)
                materials.add(material);
        }
        if (!candidates.isEmpty()) {
            return result(AutomationPreparationStatus.AMBIGUOUS, "存在多个可选的供应商或物料，请明确选择", List.of(), candidates, null);
        }
        if (supplier == null) {
            Supplier inactiveSupplier = findUniqueSupplier(source.getSupplierKeyword(), null);
            if (inactiveSupplier != null) {
                return result(AutomationPreparationStatus.INVALID, "供应商 " + inactiveSupplier.getSupplierCode()
                        + " 已停用，请选择启用供应商", List.of(), List.of(), null);
            }
            return result(AutomationPreparationStatus.INVALID, "未找到供应商：" + source.getSupplierKeyword(), List.of(),
                    List.of(), null);
        }
        if (materials.size() != source.getLines().size()) {
            for (PurchaseOrderDraftLineRequest line : source.getLines()) {
                Material inactiveMaterial = findUniqueMaterial(line.getMaterialKeyword(), null);
                if (inactiveMaterial != null && !ACTIVE_MATERIAL_STATUS.equals(inactiveMaterial.getStatus())) {
                    return result(AutomationPreparationStatus.INVALID, "物料 " + inactiveMaterial.getMaterialCode()
                            + " 已停用，请选择启用物料", List.of(), List.of(), null);
                }
            }
            return result(AutomationPreparationStatus.INVALID, "未找到启用物料，请检查物料编码或名称", List.of(), List.of(), null);
        }

        Set<Long> materialIds = new LinkedHashSet<>();
        List<PurchaseOrderDraftLine> lines = new ArrayList<>();
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal taxRate = supplier.getTaxRate() == null ? BigDecimal.ZERO : supplier.getTaxRate();
        for (int index = 0; index < source.getLines().size(); index++) {
            PurchaseOrderDraftLineRequest input = source.getLines().get(index);
            if (!isPositive(input.getQuantity())) {
                return result(AutomationPreparationStatus.NEED_INPUT, "第 " + (index + 1) + " 行缺少大于 0 的采购数量",
                        List.of("第 " + (index + 1) + " 行采购数量"), List.of(), null);
            }
            if (input.getUnitPrice() == null || input.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                return result(AutomationPreparationStatus.NEED_INPUT, "第 " + (index + 1) + " 行缺少有效的含税单价",
                        List.of("第 " + (index + 1) + " 行含税单价"), List.of(), null);
            }
            if (input.getQuoteId() != null || input.getQuoteLineId() != null) {
                if (!quoteService.validateSelection(input.getQuoteId(), input.getQuoteLineId(),
                        supplier.getSupplierCode(),
                        materials.get(index).getMaterialCode(), input.getQuantity(), input.getUnitPrice()))
                    return result(AutomationPreparationStatus.INVALID, "第 " + (index + 1)
                            + " 行引用的供应商报价已失效、数量不匹配或单价不一致，请重新比较报价",
                            List.of(), List.of(), null);
            }
            if (StringUtils.isNotBlank(input.getPlannedDate()) && !isDate(input.getPlannedDate())) {
                return result(AutomationPreparationStatus.INVALID, "第 " + (index + 1) + " 行计划到货日期格式应为 yyyy-MM-dd",
                        List.of(), List.of(), null);
            }
            Material material = materials.get(index);
            if (!materialIds.add(material.getMaterialId())) {
                return result(AutomationPreparationStatus.INVALID, "采购明细中不能重复出现同一物料：" + material.getMaterialCode(),
                        List.of(), List.of(), null);
            }
            BigDecimal quantity = scale(input.getQuantity());
            BigDecimal unitPrice = scale(input.getUnitPrice());
            BigDecimal amount = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
            lines.add(new PurchaseOrderDraftLine(index + 1, material.getMaterialId(), material.getMaterialCode(),
                    material.getMaterialName(), material.getSpec(), material.getModel(), material.getUnit(), quantity,
                    unitPrice, taxRate, amount, blankToNull(input.getPlannedDate()), input.getQuoteId(),
                    input.getQuoteLineId(),
                    StringUtils.isBlank(input.getPriceSource()) ? "MANUAL" : input.getPriceSource()));
            totalQuantity = totalQuantity.add(quantity);
            totalAmount = totalAmount.add(amount);
        }
        PurchaseOrderDraft draft = new PurchaseOrderDraft(supplier.getSupplierCode(), supplier.getSupplierName(),
                defaultCurrency(supplier.getCurrency()), source.getOrderDate(), blankToNull(source.getExpectedDate()),
                blankToNull(source.getRemark()), scale(totalQuantity), totalAmount.setScale(2, RoundingMode.HALF_UP),
                lines);
        return result(AutomationPreparationStatus.READY, "采购订单草稿已准备，请由用户确认创建", List.of(), List.of(), draft);
    }

    /** 在用户确认后的登录态下创建采购订单草稿，并以请求键保证幂等。 */
    @Transactional(rollbackFor = Exception.class)
    public CreatePurchaseOrderDraftResult createDraft(CreatePurchaseOrderDraftRequest request, Long userId,
            String username) {
        if (request == null || StringUtils.isBlank(request.getRequestId()) || request.getDraft() == null) {
            throw new ServiceException("确认创建时缺少草稿或请求标识");
        }
        String actionKey = normalizeActionKey(request.getRequestId());
        synchronized ((ACTION_TYPE + ':' + actionKey).intern()) {
            AutomationAction existed = actionMapper.selectByActionKey(actionKey);
            if (existed != null) {
                if (!userId.equals(existed.getUserId()))
                    throw new ServiceException("该自动化请求不属于当前用户");
                if (ACTION_STATUS_COMPLETED.equals(existed.getStatus())) {
                    return new CreatePurchaseOrderDraftResult(existed.getTargetId(), existed.getTargetCode(), true);
                }
                throw new ServiceException("该采购订单正在创建，请勿重复提交");
            }
            PurchaseOrderPreparationResult prepared = prepare(toPreparationRequest(request.getDraft()));
            if (prepared.getStatus() != AutomationPreparationStatus.READY) {
                throw new ServiceException("采购订单草稿尚不可创建：" + prepared.getMessage());
            }
            AutomationAction action = new AutomationAction();
            action.setActionKey(actionKey);
            action.setActionType(ACTION_TYPE);
            action.setUserId(userId);
            action.setStatus(ACTION_STATUS_PROCESSING);
            actionMapper.insert(action);

            PurchaseOrder order = toPurchaseOrder(prepared.getDraft(), actionKey, username);
            purchaseOrderService.insertPurchaseOrder(order);
            actionMapper.complete(actionKey, order.getId(), order.getOrderCode());
            return new CreatePurchaseOrderDraftResult(order.getId(), order.getOrderCode(), false);
        }
    }

    private List<String> collectMissingFields(PurchaseOrderDraftRequest request) {
        List<String> fields = new ArrayList<>();
        if (StringUtils.isBlank(request.getSupplierKeyword()))
            fields.add("供应商");
        if (StringUtils.isBlank(request.getOrderDate()))
            fields.add("订单日期");
        if (request.getLines() == null || request.getLines().isEmpty())
            fields.add("采购明细");
        if (request.getLines() != null) {
            for (int index = 0; index < request.getLines().size(); index++) {
                PurchaseOrderDraftLineRequest line = request.getLines().get(index);
                String prefix = "第 " + (index + 1) + " 行";
                if (line == null) {
                    fields.add(prefix + "采购明细");
                    continue;
                }
                if (StringUtils.isBlank(line.getMaterialKeyword()))
                    fields.add(prefix + "物料");
                if (!isPositive(line.getQuantity()))
                    fields.add(prefix + "采购数量");
                if (line.getUnitPrice() == null || line.getUnitPrice().compareTo(BigDecimal.ZERO) < 0)
                    fields.add(prefix + "含税单价");
            }
        }
        return fields;
    }

    /** 补齐可以由系统安全推导的字段，避免向用户追问非必要信息。 */
    private PurchaseOrderDraftRequest applyDefaults(PurchaseOrderDraftRequest request) {
        String orderDate = StringUtils.isBlank(request.getOrderDate())
                ? LocalDate.now().format(DATE_FORMATTER)
                : request.getOrderDate().trim();
        return new PurchaseOrderDraftRequest(request.getSupplierKeyword(), orderDate, request.getExpectedDate(),
                request.getRemark(), request.getLines());
    }

    private Supplier resolveSupplier(String keyword, List<AutomationCandidate> candidates) {
        List<Supplier> matched = selectSuppliers(keyword, ACTIVE_SUPPLIER_STATUS);
        if (matched.size() > 1) {
            candidates.add(new AutomationCandidate("supplier", keyword,
                    matched.stream().map(item -> new AutomationCandidateOption(item.getId(), item.getSupplierCode(),
                            item.getSupplierName(), null, null, null)).toList()));
            return null;
        }
        return matched.isEmpty() ? null : matched.get(0);
    }

    /** 按关键词在指定状态范围内查询唯一供应商，不产生候选项副作用。 */
    private Supplier findUniqueSupplier(String keyword, String status) {
        List<Supplier> matched = selectSuppliers(keyword, status);
        return matched.size() == 1 ? matched.get(0) : null;
    }

    /** 查询并按编码、名称优先级匹配供应商。 */
    private List<Supplier> selectSuppliers(String keyword, String status) {
        Supplier query = new Supplier();
        query.setStatus(status);
        return selectBest(keyword, supplierService.selectList(query), Supplier::getSupplierCode,
                Supplier::getSupplierName);
    }

    private Material resolveMaterial(String keyword, List<AutomationCandidate> candidates) {
        if (StringUtils.isBlank(keyword)) {
            candidates.add(new AutomationCandidate("material", "", List.of()));
            return null;
        }
        List<Material> matched = selectBest(keyword,
                materialService.selectMaterialListForAgent(keyword, null, null, ACTIVE_MATERIAL_STATUS),
                Material::getMaterialCode, Material::getMaterialName);
        if (matched.size() > 1) {
            candidates.add(new AutomationCandidate("material", keyword,
                    matched.stream()
                            .map(item -> new AutomationCandidateOption(item.getMaterialId(), item.getMaterialCode(),
                                    item.getMaterialName(), item.getSpec(), item.getModel(), item.getUnit()))
                            .toList()));
            return null;
        }
        return matched.isEmpty() ? null : matched.get(0);
    }

    /** 按关键词查询唯一物料，不产生候选项副作用。 */
    private Material findUniqueMaterial(String keyword, String status) {
        if (StringUtils.isBlank(keyword))
            return null;
        List<Material> matched = selectBest(keyword,
                materialService.selectMaterialListForAgent(keyword, null, null, status), Material::getMaterialCode,
                Material::getMaterialName);
        return matched.size() == 1 ? matched.get(0) : null;
    }

    private <T> List<T> selectBest(String keyword, List<T> source, java.util.function.Function<T, String> code,
            java.util.function.Function<T, String> name) {
        String normalized = keyword.trim().toLowerCase(Locale.ROOT);
        List<T> exact = source.stream()
                .filter(item -> normalized.equals(safe(code.apply(item)).toLowerCase(Locale.ROOT))
                        || normalized.equals(safe(name.apply(item)).toLowerCase(Locale.ROOT)))
                .toList();
        if (!exact.isEmpty())
            return exact;
        return source.stream().filter(item -> safe(code.apply(item)).toLowerCase(Locale.ROOT).contains(normalized)
                || safe(name.apply(item)).toLowerCase(Locale.ROOT).contains(normalized))
                .sorted(Comparator.comparing(item -> safe(code.apply(item)))).toList();
    }

    private PurchaseOrderDraftRequest toPreparationRequest(PurchaseOrderDraft draft) {
        List<PurchaseOrderDraftLineRequest> lines = draft.getLines() == null
                ? List.of()
                : draft.getLines().stream()
                        .map(line -> new PurchaseOrderDraftLineRequest(line.getMaterialCode(), line.getQuantity(),
                                line.getUnitPrice(), line.getPlannedDate(),
                                line.getQuoteId(), line.getQuoteLineId(), line.getPriceSource()))
                        .collect(Collectors.toList());
        return new PurchaseOrderDraftRequest(draft.getSupplierCode(), draft.getOrderDate(), draft.getExpectedDate(),
                draft.getRemark(), lines);
    }

    private PurchaseOrder toPurchaseOrder(PurchaseOrderDraft draft, String actionKey, String username) {
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderCode(generateOrderCode(actionKey));
        order.setSupplierCode(draft.getSupplierCode());
        order.setSupplierName(draft.getSupplierName());
        order.setOrderDate(draft.getOrderDate());
        order.setExpectedDate(draft.getExpectedDate());
        order.setStatus(PurchaseDocumentStatus.DRAFT.getCode());
        order.setCurrency(draft.getCurrency());
        order.setBillType(PurchaseOrderBillType.NORMAL.getCode());
        order.setTotalQuantity(draft.getTotalQuantity());
        order.setTotalAmount(draft.getTotalAmount());
        order.setRemark(draft.getRemark());
        order.setCreateBy(username);
        List<PurchaseOrderLine> lines = draft.getLines().stream()
                .map(line -> toPurchaseOrderLine(line, username)).collect(Collectors.toList());
        order.setLines(lines);
        return order;
    }

    private PurchaseOrderLine toPurchaseOrderLine(PurchaseOrderDraftLine line, String username) {
        PurchaseOrderLine target = new PurchaseOrderLine();
        target.setLineNo(line.getLineNo());
        target.setMaterialId(line.getMaterialId());
        target.setMaterialCode(line.getMaterialCode());
        target.setMaterialName(line.getMaterialName());
        target.setSpec(line.getSpec());
        target.setModel(line.getModel());
        target.setUnit(line.getUnit());
        target.setOrderQuantity(line.getQuantity());
        target.setUnitPrice(line.getUnitPrice());
        target.setTaxRate(line.getTaxRate());
        target.setAmount(line.getAmount());
        target.setPlannedDate(line.getPlannedDate());
        target.setQuoteId(line.getQuoteId());
        target.setQuoteLineId(line.getQuoteLineId());
        target.setPriceSource(line.getPriceSource());
        target.setCreateBy(username);
        return target;
    }

    private String generateOrderCode(String actionKey) {
        String suffix = actionKey.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        if (suffix.length() > 24)
            suffix = suffix.substring(suffix.length() - 24);
        if (suffix.isBlank())
            suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase(Locale.ROOT);
        return "AI-PO-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + '-' + suffix;
    }

    private String normalizeActionKey(String requestId) {
        String key = requestId.trim();
        if (key.length() > 64)
            throw new ServiceException("请求标识长度不能超过 64 位");
        return key;
    }

    private boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean isDate(String value) {
        try {
            LocalDate.parse(value, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException ignored) {
            return false;
        }
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(6, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    private String defaultCurrency(String currency) {
        return StringUtils.isBlank(currency) ? "CNY" : currency.trim().toUpperCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private PurchaseOrderPreparationResult result(AutomationPreparationStatus status, String message,
            List<String> missingFields, List<AutomationCandidate> candidates,
            PurchaseOrderDraft draft) {
        return new PurchaseOrderPreparationResult(status, message, missingFields, candidates, draft);
    }
}
