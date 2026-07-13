package com.ruoyi.web.controller.mes;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mes.base.domain.BomImportTask;
import com.ruoyi.mes.base.domain.dto.BomImportApplyRequest;
import com.ruoyi.mes.base.domain.dto.BomImportCreateRequest;
import com.ruoyi.mes.base.domain.dto.BomImportDraft;
import com.ruoyi.mes.base.service.IBomImportService;
import com.ruoyi.web.service.mes.BomOcrWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** BOM OCR 导入控制器。 */
@RestController
@RequestMapping("/mes/base/bomImport")
public class BomImportController extends BaseController {
    @Autowired
    private IBomImportService bomImportService;

    @Autowired
    private BomOcrWorkflowService bomOcrWorkflowService;

    /** 查询 BOM OCR 导入任务列表。 */
    @PreAuthorize("@ss.hasPermi('base:bomImport:list')")
    @GetMapping("/list")
    public TableDataInfo list(BomImportTask task) {
        startPage();
        return getDataTable(bomImportService.selectBomImportTaskList(task));
    }

    /** 获取 BOM OCR 导入草稿详情。 */
    @PreAuthorize("@ss.hasPermi('base:bomImport:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(bomImportService.selectBomImportDraftById(id));
    }

    /** 从 Dify/大模型结构化结果创建 BOM 导入草稿。 */
    @PreAuthorize("@ss.hasPermi('base:bomImport:add')")
    @Log(title = "BOM OCR导入草稿", businessType = BusinessType.INSERT)
    @PostMapping("/draft")
    public AjaxResult createDraft(@RequestBody BomImportCreateRequest request) {
        return success(bomImportService.createDraft(request, getUsername()));
    }

    /** 上传图纸并调用 BOM_OCR Dify 工作流创建导入草稿。 */
    @PreAuthorize("@ss.hasPermi('base:bomImport:add')")
    @Log(title = "BOM OCR识别导入", businessType = BusinessType.INSERT)
    @PostMapping(value = "/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult recognize(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileVariable", required = false) String fileVariable,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "inputs", required = false) String inputs) {
        return success(bomOcrWorkflowService.recognize(file, fileVariable, query, inputs, getUserId(), getUsername()));
    }

    /** 保存人工修正后的 BOM 导入草稿。 */
    @PreAuthorize("@ss.hasPermi('base:bomImport:edit')")
    @Log(title = "BOM OCR导入草稿", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult updateDraft(@PathVariable Long id, @RequestBody BomImportDraft draft) {
        return success(bomImportService.updateDraft(id, draft, getUsername()));
    }

    /** 校验 BOM 导入草稿。 */
    @PreAuthorize("@ss.hasPermi('base:bomImport:query')")
    @PostMapping("/{id}/validate")
    public AjaxResult validate(@PathVariable Long id) {
        return success(bomImportService.validateDraft(id, getUsername()));
    }

    /** 将 BOM OCR 导入草稿应用到指定 BOM 版本。 */
    @PreAuthorize("@ss.hasPermi('base:bomItem:add')")
    @Log(title = "BOM OCR导入正式BOM", businessType = BusinessType.INSERT)
    @PostMapping("/{id}/import")
    public AjaxResult importToBomVersion(@PathVariable Long id, @RequestBody BomImportApplyRequest request) {
        return success(bomImportService.importToBomVersion(id, request.getBomVersionId(), getUsername()));
    }

    /** 删除 BOM OCR 导入任务。 */
    @PreAuthorize("@ss.hasPermi('base:bomImport:remove')")
    @Log(title = "BOM OCR导入草稿", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(bomImportService.deleteBomImportTaskByIds(ids));
    }
}
