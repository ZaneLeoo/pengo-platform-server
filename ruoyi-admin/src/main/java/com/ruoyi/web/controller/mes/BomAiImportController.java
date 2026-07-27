package com.ruoyi.web.controller.mes;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.domain.dto.BomAiConfirmResult;
import com.ruoyi.web.domain.dto.BomAiImportConfirmRequest;
import com.ruoyi.web.domain.dto.BomAiPreviewResult;
import com.ruoyi.web.service.mes.BomAiImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * BOM AI 图纸导入控制器。
 */
@RestController
@RequestMapping("/mes/base/bomAiImport")
public class BomAiImportController extends BaseController {

    @Autowired
    private BomAiImportService bomAiImportService;

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
        BomAiPreviewResult result = bomAiImportService.recognize(uploads.toArray(new MultipartFile[0]));
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
        BomAiConfirmResult result = bomAiImportService.confirm(request);
        if (result.isSuccess()) {
            return success(result);
        }
        return error(result.getError());
    }
}
