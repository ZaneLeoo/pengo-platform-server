package com.ruoyi.mes.purchase.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.base.domain.Material;
import com.ruoyi.mes.base.domain.Supplier;
import com.ruoyi.mes.base.service.IMaterialService;
import com.ruoyi.mes.base.service.ISupplierService;
import com.ruoyi.mes.common.enums.PurchaseQuoteStatus;
import com.ruoyi.mes.purchase.domain.PurchaseSupplierQuote;
import com.ruoyi.mes.purchase.domain.PurchaseSupplierQuoteLine;
import com.ruoyi.mes.purchase.domain.dto.PurchaseQuoteCandidate;
import com.ruoyi.mes.purchase.domain.dto.PurchaseQuoteCompareLineRequest;
import com.ruoyi.mes.purchase.domain.dto.PurchaseQuoteCompareRequest;
import com.ruoyi.mes.purchase.domain.dto.PurchaseQuoteCompareResult;
import com.ruoyi.mes.purchase.domain.dto.PurchaseQuoteRecommendation;
import com.ruoyi.mes.purchase.mapper.PurchaseSupplierQuoteMapper;
import com.ruoyi.mes.purchase.service.IPurchaseSupplierQuoteService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 供应商报价业务处理。 */
@Service
public class PurchaseSupplierQuoteServiceImpl implements IPurchaseSupplierQuoteService
{
    private static final String ACTIVE_SUPPLIER_STATUS = "NORMAL";
    private static final String ACTIVE_MATERIAL_STATUS = "0";
    private static final String TAX_INCLUDED = "Y";
    private static final String TAX_EXCLUDED_BASIS = "TAX_EXCLUDED";
    private static final String LOWEST_VALID_PRICE = "LOWEST_VALID_PRICE";
    private static final String READY = "READY";
    private static final String NEED_INPUT = "NEED_INPUT";
    private static final String INVALID = "INVALID";
    private static final String NO_CANDIDATE = "NO_CANDIDATE";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final PurchaseSupplierQuoteMapper quoteMapper;
    private final ISupplierService supplierService;
    private final IMaterialService materialService;

    public PurchaseSupplierQuoteServiceImpl(PurchaseSupplierQuoteMapper quoteMapper,
                                             ISupplierService supplierService,
                                             IMaterialService materialService)
    {
        this.quoteMapper = quoteMapper;
        this.supplierService = supplierService;
        this.materialService = materialService;
    }

    @Override
    public List<PurchaseSupplierQuote> selectList(PurchaseSupplierQuote query)
    {
        return quoteMapper.selectList(query);
    }

    @Override
    public PurchaseSupplierQuote selectById(Long id)
    {
        PurchaseSupplierQuote quote = quoteMapper.selectById(id);
        if (quote != null) quote.setLines(quoteMapper.selectLinesByQuoteId(id));
        return quote;
    }

    @Override
    public boolean checkQuoteCodeUnique(PurchaseSupplierQuote quote)
    {
        PurchaseSupplierQuote existing = quoteMapper.selectByCode(quote.getQuoteCode());
        return existing == null || existing.getId().equals(quote.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(PurchaseSupplierQuote quote)
    {
        normalizeAndValidate(quote, true);
        quote.setStatus(PurchaseQuoteStatus.DRAFT.getCode());
        quoteMapper.insert(quote);
        insertLines(quote);
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(PurchaseSupplierQuote quote)
    {
        PurchaseSupplierQuote existing = required(quote.getId());
        if (!PurchaseQuoteStatus.DRAFT.getCode().equals(existing.getStatus()))
            throw new ServiceException("已审核或已作废的报价不允许编辑");
        normalizeAndValidate(quote, false);
        quoteMapper.update(quote);
        Long[] ids = {quote.getId()};
        quoteMapper.deleteLinesByQuoteIds(ids);
        insertLines(quote);
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] ids)
    {
        for (Long id : ids)
        {
            PurchaseSupplierQuote quote = required(id);
            if (!PurchaseQuoteStatus.DRAFT.getCode().equals(quote.getStatus()))
                throw new ServiceException("只有草稿报价允许删除");
        }
        quoteMapper.deleteLinesByQuoteIds(ids);
        return quoteMapper.deleteByIds(ids);
    }

    @Override
    public void approve(Long id, String operator)
    {
        PurchaseSupplierQuote quote = required(id);
        if (!PurchaseQuoteStatus.DRAFT.getCode().equals(quote.getStatus()))
            throw new ServiceException("只有草稿报价允许审核");
        if (quote.getExpireDate() != null && quote.getExpireDate().isBefore(quote.getEffectiveDate()))
            throw new ServiceException("报价失效日期不能早于生效日期");
        if (quoteMapper.updateStatus(id, PurchaseQuoteStatus.DRAFT.getCode(),
            PurchaseQuoteStatus.APPROVED.getCode(), operator) != 1)
            throw new ServiceException("报价状态已变化，请刷新后重试");
    }

    @Override
    public void unapprove(Long id, String operator)
    {
        PurchaseSupplierQuote quote = required(id);
        if (!PurchaseQuoteStatus.APPROVED.getCode().equals(quote.getStatus()))
            throw new ServiceException("只有已审核报价允许弃审");
        if (quoteMapper.updateStatus(id, PurchaseQuoteStatus.APPROVED.getCode(),
            PurchaseQuoteStatus.DRAFT.getCode(), operator) != 1)
            throw new ServiceException("报价状态已变化，请刷新后重试");
    }

    @Override
    public PurchaseQuoteCompareResult compare(PurchaseQuoteCompareRequest request)
    {
        if (request == null || request.lines() == null || request.lines().isEmpty())
            return new PurchaseQuoteCompareResult(NEED_INPUT, "MISSING_REQUIRED_FIELDS", "ASK_USER",
                "缺少报价比较明细，必须传 lines 数组。", List.of("lines"), List.of(), List.of());
        String currency = defaultCurrency(request.currency());
        String basis = StringUtils.isBlank(request.priceBasis()) ? "TAX_INCLUDED" : request.priceBasis().trim();
        if (!"TAX_INCLUDED".equals(basis) && !TAX_EXCLUDED_BASIS.equals(basis))
            return invalid("priceBasis 必须是 TAX_INCLUDED 或 TAX_EXCLUDED。", "priceBasis");
        String strategy = StringUtils.isBlank(request.strategy()) ? LOWEST_VALID_PRICE : request.strategy().trim();
        if (!LOWEST_VALID_PRICE.equals(strategy))
            return invalid("strategy 当前只支持 LOWEST_VALID_PRICE。", "strategy");

        List<PurchaseQuoteRecommendation> recommendations = new ArrayList<>();
        List<PurchaseQuoteCandidate> allCandidates = new ArrayList<>();
        for (int index = 0; index < request.lines().size(); index++)
        {
            PurchaseQuoteCompareLineRequest line = request.lines().get(index);
            String prefix = "lines[" + index + "].";
            if (line == null || StringUtils.isBlank(line.materialCode()))
                return needInput(prefix + "materialCode", "缺少物料编码，必须传 materialCode。");
            if (!positive(line.quantity()))
                return needInput(prefix + "quantity", "采购数量必须大于0，必须传 quantity。");
            LocalDate asOfDate = LocalDate.now();
            if (StringUtils.isNotBlank(line.requiredDate()) && !isDate(line.requiredDate()))
                return invalid("requiredDate 格式必须是 yyyy-MM-dd。", prefix + "requiredDate");
            Material material = materialService.selectMaterialListForAgent(line.materialCode().trim(), null, null,
                ACTIVE_MATERIAL_STATUS).stream()
                .filter(item -> line.materialCode().trim().equalsIgnoreCase(item.getMaterialCode()))
                .findFirst().orElse(null);
            if (material == null)
                return new PurchaseQuoteCompareResult(INVALID, "MATERIAL_NOT_AVAILABLE", "ASK_USER",
                    "未找到启用物料：" + line.materialCode(), List.of(prefix + "materialCode"), List.of(), List.of());
            List<PurchaseQuoteCandidate> candidates = quoteMapper.selectCandidates(material.getMaterialCode(),
                line.quantity(), currency, asOfDate.toString());
            if (candidates.isEmpty())
                return new PurchaseQuoteCompareResult(NO_CANDIDATE, "NO_VALID_QUOTE", "ASK_USER",
                    "物料 " + material.getMaterialCode() + " 当前没有满足数量、币种和有效期条件的报价。",
                    List.of(), List.of(), allCandidates);
            allCandidates.addAll(candidates);
            PurchaseQuoteCandidate selected = candidates.get(0);
            BigDecimal comparisonPrice = TAX_EXCLUDED_BASIS.equals(basis)
                ? taxExcludedPrice(selected) : selected.comparableUnitPrice();
            BigDecimal orderUnitPrice = selected.comparableUnitPrice();
            BigDecimal amount = orderUnitPrice.multiply(line.quantity()).setScale(2, RoundingMode.HALF_UP);
            recommendations.add(new PurchaseQuoteRecommendation(material.getMaterialCode(), material.getMaterialName(),
                scale(line.quantity()), selected.supplierId(), selected.supplierCode(),
                selected.supplierName(), selected.quoteId(), selected.quoteLineId(), selected.quoteCode(),
                scale(selected.unitPrice()), scale(orderUnitPrice), scale(selected.taxRate()), selected.taxIncluded(), selected.currency(),
                scale(comparisonPrice), amount, selected.leadTimeDays(), selected.expireDate(),
                "当前有效报价中按" + (TAX_EXCLUDED_BASIS.equals(basis) ? "未税" : "含税") + "可比单价最低"));
        }
        return new PurchaseQuoteCompareResult(READY, "QUOTE_MATCHED", "SHOW_RECOMMENDATION",
            "已找到当前有效报价，请向用户展示推荐供应商和报价有效期，确认后再准备采购订单。",
            List.of(), recommendations, allCandidates);
    }

    @Override
    public boolean validateSelection(Long quoteId, Long quoteLineId, String supplierCode, String materialCode,
                                     BigDecimal quantity, BigDecimal orderUnitPrice)
    {
        if (quoteId == null || quoteLineId == null || StringUtils.isBlank(supplierCode)
            || StringUtils.isBlank(materialCode) || !positive(quantity) || orderUnitPrice == null) return false;
        PurchaseSupplierQuote quote = quoteMapper.selectById(quoteId);
        if (quote == null || !PurchaseQuoteStatus.APPROVED.getCode().equals(quote.getStatus())
            || !supplierCode.equalsIgnoreCase(quote.getSupplierCode())) return false;
        LocalDate today = LocalDate.now();
        if (quote.getEffectiveDate().isAfter(today)
            || (quote.getExpireDate() != null && quote.getExpireDate().isBefore(today))) return false;
        PurchaseSupplierQuoteLine line = quoteMapper.selectLinesByQuoteId(quoteId).stream()
            .filter(item -> quoteLineId.equals(item.getId()) && materialCode.equalsIgnoreCase(item.getMaterialCode()))
            .findFirst().orElse(null);
        if (line == null || quantity.compareTo(line.getMinOrderQuantity()) < 0
            || (line.getMinQuantity() != null && quantity.compareTo(line.getMinQuantity()) < 0)
            || (line.getMaxQuantity() != null && quantity.compareTo(line.getMaxQuantity()) > 0)) return false;
        BigDecimal expected = TAX_INCLUDED.equals(quote.getTaxIncluded()) ? line.getUnitPrice()
            : line.getUnitPrice().multiply(BigDecimal.ONE.add(line.getTaxRate().divide(BigDecimal.valueOf(100), 8,
                RoundingMode.HALF_UP)));
        return expected.subtract(orderUnitPrice).abs().compareTo(new BigDecimal("0.0001")) <= 0;
    }

    private void normalizeAndValidate(PurchaseSupplierQuote quote, boolean create)
    {
        if (quote.getExpireDate() != null && quote.getExpireDate().isBefore(quote.getEffectiveDate()))
            throw new ServiceException("报价失效日期不能早于生效日期");
        Supplier supplier = supplierService.selectById(quote.getSupplierId());
        if (supplier == null) throw new ServiceException("供应商不存在");
        if (!ACTIVE_SUPPLIER_STATUS.equals(supplier.getStatus())) throw new ServiceException("供应商已停用");
        quote.setSupplierCode(supplier.getSupplierCode());
        quote.setSupplierName(supplier.getSupplierName());
        quote.setCurrency(defaultCurrency(quote.getCurrency()));
        quote.setTaxIncluded(quote.getTaxIncluded().toUpperCase(Locale.ROOT));
        if (!"Y".equals(quote.getTaxIncluded()) && !"N".equals(quote.getTaxIncluded()))
            throw new ServiceException("含税标识只能是 Y 或 N");
        if (quote.getLines() == null || quote.getLines().isEmpty()) throw new ServiceException("报价单至少需要一条明细");
        if (quote.getStatus() == null || create) quote.setStatus(PurchaseQuoteStatus.DRAFT.getCode());
        for (PurchaseSupplierQuoteLine line : quote.getLines())
        {
            Material material = materialService.selectMaterialById(line.getMaterialId());
            if (material == null) throw new ServiceException("报价物料不存在");
            if (!ACTIVE_MATERIAL_STATUS.equals(material.getStatus())) throw new ServiceException("报价物料已停用");
            line.setMaterialCode(material.getMaterialCode());
            line.setMaterialName(material.getMaterialName());
            line.setSpec(material.getSpec());
            line.setUnit(material.getUnit());
            if (line.getMinQuantity() != null && line.getMaxQuantity() != null
                && line.getMaxQuantity().compareTo(line.getMinQuantity()) < 0)
                throw new ServiceException("报价阶梯结束数量不能小于起始数量");
        }
    }

    private void insertLines(PurchaseSupplierQuote quote)
    {
        for (int index = 0; index < quote.getLines().size(); index++)
        {
            PurchaseSupplierQuoteLine line = quote.getLines().get(index);
            line.setQuoteId(quote.getId());
            if (line.getLineNo() == null) line.setLineNo(index + 1);
            quoteMapper.insertLine(line);
        }
    }

    private PurchaseSupplierQuote required(Long id)
    {
        PurchaseSupplierQuote quote = quoteMapper.selectById(id);
        if (quote == null) throw new ServiceException("供应商报价不存在");
        return quote;
    }

    private BigDecimal taxExcludedPrice(PurchaseQuoteCandidate candidate)
    {
        if (!candidate.taxIncluded()) return candidate.unitPrice();
        BigDecimal divisor = BigDecimal.ONE.add(candidate.taxRate().divide(BigDecimal.valueOf(100), 8,
            RoundingMode.HALF_UP));
        return candidate.unitPrice().divide(divisor, 6, RoundingMode.HALF_UP);
    }

    private PurchaseQuoteCompareResult needInput(String field, String message)
    {
        return new PurchaseQuoteCompareResult(NEED_INPUT, "MISSING_REQUIRED_FIELDS", "ASK_USER", message,
            List.of(field), List.of(), List.of());
    }

    private PurchaseQuoteCompareResult invalid(String message, String field)
    {
        return new PurchaseQuoteCompareResult(INVALID, "INVALID_ARGUMENT", "ASK_USER", message,
            List.of(field), List.of(), List.of());
    }

    private boolean positive(BigDecimal value) { return value != null && value.compareTo(BigDecimal.ZERO) > 0; }

    private boolean isDate(String value)
    {
        try { LocalDate.parse(value, DATE_FORMATTER); return true; }
        catch (DateTimeParseException ignored) { return false; }
    }

    private String defaultCurrency(String value)
    {
        return StringUtils.isBlank(value) ? "CNY" : value.trim().toUpperCase(Locale.ROOT);
    }

    private BigDecimal scale(BigDecimal value)
    {
        return value == null ? null : value.setScale(6, RoundingMode.HALF_UP).stripTrailingZeros();
    }
}
