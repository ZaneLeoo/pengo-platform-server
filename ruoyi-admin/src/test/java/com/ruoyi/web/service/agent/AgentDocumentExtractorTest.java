package com.ruoyi.web.service.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayOutputStream;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class AgentDocumentExtractorTest {
    private final AgentDocumentExtractor extractor = new AgentDocumentExtractor();

    @Test
    void extractsPlainText() throws IOException {
        AgentDocumentExtractor.ExtractionResult result = extractor
                .extract("物料编码,物料名称\nMAT-001,控制板".getBytes(StandardCharsets.UTF_8), "csv");

        assertTrue(result.getText().contains("MAT-001"));
        assertTrue(result.getText().contains("控制板"));
        assertEquals(result.getText().length(), result.getCharacters());
    }

    @Test
    void rejectsEmptyDocument() {
        IOException error = assertThrows(IOException.class,
                () -> extractor.extract("   \n".getBytes(StandardCharsets.UTF_8), "txt"));

        assertTrue(error.getMessage().contains("没有可提取"));
    }

    @Test
    void extractsExcelWorkbook() throws IOException {
        byte[] content;
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            var row = workbook.createSheet("库存").createRow(0);
            row.createCell(0).setCellValue("物料编码");
            row.createCell(1).setCellValue("MAT-001");
            workbook.write(output);
            content = output.toByteArray();
        }

        AgentDocumentExtractor.ExtractionResult result = extractor.extract(content, "xlsx");

        assertTrue(result.getText().contains("MAT-001"));
    }
}
