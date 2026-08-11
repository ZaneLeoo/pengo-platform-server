package com.ruoyi.web.service.mes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.web.domain.BomAiImportTrace;
import com.ruoyi.web.domain.enums.BomAiImportTraceStatus;
import com.ruoyi.web.mapper.mes.BomAiImportTraceMapper;
import com.ruoyi.mes.base.mapper.BomMasterMapper;
import com.ruoyi.mes.base.mapper.BomVersionMapper;
import com.ruoyi.mes.base.domain.BomMaster;
import com.ruoyi.mes.base.domain.BomVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

/** BOM AI 追溯状态机测试。 */
@ExtendWith(MockitoExtension.class)
class BomAiImportTraceServiceTest {
    @Mock
    private BomAiImportTraceMapper traceMapper;
    @Mock
    private BomAiImportFileStorage fileStorage;
    @Mock
    private BomMasterMapper bomMasterMapper;
    @Mock
    private BomVersionMapper bomVersionMapper;
    @InjectMocks
    private BomAiImportTraceService service;

    @Test
    void onlyOneRequestCanAcquireImportingStatus() {
        BomAiImportTrace importing = new BomAiImportTrace();
        importing.setId(7L);
        importing.setStatus(BomAiImportTraceStatus.IMPORTING.name());
        when(traceMapper.transitionStatus(eq(7L), eq("RECOGNIZED"), eq("IMPORTING"), anyString()))
                .thenReturn(1, 0);
        when(traceMapper.selectById(7L)).thenReturn(importing);

        assertEquals("IMPORTING", service.acquireForImport(7L, "admin").getStatus());
        assertThrows(ServiceException.class, () -> service.acquireForImport(7L, "admin"));
    }

    @Test
    void importedRetryIsReturnedIdempotently() {
        BomAiImportTrace imported = new BomAiImportTrace();
        imported.setId(8L);
        imported.setStatus(BomAiImportTraceStatus.IMPORTED.name());
        when(traceMapper.transitionStatus(eq(8L), eq("RECOGNIZED"), eq("IMPORTING"), anyString())).thenReturn(0);
        when(traceMapper.selectById(8L)).thenReturn(imported);

        assertEquals("IMPORTED", service.acquireForImport(8L, "admin").getStatus());
    }

    @Test
    void cancelledRetryIsIdempotent() {
        BomAiImportTrace cancelled = new BomAiImportTrace();
        cancelled.setId(9L);
        cancelled.setStatus(BomAiImportTraceStatus.CANCELLED.name());
        when(traceMapper.transitionStatus(eq(9L), eq("RECOGNIZED"), eq("CANCELLED"), anyString())).thenReturn(0);
        when(traceMapper.selectById(9L)).thenReturn(cancelled);

        assertDoesNotThrow(() -> service.cancel(9L, "admin"));
    }

    @Test
    void detailHydratesImportedBomSummary() {
        BomAiImportTrace trace = new BomAiImportTrace();
        trace.setId(10L);
        trace.setStatus(BomAiImportTraceStatus.IMPORTED.name());
        trace.setImportedBomVersionIds("[12]");
        BomVersion version = new BomVersion();
        version.setId(12L);
        version.setBomMasterId(17L);
        version.setVersionCode("V1.1");
        BomMaster master = new BomMaster();
        master.setId(17L);
        master.setBomCode("BOM-TL-1000");
        master.setParentItemCode("TL-1000");
        master.setParentItemName("智能护眼台灯");
        when(traceMapper.selectById(10L)).thenReturn(trace);
        when(bomVersionMapper.selectBomVersionById(12L)).thenReturn(version);
        when(bomMasterMapper.selectBomMasterById(17L)).thenReturn(master);

        BomAiImportTrace detail = service.selectById(10L);

        assertEquals(1, detail.getImportedBoms().size());
        assertEquals("BOM-TL-1000", detail.getImportedBoms().get(0).getBomCode());
        assertEquals("V1.1", detail.getImportedBoms().get(0).getVersionCode());
    }

    @Test
    void ignoresDuplicateWhenAllImportedVersionsWereDeleted() {
        BomAiImportTrace current = new BomAiImportTrace();
        current.setId(20L);
        current.setSourceFingerprint("same-file");
        BomAiImportTrace historical = new BomAiImportTrace();
        historical.setId(19L);
        historical.setImportedBomVersionIds("[12,13]");
        when(traceMapper.selectImportedByFingerprint("same-file", 20L)).thenReturn(List.of(historical));
        when(bomVersionMapper.selectBomVersionById(12L)).thenReturn(null);
        when(bomVersionMapper.selectBomVersionById(13L)).thenReturn(null);

        assertEquals(null, service.findImportedDuplicate(current));
    }

    @Test
    void keepsDuplicateWhenAnyImportedVersionStillExists() {
        BomAiImportTrace current = new BomAiImportTrace();
        current.setId(22L);
        current.setSourceFingerprint("same-file");
        BomAiImportTrace historical = new BomAiImportTrace();
        historical.setId(21L);
        historical.setImportedBomVersionIds("[14]");
        when(traceMapper.selectImportedByFingerprint("same-file", 22L)).thenReturn(List.of(historical));
        when(bomVersionMapper.selectBomVersionById(14L)).thenReturn(new BomVersion());

        assertEquals(historical, service.findImportedDuplicate(current));
    }
}
