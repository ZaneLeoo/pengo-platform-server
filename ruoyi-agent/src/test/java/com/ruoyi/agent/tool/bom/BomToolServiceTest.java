package com.ruoyi.agent.tool.bom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ruoyi.agent.tool.shared.AgentToolNextAction;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import com.ruoyi.agent.tool.shared.AgentToolStatus;
import com.ruoyi.mes.base.domain.BomItem;
import com.ruoyi.mes.base.domain.BomMaster;
import com.ruoyi.mes.base.domain.BomVersion;
import com.ruoyi.mes.base.service.IBomItemService;
import com.ruoyi.mes.base.service.IBomMasterService;
import com.ruoyi.mes.base.service.IBomVersionService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BomToolServiceTest {
    private IBomMasterService masterService;
    private IBomVersionService versionService;
    private IBomItemService itemService;
    private BomToolService toolService;

    @BeforeEach
    void setUp() {
        masterService = mock(IBomMasterService.class);
        versionService = mock(IBomVersionService.class);
        itemService = mock(IBomItemService.class);
        toolService = new BomToolService(masterService, versionService, itemService);
    }

    @Test
    void shouldReturnBomAndAllVersions() {
        BomMaster master = master();
        when(masterService.selectBomMasterListForAgent("灯杆", null, "ACTIVE")).thenReturn(List.of(master));
        when(versionService.selectBomVersionList(any())).thenReturn(List.of(version(10L, "V1", 1),
                version(11L, "V2", 0)));
        BomToolQuery query = new BomToolQuery("灯杆", null, false);

        AgentToolResult<List<BomToolItem>> result = toolService.query(query);

        assertThat(result.getStatus()).isEqualTo(AgentToolStatus.SUCCESS);
        assertThat(result.getData()).singleElement().satisfies(item -> {
            assertThat(item.getCode()).isEqualTo("BOM-LAMPPOST");
            assertThat(item.getVersions()).extracting(BomVersionToolItem::getCode).containsExactly("V1", "V2");
        });
    }

    @Test
    void shouldUseDefaultVersionAndReturnComponents() {
        BomMaster master = master();
        when(masterService.selectBomMasterById(1L)).thenReturn(master);
        when(versionService.selectBomVersionList(any())).thenReturn(List.of(version(10L, "V1", 1),
                version(11L, "V2", 0)));
        when(itemService.selectBomItemList(any())).thenReturn(List.of(component()));
        BomStructureQuery query = new BomStructureQuery();
        query.setBomId(1L);

        AgentToolResult<BomStructureToolResult> result = toolService.structure(query);

        assertThat(result.getStatus()).isEqualTo(AgentToolStatus.SUCCESS);
        assertThat(result.getData().getVersion().getCode()).isEqualTo("V1");
        assertThat(result.getData().getComponents()).singleElement()
                .extracting(BomComponentToolItem::getMaterialCode).isEqualTo("PCB-CTRL");
    }

    @Test
    void shouldAskForVersionWhenNoDefaultCanBeSelected() {
        when(masterService.selectBomMasterById(1L)).thenReturn(master());
        when(versionService.selectBomVersionList(any())).thenReturn(List.of(version(10L, "V1", 0),
                version(11L, "V2", 0)));
        BomStructureQuery query = new BomStructureQuery();
        query.setBomId(1L);

        AgentToolResult<BomStructureToolResult> result = toolService.structure(query);

        assertThat(result.getStatus()).isEqualTo(AgentToolStatus.AMBIGUOUS);
        assertThat(result.getNextAction()).isEqualTo(AgentToolNextAction.SELECT_CANDIDATE);
        assertThat(result.getIssues()).singleElement().satisfies(issue -> {
            assertThat(issue.getField()).isEqualTo("versionId");
            assertThat(issue.getCandidates()).hasSize(2);
        });
    }

    private BomMaster master() {
        BomMaster master = new BomMaster();
        master.setId(1L);
        master.setBomCode("BOM-LAMPPOST");
        master.setParentItemCode("lamppost");
        master.setParentItemName("灯杆组件");
        master.setParentItemUnit("套");
        master.setBomType("MAKE");
        master.setStatus("ACTIVE");
        return master;
    }

    private BomVersion version(Long id, String code, int defaultFlag) {
        BomVersion version = new BomVersion();
        version.setId(id);
        version.setBomMasterId(1L);
        version.setVersionCode(code);
        version.setBaseQty(BigDecimal.ONE);
        version.setUsageType("GENERAL");
        version.setStatus("EFFECTIVE");
        version.setApproveStatus("APPROVED");
        version.setDefaultFlag(defaultFlag);
        return version;
    }

    private BomItem component() {
        BomItem item = new BomItem();
        item.setId(100L);
        item.setBomVersionId(10L);
        item.setLineNo(10);
        item.setComponentItemId(20L);
        item.setComponentItemCode("PCB-CTRL");
        item.setComponentItemName("PCB控制板");
        item.setComponentQty(BigDecimal.ONE);
        item.setComponentItemUnit("件");
        item.setSupplyType("PUSH");
        item.setIsVirtual(0);
        item.setMrpExpandFlag(1);
        return item;
    }
}
