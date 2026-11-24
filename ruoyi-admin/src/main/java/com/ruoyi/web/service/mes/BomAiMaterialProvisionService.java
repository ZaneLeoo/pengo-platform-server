package com.ruoyi.web.service.mes;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.base.domain.Material;
import com.ruoyi.mes.base.domain.MaterialCategory;
import com.ruoyi.mes.base.domain.Unit;
import com.ruoyi.mes.base.domain.UnitGroup;
import com.ruoyi.mes.base.domain.UnitGroupDetail;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BOM AI 导入场景下的未匹配物料一键建档与自动建档服务。
 */
@Service
public class BomAiMaterialProvisionService {

    public static final String DEFAULT_UNIT_GROUP_CODE = "AI_GENERAL";
    private static final String DEFAULT_UNIT_GROUP_NAME = "通用计数单位组";
    private static final String DEFAULT_UNIT = "个";
    private static final String ROLE_PARENT = "PARENT";
    private static final String ROLE_COMPONENT = "COMPONENT";
    private static final String AUTO_REMARK_PREFIX = "AI图纸导入自动建档";

    @Autowired
    private IMaterialService materialService;
    @Autowired
    private MaterialCategoryMapper materialCategoryMapper;
    @Autowired
    private UnitMapper unitMapper;
    @Autowired
    private UnitGroupMapper unitGroupMapper;
    @Autowired
    private UnitGroupDetailMapper unitGroupDetailMapper;

    /**
     * 批量一键建档未匹配物料（幂等：同编码已存在且启用则直接复用）。
     */
    @Transactional(rollbackFor = Exception.class)
    public BomAiQuickCreateMaterialsResult quickCreateMaterials(List<BomAiMaterialDraft> drafts, String operator) {
        if (drafts == null || drafts.isEmpty()) {
            throw new ServiceException("请至少提供一条待建档物料");
        }
        List<BomAiMaterialDraft> deduplicated = deduplicateDrafts(drafts);
        List<BomAiProvisionedMaterial> provisioned = new ArrayList<>();
        int createdCount = 0;
        int reusedCount = 0;
        for (BomAiMaterialDraft draft : deduplicated) {
            BomAiProvisionedMaterial item = provisionSingle(draft, operator);
            provisioned.add(item);
            if (item.isCreated()) {
                createdCount++;
            } else {
                reusedCount++;
            }
        }
        BomAiQuickCreateMaterialsResult result = new BomAiQuickCreateMaterialsResult();
        result.setCreatedCount(createdCount);
        result.setReusedCount(reusedCount);
        result.setMaterials(provisioned);
        return result;
    }

    /**
     * 在确认导入事务中为所有尚未建档的母件与子件自动建档。
     */
    @Transactional(rollbackFor = Exception.class)
    public List<BomAiProvisionedMaterial> ensureMaterialsForDocuments(List<BomAiDocument> documents,
            List<BomAiMaterialDraft> draftOverrides, String operator) {
        if (documents == null || documents.isEmpty()) {
            return List.of();
        }
        Map<String, BomAiMaterialDraft> overrideMap = indexOverrides(draftOverrides);
        List<BomAiMaterialDraft> missingDrafts = collectMissingDrafts(documents, overrideMap);
        List<BomAiProvisionedMaterial> created = new ArrayList<>();
        for (BomAiMaterialDraft draft : missingDrafts) {
            BomAiProvisionedMaterial provisioned = provisionSingle(draft, operator);
            if (provisioned.isCreated()) {
                created.add(provisioned);
            }
        }
        return created;
    }

    private List<BomAiMaterialDraft> collectMissingDrafts(List<BomAiDocument> documents,
            Map<String, BomAiMaterialDraft> overrideMap) {
        Set<String> allComponentCodes = collectComponentCodes(documents);
        Map<String, BomAiMaterialDraft> missingByCode = new LinkedHashMap<>();
        // 先收集所有页签的母件，确保跨页子装配件（既是前页子件又是后页母件）始终按半成品/自制建档，
        // 避免误建为外购原材料导致多层 BOM 无法展开。
        for (BomAiDocument document : documents) {
            if (document != null) {
                collectMissingParent(document.getHeader(), allComponentCodes, overrideMap, missingByCode);
            }
        }
        for (BomAiDocument document : documents) {
            if (document != null) {
                collectMissingItems(document.getItems(), overrideMap, missingByCode);
            }
        }
        return new ArrayList<>(missingByCode.values());
    }

    private Set<String> collectComponentCodes(List<BomAiDocument> documents) {
        Set<String> codes = new HashSet<>();
        for (BomAiDocument document : documents) {
            if (document == null || document.getItems() == null) {
                continue;
            }
            for (BomAiImportItem item : document.getItems()) {
                if (item == null) {
                    continue;
                }
                String code = firstNonBlank(item.getFinalItemCode(), item.getComponentCode());
                if (StringUtils.isNotBlank(code)) {
                    codes.add(code.trim());
                }
            }
        }
        return codes;
    }

    private void collectMissingParent(BomAiImportHeader header, Set<String> allComponentCodes,
            Map<String, BomAiMaterialDraft> overrideMap, Map<String, BomAiMaterialDraft> missingByCode) {
        if (header == null) {
            return;
        }
        String code = trimToNull(firstNonBlank(header.getFinalParentItemCode(), header.getParentItemCode()));
        if (code == null || missingByCode.containsKey(code)) {
            return;
        }
        if (isExistingMaterial(code)) {
            return;
        }
        BomAiMaterialDraft draft = new BomAiMaterialDraft();
        draft.setMaterialCode(code);
        draft.setMaterialName(trimToNull(firstNonBlank(header.getFinalParentItemName(), header.getParentItemName())));
        draft.setSpec(trimToNull(firstNonBlank(header.getFinalParentItemSpec(), header.getParentItemSpec())));
        draft.setModel(code);
        draft.setDrawingNo(trimToNull(header.getDrawingNo()));
        draft.setMaterialVersion(trimToNull(header.getRevision()));
        draft.setRole(ROLE_PARENT);
        draft.setMaterialType(allComponentCodes.contains(code) ? "SEMI_FINISHED" : "FINISHED");
        draft.setSourceType("MAKE");
        draft.setUnit(allComponentCodes.contains(code) ? "套" : "台");
        mergeOverride(draft, overrideMap.get(code));
        missingByCode.put(code, draft);
    }

    private void collectMissingItems(List<BomAiImportItem> items, Map<String, BomAiMaterialDraft> overrideMap,
            Map<String, BomAiMaterialDraft> missingByCode) {
        if (items == null) {
            return;
        }
        for (BomAiImportItem item : items) {
            if (item == null) {
                continue;
            }
            String code = trimToNull(firstNonBlank(item.getFinalItemCode(), item.getComponentCode()));
            if (code == null || missingByCode.containsKey(code) || isExistingMaterial(code)) {
                continue;
            }
            BomAiMaterialDraft draft = new BomAiMaterialDraft();
            draft.setMaterialCode(code);
            draft.setMaterialName(trimToNull(firstNonBlank(item.getFinalItemName(), item.getItemName())));
            draft.setSpec(trimToNull(firstNonBlank(item.getFinalSpec(), item.getSpec())));
            draft.setDrawingNo(trimToNull(item.getDrawingNo()));
            draft.setUnit(trimToNull(firstNonBlank(item.getFinalUnit(), item.getUnit())));
            draft.setRemark(trimToNull(item.getRemark()));
            draft.setRole(ROLE_COMPONENT);
            draft.setMaterialType("RAW");
            draft.setSourceType("PURCHASE");
            mergeOverride(draft, overrideMap.get(code));
            missingByCode.put(code, draft);
        }
    }

    private BomAiProvisionedMaterial provisionSingle(BomAiMaterialDraft draft, String operator) {
        String code = trimToNull(draft.getMaterialCode());
        String name = trimToNull(draft.getMaterialName());
        if (code == null) {
            throw new ServiceException("待建档物料编码不能为空");
        }
        if (code.length() > 64) {
            throw new ServiceException("物料编码长度不能超过64个字符：" + code);
        }
        if (name == null) {
            throw new ServiceException("物料名称不能为空（编码：" + code + "）");
        }
        Material existing = materialService.selectMaterialByCode(code);
        if (existing != null) {
            if (!"0".equals(existing.getStatus())) {
                throw new ServiceException("物料已存在但处于停用状态，请先启用：" + code);
            }
            return toProvisioned(existing, false);
        }
        Material material = buildMaterial(draft, code, name, operator);
        materialService.insertMaterial(material);
        Material saved = material.getMaterialId() != null ? material : materialService.selectMaterialByCode(code);
        return toProvisioned(saved != null ? saved : material, true);
    }

    private Material buildMaterial(BomAiMaterialDraft draft, String code, String name, String operator) {
        String materialType = resolveMaterialType(draft);
        String sourceType = resolveSourceType(draft, materialType);
        UnitBinding unitBinding = resolveUnitBinding(draft, operator);

        Material material = new Material();
        material.setMaterialCode(code);
        material.setMaterialName(truncate(name, 100));
        material.setMaterialType(materialType);
        material.setSourceType(sourceType);
        material.setCategoryId(resolveCategoryId(draft.getCategoryId(), materialType));
        material.setSpec(truncate(trimToNull(draft.getSpec()), 120));
        material.setModel(truncate(trimToNull(draft.getModel()), 120));
        material.setUnitGroupCode(unitBinding.unitGroupCode);
        material.setUnit(unitBinding.unitCode);
        material.setDrawingNo(truncate(trimToNull(draft.getDrawingNo()), 64));
        material.setMaterialVersion(truncate(trimToNull(draft.getMaterialVersion()), 32));
        material.setLotControlFlag("N");
        material.setShelfLifeControlFlag("N");
        material.setSnControlFlag("N");
        material.setInspectionFlag("N");
        material.setSafeStock(BigDecimal.ZERO);
        material.setStatus("0");
        material.setCreateBy(StringUtils.isBlank(operator) ? "admin" : operator);
        material.setRemark(buildRemark(draft.getRemark()));
        return material;
    }

    private UnitBinding resolveUnitBinding(BomAiMaterialDraft draft, String operator) {
        String requestedGroup = trimToNull(draft.getUnitGroupCode());
        String requestedUnit = truncate(trimToNull(draft.getUnit()), 64);
        if (requestedGroup != null) {
            UnitGroup group = unitGroupMapper.selectUnitGroupByCode(requestedGroup);
            if (group == null) {
                throw new ServiceException("计量单位组不存在：" + requestedGroup);
            }
            String resolvedUnit = requestedUnit != null ? requestedUnit : firstUnitInGroup(group.getId());
            if (resolvedUnit == null) {
                throw new ServiceException("计量单位组下未配置任何单位：" + requestedGroup);
            }
            return new UnitBinding(requestedGroup, resolvedUnit);
        }
        String unitCode = requestedUnit != null ? requestedUnit : DEFAULT_UNIT;
        UnitGroup defaultGroup = ensureDefaultUnitGroup(operator);
        ensureUnitExists(unitCode, operator);
        ensureUnitInGroup(defaultGroup.getId(), unitCode, operator);
        return new UnitBinding(defaultGroup.getGroupCode(), unitCode);
    }

    private UnitGroup ensureDefaultUnitGroup(String operator) {
        UnitGroup existing = unitGroupMapper.selectUnitGroupByCode(DEFAULT_UNIT_GROUP_CODE);
        if (existing != null) {
            return existing;
        }
        UnitGroup group = new UnitGroup();
        group.setGroupCode(DEFAULT_UNIT_GROUP_CODE);
        group.setGroupName(DEFAULT_UNIT_GROUP_NAME);
        group.setGroupType("GENERAL");
        group.setStatus("0");
        group.setCreateBy(StringUtils.isBlank(operator) ? "admin" : operator);
        group.setRemark("BOM AI 图纸自动建档默认单位组");
        unitGroupMapper.insertUnitGroup(group);
        return group.getId() != null ? group : unitGroupMapper.selectUnitGroupByCode(DEFAULT_UNIT_GROUP_CODE);
    }

    private void ensureUnitExists(String unitCode, String operator) {
        if (unitMapper.selectUnitByCode(unitCode) != null) {
            return;
        }
        Unit unit = new Unit();
        unit.setUnitCode(unitCode);
        unit.setUnitName(unitCode);
        unit.setStatus("0");
        unit.setCreateBy(StringUtils.isBlank(operator) ? "admin" : operator);
        unit.setRemark("BOM AI 图纸导入自动补充单位");
        unitMapper.insertUnit(unit);
    }

    private void ensureUnitInGroup(Long groupId, String unitCode, String operator) {
        if (groupId == null || unitGroupDetailMapper.selectByGroupAndUnit(groupId, unitCode) != null) {
            return;
        }
        UnitGroupDetail detail = new UnitGroupDetail();
        detail.setGroupId(groupId);
        detail.setUnitCode(unitCode);
        detail.setUnitName(unitCode);
        detail.setSortOrder(10);
        detail.setCreateBy(StringUtils.isBlank(operator) ? "admin" : operator);
        unitGroupDetailMapper.insertDetail(detail);
    }

    private String firstUnitInGroup(Long groupId) {
        List<UnitGroupDetail> details = unitGroupDetailMapper.selectByGroupId(groupId);
        return details != null && !details.isEmpty() ? details.get(0).getUnitCode() : null;
    }

    private Long resolveCategoryId(Long requestedCategoryId, String materialType) {
        if (requestedCategoryId != null) {
            return requestedCategoryId;
        }
        String categoryCode = switchCategoryCode(materialType);
        if (categoryCode != null) {
            MaterialCategory matched = materialCategoryMapper.selectCategoryByCode(categoryCode);
            if (matched != null) {
                return matched.getCategoryId();
            }
        }
        return null;
    }

    private String switchCategoryCode(String materialType) {
        if ("FINISHED".equals(materialType)) {
            return "FINISHED_PRODUCT";
        }
        if ("SEMI_FINISHED".equals(materialType)) {
            return "SEMI_FINISHED";
        }
        if ("PACKAGE".equals(materialType)) {
            return "PACKAGING";
        }
        return "STRUCTURAL_PART";
    }

    private String resolveMaterialType(BomAiMaterialDraft draft) {
        String requested = trimToNull(draft.getMaterialType());
        if (requested != null) {
            return requested;
        }
        return ROLE_PARENT.equalsIgnoreCase(draft.getRole()) ? "SEMI_FINISHED" : "RAW";
    }

    private String resolveSourceType(BomAiMaterialDraft draft, String materialType) {
        String requested = trimToNull(draft.getSourceType());
        if (requested != null) {
            return requested;
        }
        return ("FINISHED".equals(materialType) || "SEMI_FINISHED".equals(materialType)) ? "MAKE" : "PURCHASE";
    }

    private List<BomAiMaterialDraft> deduplicateDrafts(List<BomAiMaterialDraft> drafts) {
        Map<String, BomAiMaterialDraft> unique = new LinkedHashMap<>();
        for (BomAiMaterialDraft draft : drafts) {
            if (draft == null) {
                continue;
            }
            String code = trimToNull(draft.getMaterialCode());
            if (code == null) {
                throw new ServiceException("待建档物料编码不能为空");
            }
            unique.putIfAbsent(code, draft);
        }
        return new ArrayList<>(unique.values());
    }

    private Map<String, BomAiMaterialDraft> indexOverrides(List<BomAiMaterialDraft> overrides) {
        Map<String, BomAiMaterialDraft> map = new HashMap<>();
        if (overrides == null) {
            return map;
        }
        for (BomAiMaterialDraft draft : overrides) {
            if (draft != null && StringUtils.isNotBlank(draft.getMaterialCode())) {
                map.put(draft.getMaterialCode().trim(), draft);
            }
        }
        return map;
    }

    private void mergeOverride(BomAiMaterialDraft target, BomAiMaterialDraft override) {
        if (override == null) {
            return;
        }
        if (StringUtils.isNotBlank(override.getMaterialName())) {
            target.setMaterialName(override.getMaterialName().trim());
        }
        if (StringUtils.isNotBlank(override.getMaterialType())) {
            target.setMaterialType(override.getMaterialType().trim());
        }
        if (StringUtils.isNotBlank(override.getSourceType())) {
            target.setSourceType(override.getSourceType().trim());
        }
        if (override.getCategoryId() != null) {
            target.setCategoryId(override.getCategoryId());
        }
        if (StringUtils.isNotBlank(override.getSpec())) {
            target.setSpec(override.getSpec().trim());
        }
        if (StringUtils.isNotBlank(override.getModel())) {
            target.setModel(override.getModel().trim());
        }
        if (StringUtils.isNotBlank(override.getUnit())) {
            target.setUnit(override.getUnit().trim());
        }
        if (StringUtils.isNotBlank(override.getUnitGroupCode())) {
            target.setUnitGroupCode(override.getUnitGroupCode().trim());
        }
        if (StringUtils.isNotBlank(override.getDrawingNo())) {
            target.setDrawingNo(override.getDrawingNo().trim());
        }
        if (StringUtils.isNotBlank(override.getMaterialVersion())) {
            target.setMaterialVersion(override.getMaterialVersion().trim());
        }
        if (StringUtils.isNotBlank(override.getRemark())) {
            target.setRemark(override.getRemark().trim());
        }
    }

    private boolean isExistingMaterial(String code) {
        Material existing = materialService.selectMaterialByCode(code);
        return existing != null && "0".equals(existing.getStatus());
    }

    private BomAiProvisionedMaterial toProvisioned(Material material, boolean created) {
        BomAiProvisionedMaterial result = new BomAiProvisionedMaterial();
        result.setMaterialId(material.getMaterialId());
        result.setMaterialCode(material.getMaterialCode());
        result.setMaterialName(material.getMaterialName());
        result.setMaterialType(material.getMaterialType());
        result.setSourceType(material.getSourceType());
        result.setCategoryId(material.getCategoryId());
        result.setSpec(material.getSpec());
        result.setUnit(material.getUnit());
        result.setUnitGroupCode(material.getUnitGroupCode());
        result.setCreated(created);
        return result;
    }

    private String buildRemark(String rawRemark) {
        String cleaned = trimToNull(rawRemark);
        if (cleaned == null) {
            return AUTO_REMARK_PREFIX;
        }
        if (cleaned.contains(AUTO_REMARK_PREFIX)) {
            return truncate(cleaned, 500);
        }
        return truncate(cleaned + "（" + AUTO_REMARK_PREFIX + "）", 500);
    }

    private String firstNonBlank(String preferred, String fallback) {
        return StringUtils.isNotBlank(preferred) ? preferred : fallback;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private static class UnitBinding {
        private final String unitGroupCode;
        private final String unitCode;

        private UnitBinding(String unitGroupCode, String unitCode) {
            this.unitGroupCode = unitGroupCode;
            this.unitCode = unitCode;
        }
    }
}
