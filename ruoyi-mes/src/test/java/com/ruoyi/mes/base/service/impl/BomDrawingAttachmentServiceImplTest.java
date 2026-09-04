package com.ruoyi.mes.base.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mes.base.domain.BomDrawingAttachment;
import com.ruoyi.mes.base.domain.BomMaster;
import com.ruoyi.mes.base.mapper.BomDrawingAttachmentMapper;
import com.ruoyi.mes.base.mapper.BomMasterMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests BOM drawing attachment ownership and limits. */
@ExtendWith(MockitoExtension.class)
class BomDrawingAttachmentServiceImplTest {
    @Mock private BomDrawingAttachmentMapper attachmentMapper;
    @Mock private BomMasterMapper bomMasterMapper;

    @Test
    void addsAttachmentToExistingBom() {
        BomDrawingAttachmentServiceImpl service = service();
        when(bomMasterMapper.selectBomMasterById(10L)).thenReturn(new BomMaster());
        when(attachmentMapper.selectByBomMasterId(10L)).thenReturn(List.of());
        when(attachmentMapper.insert(any()))
                .thenAnswer(
                        invocation -> {
                            BomDrawingAttachment attachment = invocation.getArgument(0);
                            attachment.setAttachmentId(99L);
                            return 1;
                        });
        BomDrawingAttachment saved = attachment("Main.PDF", " /profile/main.pdf ");
        when(attachmentMapper.selectById(99L)).thenReturn(saved);

        service.add(10L, saved, "tester");

        assertEquals(10L, saved.getBomMasterId());
        assertEquals("pdf", saved.getFileExt());
        assertEquals("/profile/main.pdf", saved.getFileUrl());
        assertEquals("tester", saved.getUploadBy());
        verify(attachmentMapper).insert(saved);
    }

    @Test
    void refusesAttachmentForMissingBom() {
        BomDrawingAttachmentServiceImpl service = service();
        when(bomMasterMapper.selectBomMasterById(10L)).thenReturn(null);

        ServiceException error =
                assertThrows(
                        ServiceException.class,
                        () -> service.add(10L, attachment("main.pdf", "/main.pdf"), "tester"));

        assertEquals("BOM不存在", error.getMessage());
        verify(attachmentMapper, never()).insert(any());
    }

    @Test
    void refusesMoreThanTwentyAttachments() {
        BomDrawingAttachmentServiceImpl service = service();
        when(bomMasterMapper.selectBomMasterById(10L)).thenReturn(new BomMaster());
        when(attachmentMapper.selectByBomMasterId(10L))
                .thenReturn(java.util.Collections.nCopies(20, new BomDrawingAttachment()));

        ServiceException error =
                assertThrows(
                        ServiceException.class,
                        () -> service.add(10L, attachment("main.pdf", "/main.pdf"), "tester"));

        assertEquals("每个BOM最多上传20个图纸附件", error.getMessage());
        verify(attachmentMapper, never()).insert(any());
    }

    @Test
    void refusesUnsupportedAttachmentType() {
        BomDrawingAttachmentServiceImpl service = service();
        when(bomMasterMapper.selectBomMasterById(10L)).thenReturn(new BomMaster());
        when(attachmentMapper.selectByBomMasterId(10L)).thenReturn(List.of());
        BomDrawingAttachment attachment = attachment("script.exe", "/script.exe");
        attachment.setFileExt(null);

        ServiceException error =
                assertThrows(ServiceException.class, () -> service.add(10L, attachment, "tester"));

        assertEquals("不支持该图纸附件格式", error.getMessage());
        verify(attachmentMapper, never()).insert(any());
    }

    @Test
    void refusesAttachmentLargerThanFiftyMegabytes() {
        BomDrawingAttachmentServiceImpl service = service();
        when(bomMasterMapper.selectBomMasterById(10L)).thenReturn(new BomMaster());
        when(attachmentMapper.selectByBomMasterId(10L)).thenReturn(List.of());
        BomDrawingAttachment attachment = attachment("main.pdf", "/main.pdf");
        attachment.setFileSize(50L * 1024 * 1024 + 1);

        ServiceException error =
                assertThrows(ServiceException.class, () -> service.add(10L, attachment, "tester"));

        assertEquals("图纸附件大小不能超过50MB", error.getMessage());
        verify(attachmentMapper, never()).insert(any());
    }

    @Test
    void refusesDeletingAttachmentFromAnotherBom() {
        BomDrawingAttachmentServiceImpl service = service();
        when(bomMasterMapper.selectBomMasterById(10L)).thenReturn(new BomMaster());
        when(attachmentMapper.delete(10L, 99L)).thenReturn(0);

        ServiceException error =
                assertThrows(ServiceException.class, () -> service.delete(10L, 99L));

        assertEquals("图纸附件不存在或不属于当前BOM", error.getMessage());
    }

    private BomDrawingAttachmentServiceImpl service() {
        return new BomDrawingAttachmentServiceImpl(attachmentMapper, bomMasterMapper);
    }

    private BomDrawingAttachment attachment(String name, String url) {
        BomDrawingAttachment attachment = new BomDrawingAttachment();
        attachment.setFileName(name);
        attachment.setFileUrl(url);
        attachment.setFileExt("PDF");
        attachment.setFileSize(1024L);
        return attachment;
    }
}
