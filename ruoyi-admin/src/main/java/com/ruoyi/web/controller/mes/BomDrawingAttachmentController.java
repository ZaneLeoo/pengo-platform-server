package com.ruoyi.web.controller.mes;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mes.base.domain.BomDrawingAttachment;
import com.ruoyi.mes.base.service.IBomDrawingAttachmentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** BOM 图纸附件接口。 */
@RestController
@RequestMapping("/mes/base/bomMaster/{bomMasterId}/attachments")
public class BomDrawingAttachmentController extends BaseController {
    private final IBomDrawingAttachmentService service;

    public BomDrawingAttachmentController(IBomDrawingAttachmentService service) {
        this.service = service;
    }

    /** 查询图纸附件。 */
    @PreAuthorize("@ss.hasPermi('base:bomMaster:query')")
    @GetMapping
    public AjaxResult list(@PathVariable Long bomMasterId) {
        return success(service.list(bomMasterId));
    }

    /** 登记已上传的图纸附件。 */
    @PreAuthorize("@ss.hasPermi('base:bomMaster:edit')")
    @Log(title = "BOM图纸附件", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(
            @PathVariable Long bomMasterId,
            @Valid @RequestBody BomDrawingAttachment attachment) {
        return success(service.add(bomMasterId, attachment, getUsername()));
    }

    /** 删除图纸附件关系。 */
    @PreAuthorize("@ss.hasPermi('base:bomMaster:edit')")
    @Log(title = "BOM图纸附件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{attachmentId}")
    public AjaxResult delete(
            @PathVariable Long bomMasterId, @PathVariable Long attachmentId) {
        service.delete(bomMasterId, attachmentId);
        return success();
    }
}
