package com.ruoyi.mes.base.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mes.base.domain.BomItem;
import com.ruoyi.mes.base.domain.BomVersion;
import com.ruoyi.mes.base.mapper.BomItemMapper;
import com.ruoyi.mes.base.mapper.BomVersionMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests the server-side BOM version lifecycle rules. */
@ExtendWith(MockitoExtension.class)
class BomVersionServiceImplTest {

    @Mock
    private BomVersionMapper bomVersionMapper;

    @Mock
    private BomItemMapper bomItemMapper;

    @InjectMocks
    private BomVersionServiceImpl service;

    @Test
    void activatesOnlyCompleteDraftVersion() {
        BomVersion version = version("DRAFT", "PENDING", 0);
        when(bomVersionMapper.selectBomVersionById(1L)).thenReturn(version);
        when(bomItemMapper.selectBomItemList(any())).thenReturn(List.of(validItem()));

        service.activateBomVersion(1L, "tester");

        ArgumentCaptor<BomVersion> captor = ArgumentCaptor.forClass(BomVersion.class);
        verify(bomVersionMapper).updateBomVersion(captor.capture());
        assertEquals("EFFECTIVE", captor.getValue().getStatus());
        assertEquals("tester", captor.getValue().getUpdateBy());
    }

    @Test
    void refusesToEditApprovedVersion() {
        BomVersion version = version("EFFECTIVE", "APPROVED", 0);
        when(bomVersionMapper.selectBomVersionById(1L)).thenReturn(version);

        ServiceException error = assertThrows(ServiceException.class, () -> service.updateBomVersion(version));

        assertEquals("已审核的BOM版本不能编辑，请先弃审", error.getMessage());
        verify(bomVersionMapper, never()).updateBomVersion(any());
    }

    @Test
    void refusesToDeleteNonDraftVersion() {
        when(bomVersionMapper.selectBomVersionById(1L)).thenReturn(version("EFFECTIVE", "PENDING", 0));

        ServiceException error = assertThrows(ServiceException.class,
                () -> service.deleteBomVersionByIds(new Long[] { 1L }));

        assertEquals("只有草稿状态的BOM版本可以删除", error.getMessage());
        verify(bomItemMapper, never()).deleteBomItemByVersionIds(any());
    }

    @Test
    void unapproveClearsDefaultFlag() {
        BomVersion version = version("EFFECTIVE", "APPROVED", 1);
        when(bomVersionMapper.selectBomVersionById(1L)).thenReturn(version);

        service.unapproveBomVersion(1L, "tester");

        ArgumentCaptor<BomVersion> captor = ArgumentCaptor.forClass(BomVersion.class);
        verify(bomVersionMapper).updateBomVersion(captor.capture());
        assertEquals("PENDING", captor.getValue().getApproveStatus());
        assertEquals(0, captor.getValue().getDefaultFlag());
    }

    private BomVersion version(String status, String approveStatus, int defaultFlag) {
        BomVersion version = new BomVersion();
        version.setId(1L);
        version.setBomMasterId(10L);
        version.setVersionCode("V1.0");
        version.setBaseQty(BigDecimal.ONE);
        version.setUsageType("GENERAL");
        version.setStatus(status);
        version.setApproveStatus(approveStatus);
        version.setDefaultFlag(defaultFlag);
        return version;
    }

    private BomItem validItem() {
        BomItem item = new BomItem();
        item.setComponentItemCode("COMPONENT-001");
        item.setComponentItemName("子件");
        item.setComponentQty(BigDecimal.ONE);
        item.setSupplyType("PICK");
        return item;
    }
}
