package com.ruoyi.web.controller.mes;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.web.domain.BomAiImportTrace;
import com.ruoyi.web.domain.dto.BomAiConfirmResult;
import com.ruoyi.web.domain.dto.BomAiImportConfirmRequest;
import com.ruoyi.web.domain.dto.BomAiPreviewResult;
import com.ruoyi.web.service.mes.BomAiImportTraceService;
import com.ruoyi.web.service.mes.BomAiImportService;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


/**
 * BOM AI 图纸导入控制器。
 */
@RestController
@RequestMapping("/mes/base/bomAiImport")
public class BomAiImportController extends BaseController {

    @Autowired
    private BomAiImportService bomAiImportService;
    @Autowired
    private BomAiImportTraceService bomAiImportTraceService;

    /** 获取当前生效的 AI 图纸导入限制。 */
    @GetMapping("/limits")
    public AjaxResult limits() {
        return success(bomAiImportService.getImportLimits());
    }

    /**
     * 上传图纸 → AI 识别 → 返回预览数据（含物料匹配状态）。
     */
    @PostMapping(value = "/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult recognize(
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        List<MultipartFile> uploads = new ArrayList<>();
        if (files != null) {
            for (MultipartFile current : files) {
                if (current != null && !current.isEmpty()) {
                    uploads.add(current);
                }
            }
        }
        // 保留旧的单文件参数，避免前端尚未更新时接口直接失效。
        if (uploads.isEmpty() && file != null && !file.isEmpty()) {
            uploads.add(file);
        }
        if (uploads.isEmpty()) {
            return error("请上传至少一张图纸");
        }
        BomAiPreviewResult result = bomAiImportService.recognize(uploads.toArray(new MultipartFile[0]), getUsername());
        if (result.isSuccess()) {
            return success(result);
        }
        return error(result.getError());
    }

    /**
     * 确认导入 → 事务写入 BOM 表。
     */
    @PostMapping("/confirm")
    public AjaxResult confirm(@RequestBody BomAiImportConfirmRequest request) {
        BomAiConfirmResult result = bomAiImportService.confirm(request, getUsername());
        if (result.isSuccess()) {
            return success(result);
        }
        return error(result.getError());
    }

    /** 恢复一条仍处于待确认状态的识别预览。 */
    @GetMapping("/trace/{traceId}/resume")
    public AjaxResult resume(@PathVariable Long traceId) {
        return success(bomAiImportTraceService.resume(traceId));
    }

    /** 主动放弃一条待确认记录。 */
    @PostMapping("/trace/{traceId}/cancel")
    public AjaxResult cancel(@PathVariable Long traceId) {
        bomAiImportTraceService.cancel(traceId, getUsername());
        return success();
    }

    /** 查询一次 AI 图纸识别/导入的追溯详情。 */
    @GetMapping("/trace/{traceId}")
    public AjaxResult trace(@PathVariable Long traceId) {
        return success(bomAiImportTraceService.selectById(traceId));
    }

    /** 分页查询 AI 图纸导入追溯记录。 */
    @GetMapping("/trace/list")
    public TableDataInfo traceList(BomAiImportTrace trace) {
        startPage();
        return getDataTable(bomAiImportTraceService.selectList(trace));
    }

    /** 下载某次 AI 图纸识别保存的原始文件。 */
    @GetMapping("/trace/{traceId}/files/{resourceId}")
    public ResponseEntity<FileSystemResource> downloadSourceFile(@PathVariable Long traceId,
            @PathVariable String resourceId) throws IOException {
        BomAiImportTraceService.StoredSourceFile stored = bomAiImportTraceService.resolveSourceFile(traceId, resourceId);
        String mediaType = stored.getSourceFile().getMediaType();
        if (mediaType == null || mediaType.isBlank()) {
            mediaType = Files.probeContentType(stored.getPath());
        }
        MediaType contentType = mediaType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(mediaType);
        return ResponseEntity.ok()
                .contentType(contentType)
                .contentLength(Files.size(stored.getPath()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(stored.getSourceFile().getOriginalFilename(), java.nio.charset.StandardCharsets.UTF_8)
                        .build().toString())
                .body(new FileSystemResource(stored.getPath()));
    }
}
