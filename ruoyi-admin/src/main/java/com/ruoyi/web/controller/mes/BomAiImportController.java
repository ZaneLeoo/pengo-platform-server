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
    public AjaxResult recognize(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return error("请上传图纸文件");
        }
        BomAiPreviewResult result = bomAiImportService.recognize(file);
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
