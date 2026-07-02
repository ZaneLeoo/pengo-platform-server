package com.ruoyi.mes.base.service.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.base.domain.ocr.BomOcrDocument;
import com.ruoyi.mes.base.domain.ocr.BomOcrIssue;
import com.ruoyi.mes.base.domain.ocr.BomOcrItem;
import com.ruoyi.mes.base.domain.ocr.BomOcrResult;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/** BOM OCR 草稿基础校验。 */
@Component
public class BomImportDraftValidator
{
    public List<BomOcrIssue> validate(BomOcrResult result)
    {
        List<BomOcrIssue> issues = new ArrayList<>();
        if (result == null)
        {
            issues.add(error("EMPTY_RESULT", "识别结果不能为空", null, "result"));
            return issues;
        }

        validateDocument(result.getDocument(), issues);
        validateItems(result.getItems(), result.getDocument(), issues);
        return issues;
    }

    private void validateDocument(BomOcrDocument document, List<BomOcrIssue> issues)
    {
        if (document == null)
        {
            issues.add(error("MISSING_DOCUMENT", "缺少图纸标题区识别结果", null, "document"));
            return;
        }
        if (StringUtils.isBlank(document.getParentNameCandidate()) && StringUtils.isBlank(document.getDrawingNo()))
        {
            issues.add(warn("MISSING_PARENT_INFO", "缺少母件名称候选和图纸编号，导入前需要人工补齐母件信息", null, "document"));
        }
        if (StringUtils.isBlank(document.getRevision()))
        {
            issues.add(warn("MISSING_REVISION", "缺少版本/版次，导入前需要确认 BOM 版本号", null, "revision"));
        }
        if (document.getBaseQtyCandidate() == null || BigDecimal.ZERO.compareTo(document.getBaseQtyCandidate()) >= 0)
        {
            issues.add(warn("MISSING_BASE_QTY", "缺少母件基准数量/底数，导入前需要确认", null, "baseQtyCandidate"));
        }
    }

    private void validateItems(List<BomOcrItem> items, BomOcrDocument document, List<BomOcrIssue> issues)
    {
        if (items == null || items.isEmpty())
        {
            issues.add(error("NO_ITEMS", "识别结果没有 BOM 明细行", null, "items"));
            return;
        }

        if (document != null && document.getTotalRows() != null && document.getTotalRows() > items.size())
        {
            issues.add(warn("PARTIAL_ROWS", "图纸总行数为 " + document.getTotalRows()
                + "，当前识别到 " + items.size() + " 行，疑似只识别到部分页或部分区域。", null, "items"));
        }

        Set<Integer> lineNos = new HashSet<>();
        List<Integer> sortedLineNos = new ArrayList<>();
        for (BomOcrItem item : items)
        {
            if (item.getLineNo() == null)
            {
                issues.add(error("MISSING_LINE_NO", "明细行缺少行号", null, "lineNo"));
            }
            else
            {
                if (!lineNos.add(item.getLineNo()))
                {
                    issues.add(error("DUPLICATE_LINE_NO", "行号 " + item.getLineNo() + " 重复", item.getLineNo(), "lineNo"));
                }
                sortedLineNos.add(item.getLineNo());
            }

            if (StringUtils.isBlank(item.getItemName()))
            {
                issues.add(error("MISSING_ITEM_NAME", "行 " + label(item) + " 缺少子件名称", item.getLineNo(), "itemName"));
            }
            if (item.getQuantity() == null || BigDecimal.ZERO.compareTo(item.getQuantity()) >= 0)
            {
                issues.add(error("INVALID_QUANTITY", "行 " + label(item) + " 数量为空或小于等于 0", item.getLineNo(), "quantity"));
            }
            if (StringUtils.isBlank(item.getComponentCodeCandidate()) && StringUtils.isBlank(item.getDrawingNo()))
            {
                issues.add(warn("MISSING_COMPONENT_CODE", "行 " + label(item) + " 缺少子件编码候选和图号", item.getLineNo(), "componentCodeCandidate"));
            }
        }

        appendLineGapIssues(sortedLineNos, issues);
    }

    private void appendLineGapIssues(List<Integer> lineNos, List<BomOcrIssue> issues)
    {
        if (lineNos.size() < 2) return;
        lineNos.sort(Comparator.naturalOrder());
        for (int i = 1; i < lineNos.size(); i++)
        {
            int previous = lineNos.get(i - 1);
            int current = lineNos.get(i);
            if (current - previous > 1)
            {
                issues.add(warn("LINE_NO_GAP", "行号 " + previous + " 到 " + current + " 之间不连续，可能存在漏识别行", null, "lineNo"));
            }
        }
    }

    private String label(BomOcrItem item)
    {
        return item.getLineNo() == null ? "未知" : String.valueOf(item.getLineNo());
    }

    private BomOcrIssue error(String code, String message, Integer lineNo, String field)
    {
        return new BomOcrIssue("error", code, message, lineNo, field);
    }

    private BomOcrIssue warn(String code, String message, Integer lineNo, String field)
    {
        return new BomOcrIssue("warning", code, message, lineNo, field);
    }
}
