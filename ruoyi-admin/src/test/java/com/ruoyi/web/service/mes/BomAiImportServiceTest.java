package com.ruoyi.web.service.mes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import com.ruoyi.agent.application.DifyAppConfigService;
import com.ruoyi.agent.infrastructure.dify.DifyClientSettings;
import com.ruoyi.agent.infrastructure.dify.DifyWorkflowClient;
import com.ruoyi.agent.infrastructure.dify.model.DifyFileUploadResult;
import com.ruoyi.agent.infrastructure.dify.model.DifyWorkflowRunRequest;
import com.ruoyi.agent.infrastructure.dify.model.DifyWorkflowRunResult;
import com.ruoyi.mes.base.domain.BomMaster;
import com.ruoyi.mes.base.domain.BomVersion;
import com.ruoyi.mes.base.domain.Material;
import com.ruoyi.mes.base.mapper.BomMasterMapper;
import com.ruoyi.mes.base.service.IBomItemService;
import com.ruoyi.mes.base.service.IBomMasterService;
import com.ruoyi.mes.base.service.IBomVersionService;
import com.ruoyi.mes.base.service.IMaterialService;
import com.ruoyi.web.domain.BomAiImportTrace;
import com.ruoyi.web.domain.dto.BomAiDocument;
import com.ruoyi.web.domain.dto.BomAiImportConfirmRequest;
import com.ruoyi.web.domain.dto.BomAiImportHeader;
import com.ruoyi.web.domain.dto.BomAiImportItem;
import com.ruoyi.web.domain.dto.BomAiPreviewResult;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/** Tests the request-variable contract of the BOM_OCR Dify workflow. */
@ExtendWith(MockitoExtension.class)
class BomAiImportServiceTest {

    @Mock
    private DifyWorkflowClient difyWorkflowClient;

    @Mock
    private DifyAppConfigService difyAppConfigService;

    @Mock
    private IBomMasterService bomMasterService;

    @Mock
    private IBomVersionService bomVersionService;

    @Mock
    private IBomItemService bomItemService;

    @Mock
    private IMaterialService materialService;

    @Mock
    private BomMasterMapper bomMasterMapper;

    @Mock
    private BomAiImportTraceService bomAiImportTraceService;

    @InjectMocks
    private BomAiImportService service;

    @Test
    void sendsImageFilesToBomImages() throws Exception {
        DifyWorkflowRunRequest request = recognizeAndCapture(new MockMultipartFile("files", "drawing.png", "image/png",
                new byte[] { 1 }));

        Map<String, Object> inputs = request.getInputs();
        assertTrue(inputs.containsKey("bom_images"));
        assertFalse(inputs.containsKey("bom_pdf"));
        assertFalse(inputs.containsKey("bom_files"));

        List<Map<String, Object>> images = fileList(inputs.get("bom_images"));
        assertEquals(1, images.size());
        assertEquals("image", images.get(0).get("type"));
        assertEquals("uploaded-file-id", images.get(0).get("upload_file_id"));
    }

    @Test
    void sendsPdfFileToBomPdf() throws Exception {
        DifyWorkflowRunRequest request = recognizeAndCapture(new MockMultipartFile("files", "drawing.pdf",
                "application/pdf", pdfBytes(1)));

        Map<String, Object> inputs = request.getInputs();
        assertFalse(inputs.containsKey("bom_images"));
        assertTrue(inputs.containsKey("bom_pdf"));
        assertFalse(inputs.containsKey("bom_files"));

        List<Map<String, Object>> pdfFiles = fileList(inputs.get("bom_pdf"));
        assertEquals(1, pdfFiles.size());
        Map<String, Object> pdf = pdfFiles.get(0);
        assertEquals("document", pdf.get("type"));
        assertEquals("uploaded-file-id", pdf.get("upload_file_id"));
    }

    @Test
    void rejectsMultiplePdfFiles() throws Exception {
        BomAiPreviewResult result = service.recognize(new MultipartFile[] {
                new MockMultipartFile("files", "drawing-1.pdf", "application/pdf", pdfBytes(1)),
                new MockMultipartFile("files", "drawing-2.pdf", "application/pdf", pdfBytes(1)) });

        assertFalse(result.isSuccess());
        assertEquals("识别失败：一次仅支持上传 1 个 PDF", result.getError());
    }

    @Test
    void rejectsMixedImageAndPdfInput() {
        BomAiPreviewResult result = service.recognize(new MultipartFile[] {
                new MockMultipartFile("files", "drawing.png", "image/png", new byte[] { 1 }),
                new MockMultipartFile("files", "drawing.pdf", "application/pdf", new byte[] { 2 }) });

        assertFalse(result.isSuccess());
        assertEquals("识别失败：一次只能上传多张图片或 1 个 PDF，不能混合上传", result.getError());
    }

    @Test
    void rejectsPdfOverTwentyPages() throws Exception {
        BomAiPreviewResult result = service.recognize(new MultipartFile[] {
                new MockMultipartFile("files", "drawing.pdf", "application/pdf", pdfBytes(21)) });

        assertFalse(result.isSuccess());
        assertEquals("识别失败：PDF 页数不能超过 20 页", result.getError());
    }

    @Test
    void confirmsBomMasterAsManufacturingAndEnabled() {
        when(bomMasterMapper.selectBomMasterByCode(any())).thenReturn(null);
        when(materialService.selectMaterialByCode("PARENT-001")).thenReturn(material("PARENT-001", 1L));
        when(materialService.selectMaterialByCode("COMPONENT-001")).thenReturn(material("COMPONENT-001", 2L));

        BomAiImportHeader header = new BomAiImportHeader();
        header.setParentItemCode("PARENT-001");
        header.setParentItemName("母件");
        BomAiImportItem item = new BomAiImportItem();
        item.setComponentCode("COMPONENT-001");
        item.setItemName("子件");
        item.setQuantity(BigDecimal.ONE);
        BomAiDocument document = new BomAiDocument();
        document.setHeader(header);
        document.setItems(List.of(item));
        BomAiImportConfirmRequest request = new BomAiImportConfirmRequest();
        request.setDocuments(List.of(document));

        assertTrue(service.confirm(request).isSuccess());
        ArgumentCaptor<BomMaster> captor = ArgumentCaptor.forClass(BomMaster.class);
        verify(bomMasterService).insertBomMaster(captor.capture());
        assertEquals("MANUFACTURING", captor.getValue().getBomType());
        assertEquals("ENABLED", captor.getValue().getStatus());
    }

    @Test
    void rejectsConfirmWhenMaterialCodeDoesNotExist() {
        BomAiImportHeader header = new BomAiImportHeader();
        header.setParentItemCode("PARENT-001");
        header.setParentItemName("母件");
        BomAiImportItem item = new BomAiImportItem();
        item.setComponentCode("UNKNOWN-001");
        item.setItemName("未建物料");
        item.setQuantity(BigDecimal.ONE);
        BomAiDocument document = new BomAiDocument();
        document.setHeader(header);
        document.setItems(List.of(item));
        BomAiImportConfirmRequest request = new BomAiImportConfirmRequest();
        request.setDocuments(List.of(document));

        when(materialService.selectMaterialByCode("PARENT-001")).thenReturn(material("PARENT-001", 1L));

        assertFalse(service.confirm(request).isSuccess());
    }

    @Test
    void reusesExistingMasterAndIncrementsVersion() {
        BomMaster existing = new BomMaster();
        existing.setId(9L);
        existing.setBomCode("BOM-PARENT-001");
        when(bomMasterMapper.selectBomMasterByParentItem(1L, "MANUFACTURING")).thenReturn(existing);
        when(materialService.selectMaterialByCode("PARENT-001")).thenReturn(material("PARENT-001", 1L));
        when(materialService.selectMaterialByCode("COMPONENT-001")).thenReturn(material("COMPONENT-001", 2L));
        BomVersion v10 = new BomVersion();
        v10.setVersionCode("V1.0");
        BomVersion v12 = new BomVersion();
        v12.setVersionCode("V1.2");
        when(bomVersionService.selectBomVersionList(any())).thenReturn(List.of(v10, v12));

        BomAiImportHeader header = new BomAiImportHeader();
        header.setParentItemCode("PARENT-001");
        header.setParentItemName("母件");
        BomAiImportItem item = new BomAiImportItem();
        item.setComponentCode("COMPONENT-001");
        item.setItemName("子件");
        item.setQuantity(BigDecimal.ONE);
        BomAiDocument document = new BomAiDocument();
        document.setHeader(header);
        document.setItems(List.of(item));
        BomAiImportConfirmRequest request = new BomAiImportConfirmRequest();
        request.setDocuments(List.of(document));

        assertTrue(service.confirm(request).isSuccess());
        verify(bomMasterService, never()).insertBomMaster(any());
        ArgumentCaptor<BomVersion> versionCaptor = ArgumentCaptor.forClass(BomVersion.class);
        verify(bomVersionService).insertBomVersion(versionCaptor.capture());
        assertEquals("V1.3", versionCaptor.getValue().getVersionCode());
        assertEquals(9L, versionCaptor.getValue().getBomMasterId());
    }

    @Test
    void rejectsNonBomDocumentReturnedByWorkflow() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "random.jpg", "image/jpeg", new byte[] { 1 });
        when(difyAppConfigService.requireSettings("BOM_OCR"))
                .thenReturn(new DifyClientSettings("http://dify.test/v1", "api-key"));
        when(bomAiImportTraceService.start(any(), any())).thenReturn(trace());
        when(difyWorkflowClient.uploadFile(any(), any()))
                .thenReturn(new DifyFileUploadResult("uploaded-file-id", file.getOriginalFilename(), 1, null, null));
        when(difyWorkflowClient.runBlocking(any(), any())).thenReturn(new DifyWorkflowRunResult(null, null, "succeeded",
                Map.of("text", "{\"documents\":[{\"pageNo\":1,\"error\":\"非制造业BOM数据\"}]}"), null, null));

        BomAiPreviewResult result = service.recognize(new MockMultipartFile[] { file });

        assertFalse(result.isSuccess());
        assertEquals("识别失败：第 1 页：非制造业BOM数据", result.getError());
    }

    private DifyWorkflowRunRequest recognizeAndCapture(MockMultipartFile... files) throws Exception {
        when(difyAppConfigService.requireSettings("BOM_OCR"))
                .thenReturn(new DifyClientSettings("http://dify.test/v1", "api-key"));
        when(bomAiImportTraceService.start(any(), any())).thenReturn(trace());
        when(difyWorkflowClient.uploadFile(any(), any()))
                .thenReturn(new DifyFileUploadResult("uploaded-file-id", files[0].getOriginalFilename(), 1, null, null));
        when(difyWorkflowClient.runBlocking(any(), any()))
                .thenReturn(new DifyWorkflowRunResult(null, null, "succeeded", Collections.emptyMap(), null, null));

        BomAiPreviewResult result = service.recognize(files);
        assertFalse(result.isSuccess());

        ArgumentCaptor<DifyWorkflowRunRequest> captor = ArgumentCaptor.forClass(DifyWorkflowRunRequest.class);
        verify(difyWorkflowClient).runBlocking(any(), captor.capture());
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fileList(Object value) {
        return (List<Map<String, Object>>) value;
    }

    private Material material(String code, Long id) {
        Material material = new Material();
        material.setMaterialId(id);
        material.setMaterialCode(code);
        material.setMaterialName(code + "名称");
        material.setStatus("0");
        return material;
    }

    private BomAiImportTrace trace() {
        BomAiImportTrace trace = new BomAiImportTrace();
        trace.setId(1L);
        trace.setImportNo("BOMAI-TEST");
        return trace;
    }

    private byte[] pdfBytes(int pageCount) throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            for (int index = 0; index < pageCount; index++) {
                document.addPage(new PDPage());
            }
            document.save(output);
            return output.toByteArray();
        }
    }
}
