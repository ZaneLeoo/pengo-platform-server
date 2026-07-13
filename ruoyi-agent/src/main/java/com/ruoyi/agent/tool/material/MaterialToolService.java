package com.ruoyi.agent.tool.material;

import com.ruoyi.agent.tool.shared.AgentToolMeta;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import com.ruoyi.agent.tool.shared.AgentToolResults;
import com.ruoyi.mes.base.domain.Material;
import com.ruoyi.mes.base.service.IMaterialService;
import java.util.List;
import org.springframework.stereotype.Service;

/** 物料查询工具应用服务。 */
@Service
public class MaterialToolService {
    private static final int MAX_RESULT_SIZE = 50;
    private final IMaterialService materialService;

    public MaterialToolService(IMaterialService materialService) {
        this.materialService = materialService;
    }

    /** 查询物料并转换成适合模型消费的最小字段集合。 */
    public AgentToolResult<List<MaterialToolItem>> query(MaterialToolQuery request) {
        MaterialToolQuery query = request == null ? new MaterialToolQuery() : request;
        String status = Boolean.FALSE.equals(query.includeDisabled()) ? "0" : null;
        List<Material> matched = materialService.selectMaterialListForAgent(query.keyword(), query.categoryId(),
                query.materialType(), status);
        boolean truncated = matched.size() > MAX_RESULT_SIZE;
        List<MaterialToolItem> data = matched.stream().limit(MAX_RESULT_SIZE).map(this::toItem).toList();
        AgentToolMeta meta = AgentToolMeta.collection(matched.size(), truncated);
        if (data.isEmpty()) {
            return AgentToolResults.noResult(MaterialToolResultCode.MATERIAL_NOT_FOUND,
                    "当前条件没有查询到物料", data, meta);
        }
        return AgentToolResults.success(MaterialToolResultCode.MATERIAL_QUERY_SUCCESS,
                "查询到 " + data.size() + " 条物料", data, meta);
    }

    /** 将 MES 物料实体转换为工具输出 DTO。 */
    private MaterialToolItem toItem(Material material) {
        return new MaterialToolItem(material.getMaterialId(), material.getMaterialCode(), material.getMaterialName(),
                material.getMaterialType(), material.getCategoryId(), material.getCategoryName(), material.getSpec(),
                material.getModel(), material.getUnit(), material.getMaterialVersion(), material.getStatus());
    }
}
