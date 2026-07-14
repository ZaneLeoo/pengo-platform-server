package com.ruoyi.agent.tool.bom;

import com.ruoyi.agent.tool.shared.AgentToolCandidate;
import com.ruoyi.agent.tool.shared.AgentToolIssue;
import com.ruoyi.agent.tool.shared.AgentToolMeta;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import com.ruoyi.agent.tool.shared.AgentToolResults;
import com.ruoyi.mes.base.domain.BomItem;
import com.ruoyi.mes.base.domain.BomMaster;
import com.ruoyi.mes.base.domain.BomVersion;
import com.ruoyi.mes.base.service.IBomItemService;
import com.ruoyi.mes.base.service.IBomMasterService;
import com.ruoyi.mes.base.service.IBomVersionService;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

/** BOM 查询工具应用服务。 */
@Service
public class BomToolService {
    private static final int MAX_RESULT_SIZE = 20;
    private final IBomMasterService bomMasterService;
    private final IBomVersionService bomVersionService;
    private final IBomItemService bomItemService;

    public BomToolService(IBomMasterService bomMasterService, IBomVersionService bomVersionService,
            IBomItemService bomItemService) {
        this.bomMasterService = bomMasterService;
        this.bomVersionService = bomVersionService;
        this.bomItemService = bomItemService;
    }

    /** 查询 BOM 是否存在，并返回每个 BOM 的全部版本摘要。 */
    public AgentToolResult<List<BomToolItem>> query(BomToolQuery request) {
        BomToolQuery query = request == null ? new BomToolQuery() : request;
        String status = Boolean.TRUE.equals(query.getIncludeDisabled()) ? null : "ACTIVE";
        List<BomMaster> matched = bomMasterService.selectBomMasterListForAgent(query.getKeyword(),
                query.getBomType(), status);
        boolean truncated = matched.size() > MAX_RESULT_SIZE;
        List<BomToolItem> data = matched.stream().limit(MAX_RESULT_SIZE).map(this::toBomItem).toList();
        AgentToolMeta meta = AgentToolMeta.collection(matched.size(), truncated);
        if (data.isEmpty()) {
            return AgentToolResults.noResult(BomToolResultCode.BOM_NOT_FOUND,
                    "当前条件没有查询到BOM", data, meta);
        }
        return AgentToolResults.success(BomToolResultCode.BOM_QUERY_SUCCESS,
                "查询到 " + data.size() + " 个BOM", data, meta);
    }

    /** 定位一个 BOM 和版本，并返回该版本的直接子件。 */
    public AgentToolResult<BomStructureToolResult> structure(BomStructureQuery request) {
        BomStructureQuery query = request == null ? new BomStructureQuery() : request;
        List<BomMaster> matched = findMasters(query);
        if (matched.isEmpty()) {
            return AgentToolResults.noResult(BomToolResultCode.BOM_NOT_FOUND,
                    "没有找到指定的BOM", null, AgentToolMeta.collection(0, false));
        }
        if (matched.size() > 1) {
            return AgentToolResults.ambiguous(BomToolResultCode.BOM_AMBIGUOUS,
                    "查询条件匹配到多个BOM，请明确选择", List.of(bomIssue(matched)), null);
        }
        BomMaster master = matched.get(0);
        List<BomVersion> versions = versions(master.getId());
        VersionSelection selection = selectVersion(query, versions);
        if (selection.version == null) {
            return selection.ambiguous
                    ? AgentToolResults.ambiguous(BomToolResultCode.BOM_VERSION_AMBIGUOUS,
                            "该BOM存在多个版本，请明确选择版本", List.of(versionIssue(versions)), null)
                    : AgentToolResults.noResult(BomToolResultCode.BOM_VERSION_NOT_FOUND,
                            "没有找到指定的BOM版本", null, AgentToolMeta.collection(0, false));
        }
        List<BomComponentToolItem> components = components(selection.version.getId());
        BomStructureToolResult data = new BomStructureToolResult(master.getId(), master.getBomCode(),
                master.getParentItemCode(), master.getParentItemName(), master.getParentItemSpec(),
                master.getParentItemUnit(), master.getBomType(), master.getStatus(),
                toVersionItem(selection.version), components);
        return AgentToolResults.success(BomToolResultCode.BOM_STRUCTURE_SUCCESS,
                "查询到BOM版本 " + selection.version.getVersionCode() + " 的 " + components.size() + " 个子件",
                data, AgentToolMeta.collection(components.size(), false));
    }

    private List<BomMaster> findMasters(BomStructureQuery query) {
        if (query.getBomId() != null) {
            BomMaster master = bomMasterService.selectBomMasterById(query.getBomId());
            return master == null ? List.of() : List.of(master);
        }
        String keyword = hasText(query.getBomCode()) ? query.getBomCode() : query.getParentItemCode();
        if (!hasText(keyword)) {
            return List.of();
        }
        List<BomMaster> matched = bomMasterService.selectBomMasterListForAgent(keyword, null, null);
        if (hasText(query.getBomCode())) {
            return matched.stream().filter(item -> query.getBomCode().equalsIgnoreCase(item.getBomCode())).toList();
        }
        return matched.stream()
                .filter(item -> query.getParentItemCode().equalsIgnoreCase(item.getParentItemCode())).toList();
    }

    private VersionSelection selectVersion(BomStructureQuery query, List<BomVersion> versions) {
        if (query.getVersionId() != null) {
            return new VersionSelection(versions.stream()
                    .filter(item -> Objects.equals(query.getVersionId(), item.getId())).findFirst().orElse(null),
                    false);
        }
        if (hasText(query.getVersionCode())) {
            return new VersionSelection(versions.stream()
                    .filter(item -> query.getVersionCode().equalsIgnoreCase(item.getVersionCode()))
                    .findFirst().orElse(null), false);
        }
        BomVersion defaultVersion = versions.stream().filter(item -> Integer.valueOf(1).equals(item.getDefaultFlag()))
                .findFirst().orElse(null);
        if (defaultVersion != null) {
            return new VersionSelection(defaultVersion, false);
        }
        return versions.size() == 1
                ? new VersionSelection(versions.get(0), false)
                : new VersionSelection(null, !versions.isEmpty());
    }

    private BomToolItem toBomItem(BomMaster master) {
        return new BomToolItem(master.getId(), master.getBomCode(), master.getParentItemId(),
                master.getParentItemCode(), master.getParentItemName(), master.getParentItemSpec(),
                master.getParentItemUnit(), master.getBomType(), master.getStatus(),
                versions(master.getId()).stream().map(this::toVersionItem).toList());
    }

    private List<BomVersion> versions(Long masterId) {
        BomVersion condition = new BomVersion();
        condition.setBomMasterId(masterId);
        return bomVersionService.selectBomVersionList(condition);
    }

    private List<BomComponentToolItem> components(Long versionId) {
        BomItem condition = new BomItem();
        condition.setBomVersionId(versionId);
        return bomItemService.selectBomItemList(condition).stream().map(this::toComponent).toList();
    }

    private BomVersionToolItem toVersionItem(BomVersion version) {
        return new BomVersionToolItem(version.getId(), version.getVersionCode(), version.getVersionName(),
                version.getVersionDesc(), version.getBaseQty(), version.getUsageType(), version.getEffectiveDate(),
                version.getExpireDate(), version.getStatus(), version.getApproveStatus(),
                Integer.valueOf(1).equals(version.getDefaultFlag()));
    }

    private BomComponentToolItem toComponent(BomItem item) {
        return new BomComponentToolItem(item.getId(), item.getLineNo(), item.getComponentItemId(),
                item.getComponentItemCode(), item.getComponentItemName(), item.getComponentItemSpec(),
                item.getComponentItemUnit(), item.getComponentAttribute(), item.getComponentQty(),
                item.getFixedLossQty(), item.getChangeLossRate(), item.getSupplyType(),
                Integer.valueOf(1).equals(item.getIsVirtual()), Integer.valueOf(1).equals(item.getMrpExpandFlag()),
                Boolean.TRUE.equals(item.getHasChildBom()), item.getComponentBomVersionId());
    }

    private AgentToolIssue bomIssue(List<BomMaster> masters) {
        List<AgentToolCandidate> candidates = masters.stream()
                .map(item -> new AgentToolCandidate(String.valueOf(item.getId()), item.getBomCode(),
                        item.getParentItemCode() + " / " + item.getParentItemName()))
                .toList();
        return new AgentToolIssue("BOM_SELECTION_REQUIRED", "bomId", "请选择一个BOM", "BOM主键ID", candidates);
    }

    private AgentToolIssue versionIssue(List<BomVersion> versions) {
        List<AgentToolCandidate> candidates = versions.stream()
                .map(item -> new AgentToolCandidate(String.valueOf(item.getId()), item.getVersionCode(),
                        item.getStatus() + " / " + item.getApproveStatus()))
                .toList();
        return new AgentToolIssue("BOM_VERSION_SELECTION_REQUIRED", "versionId", "请选择一个BOM版本",
                "BOM版本主键ID", candidates);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static final class VersionSelection {
        private final BomVersion version;
        private final boolean ambiguous;

        private VersionSelection(BomVersion version, boolean ambiguous) {
            this.version = version;
            this.ambiguous = ambiguous;
        }
    }
}
