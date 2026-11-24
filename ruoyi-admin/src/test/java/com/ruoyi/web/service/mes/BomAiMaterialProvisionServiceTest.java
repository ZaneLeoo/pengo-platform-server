package com.ruoyi.web.service.mes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mes.base.domain.Material;
import com.ruoyi.mes.base.domain.MaterialCategory;
import com.ruoyi.mes.base.domain.UnitGroup;
import com.ruoyi.mes.base.mapper.MaterialCategoryMapper;
import com.ruoyi.mes.base.mapper.UnitGroupDetailMapper;
import com.ruoyi.mes.base.mapper.UnitGroupMapper;
import com.ruoyi.mes.base.mapper.UnitMapper;
import com.ruoyi.mes.base.service.IMaterialService;
import com.ruoyi.web.domain.dto.BomAiDocument;
import com.ruoyi.web.domain.dto.BomAiImportHeader;
import com.ruoyi.web.domain.dto.BomAiImportItem;
import com.ruoyi.web.domain.dto.BomAiMaterialDraft;
import com.ruoyi.web.domain.dto.BomAiProvisionedMaterial;
import com.ruoyi.web.domain.dto.BomAiQuickCreateMaterialsResult;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests quick-create and auto-create material provisioning for BOM AI import.
 */
@ExtendWith(MockitoExtension.class)
class BomAiMaterialProvisionServiceTest {

    @Mock
    private IMaterialService materialService;

    @Mock
    private MaterialCategoryMapper materialCategoryMapper;

    @Mock
    private UnitMapper unitMapper;

    @Mock
    private UnitGroupMapper unitGroupMapper;

    @Mock
    private UnitGroupDetailMapper unitGroupDetailMapper;

    @InjectMocks
    private BomAiMaterialProvisionService service;

    @Test
    void quickCreateMaterialsCreatesNewMaterialWithDefaultUnitGroupAndDrawingUnit() {
        UnitGroup defaultGroup = new UnitGroup();
        defaultGroup.setId(10L);
        defaultGroup.setGroupCode(BomAiMaterialProvisionService.DEFAULT_UNIT_GROUP_CODE);

        MaterialCategory structuralCategory = new MaterialCategory();
        structuralCategory.setCategoryId(5L);
        structuralCategory.setCategoryCode("STRUCTURAL_PART");

        when(materialService.selectMaterialByCode("MAT-NEW-001")).thenReturn(null);
        when(unitGroupMapper.selectUnitGroupByCode(BomAiMaterialProvisionService.DEFAULT_UNIT_GROUP_CODE))
                .thenReturn(defaultGroup);
        when(unitMapper.selectUnitByCode("片")).thenReturn(null);
        when(unitGroupDetailMapper.selectByGroupAndUnit(10L, "片")).thenReturn(null);
        when(materialCategoryMapper.selectCategoryByCode("STRUCTURAL_PART")).thenReturn(structuralCategory);
        doAnswer(invocation -> {
            Material saved = invocation.getArgument(0);
            saved.setMaterialId(101L);
            return 1;
        }).when(materialService).insertMaterial(any(Material.class));

        BomAiMaterialDraft draft = new BomAiMaterialDraft();
        draft.setMaterialCode("MAT-NEW-001");
        draft.setMaterialName("钣金底板");
        draft.setSpec("2.0mm SPCC");
        draft.setUnit("片");
        draft.setDrawingNo("DWG-001");

        BomAiQuickCreateMaterialsResult result = service.quickCreateMaterials(List.of(draft), "tester");

        assertEquals(1, result.getCreatedCount());
        assertEquals(0, result.getReusedCount());
        assertEquals(1, result.getMaterials().size());
        BomAiProvisionedMaterial provisioned = result.getMaterials().get(0);
        assertTrue(provisioned.isCreated());
        assertEquals(101L, provisioned.getMaterialId());
        assertEquals("MAT-NEW-001", provisioned.getMaterialCode());
        assertEquals(BomAiMaterialProvisionService.DEFAULT_UNIT_GROUP_CODE, provisioned.getUnitGroupCode());
        assertEquals("片", provisioned.getUnit());

        ArgumentCaptor<Material> captor = ArgumentCaptor.forClass(Material.class);
        verify(materialService).insertMaterial(captor.capture());
        Material inserted = captor.getValue();
        assertEquals(BomAiMaterialProvisionService.DEFAULT_UNIT_GROUP_CODE, inserted.getUnitGroupCode());
        assertEquals("片", inserted.getUnit());
        assertEquals("RAW", inserted.getMaterialType());
        assertEquals("PURCHASE", inserted.getSourceType());
        assertEquals(5L, inserted.getCategoryId());
        assertEquals("0", inserted.getStatus());
        assertEquals("tester", inserted.getCreateBy());
    }

    @Test
    void quickCreateMaterialsIdempotentlyReusesEnabledMaterialAndDeduplicatesBatch() {
        Material existing = new Material();
        existing.setMaterialId(202L);
        existing.setMaterialCode("MAT-EXIST-001");
        existing.setMaterialName("已存在物料");
        existing.setMaterialType("RAW");
        existing.setSourceType("PURCHASE");
        existing.setUnit("个");
        existing.setUnitGroupCode(BomAiMaterialProvisionService.DEFAULT_UNIT_GROUP_CODE);
        existing.setStatus("0");

        when(materialService.selectMaterialByCode("MAT-EXIST-001")).thenReturn(existing);

        BomAiMaterialDraft first = new BomAiMaterialDraft();
        first.setMaterialCode("MAT-EXIST-001");
        first.setMaterialName("已存在物料");

        BomAiMaterialDraft duplicate = new BomAiMaterialDraft();
        duplicate.setMaterialCode(" MAT-EXIST-001 ");
        duplicate.setMaterialName("重复提交物料");

        BomAiQuickCreateMaterialsResult result = service.quickCreateMaterials(List.of(first, duplicate), "tester");

        assertEquals(0, result.getCreatedCount());
        assertEquals(1, result.getReusedCount());
        assertEquals(1, result.getMaterials().size());
        BomAiProvisionedMaterial item = result.getMaterials().get(0);
        assertFalse(item.isCreated());
        assertEquals(202L, item.getMaterialId());
        assertEquals("MAT-EXIST-001", item.getMaterialCode());

        verify(materialService, times(1)).selectMaterialByCode("MAT-EXIST-001");
        verify(materialService, never()).insertMaterial(any());
    }

    @Test
    void quickCreateMaterialsRejectsExistingDisabledMaterial() {
        Material disabled = new Material();
        disabled.setMaterialId(303L);
        disabled.setMaterialCode("MAT-DISABLED-001");
        disabled.setMaterialName("停用物料");
        disabled.setStatus("1");

        when(materialService.selectMaterialByCode("MAT-DISABLED-001")).thenReturn(disabled);

        BomAiMaterialDraft draft = new BomAiMaterialDraft();
        draft.setMaterialCode("MAT-DISABLED-001");
        draft.setMaterialName("停用物料");

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.quickCreateMaterials(List.of(draft), "tester"));

        assertTrue(exception.getMessage().contains("MAT-DISABLED-001"));
        verify(materialService, never()).insertMaterial(any());
    }

    @Test
    void ensureMaterialsForDocumentsInfersTypesAndRespectsDraftOverrides() {
        UnitGroup defaultGroup = new UnitGroup();
        defaultGroup.setId(10L);
        defaultGroup.setGroupCode(BomAiMaterialProvisionService.DEFAULT_UNIT_GROUP_CODE);

        when(unitGroupMapper.selectUnitGroupByCode(BomAiMaterialProvisionService.DEFAULT_UNIT_GROUP_CODE))
                .thenReturn(defaultGroup);
        when(materialService.selectMaterialByCode(any())).thenReturn(null);
        doAnswer(invocation -> {
            Material saved = invocation.getArgument(0);
            saved.setMaterialId(500L);
            return 1;
        }).when(materialService).insertMaterial(any(Material.class));

        BomAiImportHeader subHeader = new BomAiImportHeader();
        subHeader.setParentItemCode("SEMI-001");
        subHeader.setParentItemName("控制盒组件");
        BomAiImportItem leafItem = new BomAiImportItem();
        leafItem.setComponentCode("LEAF-001");
        leafItem.setItemName("铝壳");
        leafItem.setUnit("片");
        BomAiImportItem overriddenItem = new BomAiImportItem();
        overriddenItem.setComponentCode("LEAF-002");
        overriddenItem.setItemName("包装纸箱");
        overriddenItem.setUnit("个");
        BomAiDocument subDoc = new BomAiDocument();
        subDoc.setPageNo(1);
        subDoc.setHeader(subHeader);
        subDoc.setItems(List.of(leafItem, overriddenItem));

        BomAiImportHeader rootHeader = new BomAiImportHeader();
        rootHeader.setParentItemCode("ROOT-001");
        rootHeader.setParentItemName("整机设备");
        BomAiImportItem semiRefItem = new BomAiImportItem();
        semiRefItem.setComponentCode("SEMI-001");
        semiRefItem.setItemName("控制盒组件");
        semiRefItem.setUnit("套");
        BomAiDocument rootDoc = new BomAiDocument();
        rootDoc.setPageNo(2);
        rootDoc.setHeader(rootHeader);
        rootDoc.setItems(List.of(semiRefItem));

        BomAiMaterialDraft override = new BomAiMaterialDraft();
        override.setMaterialCode("LEAF-002");
        override.setMaterialName("定制彩盒");
        override.setMaterialType("PACKAGE");
        override.setSourceType("OUTSOURCE");
        override.setUnit("盒");

        List<BomAiProvisionedMaterial> created = service.ensureMaterialsForDocuments(
                List.of(subDoc, rootDoc), List.of(override), "tester");

        assertEquals(4, created.size());
        Map<String, BomAiProvisionedMaterial> byCode = created.stream()
                .collect(Collectors.toMap(BomAiProvisionedMaterial::getMaterialCode, Function.identity()));

        BomAiProvisionedMaterial root = byCode.get("ROOT-001");
        assertEquals("FINISHED", root.getMaterialType());
        assertEquals("MAKE", root.getSourceType());
        assertEquals("台", root.getUnit());

        BomAiProvisionedMaterial semi = byCode.get("SEMI-001");
        assertEquals("SEMI_FINISHED", semi.getMaterialType());
        assertEquals("MAKE", semi.getSourceType());
        assertEquals("套", semi.getUnit());

        BomAiProvisionedMaterial leaf = byCode.get("LEAF-001");
        assertEquals("RAW", leaf.getMaterialType());
        assertEquals("PURCHASE", leaf.getSourceType());
        assertEquals("片", leaf.getUnit());

        BomAiProvisionedMaterial overridden = byCode.get("LEAF-002");
        assertEquals("定制彩盒", overridden.getMaterialName());
        assertEquals("PACKAGE", overridden.getMaterialType());
        assertEquals("OUTSOURCE", overridden.getSourceType());
        assertEquals("盒", overridden.getUnit());
    }
}
