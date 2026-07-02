package com.ruoyi.mes.base.service.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ruoyi.mes.base.domain.ocr.BomOcrDocument;
import com.ruoyi.mes.base.domain.ocr.BomOcrIssue;
import com.ruoyi.mes.base.domain.ocr.BomOcrItem;
import com.ruoyi.mes.base.domain.ocr.BomOcrResult;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class BomImportDraftValidatorTest
{
    private final BomImportDraftValidator validator = new BomImportDraftValidator();

    @Test
    void shouldWarnWhenOnlyPartialRowsAreRecognized()
    {
        BomOcrResult result = new BomOcrResult();
        BomOcrDocument document = new BomOcrDocument();
        document.setParentNameCandidate("1588");
        document.setDrawingNo("1588-003-5200-9000");
        document.setRevision("A");
        document.setBaseQtyCandidate(BigDecimal.ONE);
        document.setTotalRows(56);
        result.setDocument(document);
        result.setItems(List.of(validItem(21), validItem(22)));

        List<BomOcrIssue> issues = validator.validate(result);

        assertTrue(issues.stream().anyMatch(issue -> "PARTIAL_ROWS".equals(issue.getCode())));
    }

    @Test
    void shouldReportInvalidItemFields()
    {
        BomOcrResult result = new BomOcrResult();
        BomOcrDocument document = new BomOcrDocument();
        document.setParentNameCandidate("1588");
        document.setDrawingNo("1588-003-5200-9000");
        document.setRevision("A");
        document.setBaseQtyCandidate(BigDecimal.ONE);
        result.setDocument(document);

        BomOcrItem first = validItem(21);
        BomOcrItem duplicate = validItem(21);
        BomOcrItem invalid = new BomOcrItem();
        invalid.setLineNo(23);
        invalid.setQuantity(BigDecimal.ZERO);
        result.setItems(List.of(first, duplicate, invalid));

        List<BomOcrIssue> issues = validator.validate(result);

        assertTrue(issues.stream().anyMatch(issue -> "DUPLICATE_LINE_NO".equals(issue.getCode())));
        assertTrue(issues.stream().anyMatch(issue -> "MISSING_ITEM_NAME".equals(issue.getCode())));
        assertTrue(issues.stream().anyMatch(issue -> "INVALID_QUANTITY".equals(issue.getCode())));
    }

    private BomOcrItem validItem(int lineNo)
    {
        BomOcrItem item = new BomOcrItem();
        item.setLineNo(lineNo);
        item.setItemName("内六角螺钉");
        item.setQuantity(BigDecimal.ONE);
        item.setComponentCodeCandidate("6000019358");
        return item;
    }
}
