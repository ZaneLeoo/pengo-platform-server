package com.ruoyi.mes.base.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mes.base.domain.BomDrawingAttachment;
import com.ruoyi.mes.base.mapper.BomDrawingAttachmentMapper;
import com.ruoyi.mes.base.mapper.BomMasterMapper;
import com.ruoyi.mes.base.service.IBomDrawingAttachmentService;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/** BOM 图纸附件业务处理。 */
@Service
public class BomDrawingAttachmentServiceImpl implements IBomDrawingAttachmentService {
    private static final int MAX_ATTACHMENTS = 20;
    private static final long MAX_FILE_SIZE_BYTES = 50L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(
                    "pdf", "dwg", "dxf", "step", "stp", "iges", "igs", "sldprt", "sldasm", "prt",
                    "asm", "png", "jpg", "jpeg", "tif", "tiff", "zip");

    private final BomDrawingAttachmentMapper attachmentMapper;
    private final BomMasterMapper bomMasterMapper;

    public BomDrawingAttachmentServiceImpl(
            BomDrawingAttachmentMapper attachmentMapper, BomMasterMapper bomMasterMapper) {
        this.attachmentMapper = attachmentMapper;
        this.bomMasterMapper = bomMasterMapper;
    }

    @Override
    public List<BomDrawingAttachment> list(Long bomMasterId) {
        requireBom(bomMasterId);
        return attachmentMapper.selectByBomMasterId(bomMasterId);
    }

    @Override
    public BomDrawingAttachment add(
            Long bomMasterId, BomDrawingAttachment attachment, String operator) {
        requireBom(bomMasterId);
        if (attachmentMapper.selectByBomMasterId(bomMasterId).size() >= MAX_ATTACHMENTS) {
            throw new ServiceException("每个BOM最多上传20个图纸附件");
        }
        validateFile(attachment);
        attachment.setAttachmentId(null);
        attachment.setBomMasterId(bomMasterId);
        attachment.setFileName(attachment.getFileName().trim());
        attachment.setFileUrl(attachment.getFileUrl().trim());
        attachment.setFileExt(resolveExtension(attachment));
        attachment.setUploadBy(operator);
        attachmentMapper.insert(attachment);
        return attachmentMapper.selectById(attachment.getAttachmentId());
    }

    @Override
    public void delete(Long bomMasterId, Long attachmentId) {
        requireBom(bomMasterId);
        if (attachmentMapper.delete(bomMasterId, attachmentId) != 1) {
            throw new ServiceException("图纸附件不存在或不属于当前BOM");
        }
    }

    private void requireBom(Long bomMasterId) {
        if (bomMasterId == null || bomMasterMapper.selectBomMasterById(bomMasterId) == null) {
            throw new ServiceException("BOM不存在");
        }
    }

    private void validateFile(BomDrawingAttachment attachment) {
        if (attachment == null
                || attachment.getFileName() == null
                || attachment.getFileName().isBlank()
                || attachment.getFileUrl() == null
                || attachment.getFileUrl().isBlank()) {
            throw new ServiceException("附件名称和地址不能为空");
        }
        String extension = resolveExtension(attachment);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ServiceException("不支持该图纸附件格式");
        }
        if (attachment.getFileSize() != null
                && attachment.getFileSize().longValue() > MAX_FILE_SIZE_BYTES) {
            throw new ServiceException("图纸附件大小不能超过50MB");
        }
    }

    private String resolveExtension(BomDrawingAttachment attachment) {
        String extension = normalizeExtension(attachment.getFileExt());
        if (!extension.isEmpty()) {
            return extension;
        }
        String fileName = attachment.getFileName().trim();
        int separator = fileName.lastIndexOf('.');
        return separator < 0 ? "" : normalizeExtension(fileName.substring(separator + 1));
    }

    private String normalizeExtension(String value) {
        if (value == null) {
            return "";
        }
        String extension = value.trim().toLowerCase();
        return extension.startsWith(".") ? extension.substring(1) : extension;
    }
}
