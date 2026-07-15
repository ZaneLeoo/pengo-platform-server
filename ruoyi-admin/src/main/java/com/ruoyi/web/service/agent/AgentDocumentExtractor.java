package com.ruoyi.web.service.agent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

/** 从用户上传的常见文档中提取可供模型阅读的纯文本。 */
@Component
public class AgentDocumentExtractor {
    private static final int MAX_EXTRACTED_CHARACTERS = 50_000;

    /** 按文件扩展名提取正文并限制上下文长度。 */
    public ExtractionResult extract(byte[] content, String extension) throws IOException {
        String text;
        try {
            text = switch (extension) {
                case "pdf" -> extractPdf(content);
                case "docx" -> extractWord(content);
                case "xlsx" -> extractExcel(content);
                case "pptx" -> extractPowerPoint(content);
                case "txt", "csv" -> new String(content, StandardCharsets.UTF_8);
                default -> throw new IOException("不支持提取该文档格式");
            };
        } catch (IOException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new IOException("无法读取文档内容，文件可能已损坏、加密或格式不受支持", exception);
        }
        text = normalize(text);
        if (text.isBlank()) {
            throw new IOException("文档中没有可提取的文本内容");
        }
        boolean truncated = text.length() > MAX_EXTRACTED_CHARACTERS;
        if (truncated) {
            text = text.substring(0, MAX_EXTRACTED_CHARACTERS);
        }
        return new ExtractionResult(text, text.length(), truncated);
    }

    private String extractPdf(byte[] content) throws IOException {
        try (PDDocument document = PDDocument.load(content)) {
            if (document.isEncrypted()) {
                throw new IOException("不支持读取加密 PDF，请先解除密码保护");
            }
            return new PDFTextStripper().getText(document);
        }
    }

    private String extractWord(byte[] content) throws IOException {
        StringBuilder text = new StringBuilder();
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(content))) {
            document.getParagraphs().forEach(paragraph -> text.append(paragraph.getText()).append('\n'));
            document.getTables().forEach(table -> table.getRows().forEach(row -> {
                row.getTableCells().forEach(cell -> text.append(cell.getText()).append('\t'));
                text.append('\n');
            }));
        }
        return text.toString();
    }

    private String extractExcel(byte[] content) throws IOException {
        StringBuilder text = new StringBuilder();
        DataFormatter formatter = new DataFormatter();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            workbook.forEach(sheet -> {
                text.append("[工作表：").append(sheet.getSheetName()).append("]\n");
                sheet.forEach(row -> {
                    row.forEach(cell -> text.append(formatter.formatCellValue(cell)).append('\t'));
                    text.append('\n');
                });
            });
        }
        return text.toString();
    }

    private String extractPowerPoint(byte[] content) throws IOException {
        StringBuilder text = new StringBuilder();
        try (XMLSlideShow presentation = new XMLSlideShow(new ByteArrayInputStream(content))) {
            for (int index = 0; index < presentation.getSlides().size(); index++) {
                text.append("[幻灯片 ").append(index + 1).append("]\n");
                for (XSLFShape shape : presentation.getSlides().get(index).getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        text.append(textShape.getText()).append('\n');
                    }
                }
            }
        }
        return text.toString();
    }

    private String normalize(String text) {
        return text.replace("\u0000", "").replaceAll("[\\t\\x0B\\f\\r ]+", " ")
                .replaceAll(" *\\n *", "\n").replaceAll("\\n{3,}", "\n\n").trim();
    }

    /** 文档提取结果。 */
    @Getter
    @AllArgsConstructor
    public static class ExtractionResult {
        private String text;
        private int characters;
        private boolean truncated;
    }
}
