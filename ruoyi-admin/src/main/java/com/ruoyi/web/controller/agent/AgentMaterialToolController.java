package com.ruoyi.web.controller.agent;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.base.domain.Material;
import com.ruoyi.mes.base.service.IMaterialService;
import com.ruoyi.system.service.ISysConfigService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 面向 Dify Agent 的物料查询工具适配器。 */
@RestController
@RequestMapping("/agent/tools/materials")
public class AgentMaterialToolController
{
    private static final String TOOL_KEY_CONFIG = "agent.tool.key";
    private static final String TOOL_KEY_HEADER = "X-Agent-Tool-Key";

    private final IMaterialService materialService;
    private final ISysConfigService configService;

    public AgentMaterialToolController(IMaterialService materialService, ISysConfigService configService)
    {
        this.materialService = materialService;
        this.configService = configService;
    }

    /** 查询物料并返回适合大模型理解的精简结果。 */
    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> query(@RequestHeader(value = TOOL_KEY_HEADER, required = false) String toolKey,
        @RequestBody(required = false) MaterialQueryRequest request)
    {
        if (!isAuthorized(toolKey))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result(false, "工具鉴权失败", List.of()));
        }
        MaterialQueryRequest query = request == null ? new MaterialQueryRequest() : request;
        String status = Boolean.FALSE.equals(query.includeDisabled()) ? "0" : null;
        List<Map<String, Object>> data = materialService.selectMaterialListForAgent(
                query.keyword(), query.categoryId(), query.materialType(), status).stream()
            .map(this::toToolData)
            .collect(Collectors.toList());
        return ResponseEntity.ok(result(true, "查询成功", data));
    }

    /** 校验 Dify 专用工具密钥。 */
    private boolean isAuthorized(String toolKey)
    {
        String configuredKey = configService.selectConfigByKey(TOOL_KEY_CONFIG);
        return StringUtils.isNotBlank(configuredKey) && configuredKey.equals(toolKey);
    }

    /** 转换为工具最小输出模型。 */
    private Map<String, Object> toToolData(Material material)
    {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", material.getMaterialId());
        data.put("code", material.getMaterialCode());
        data.put("name", material.getMaterialName());
        data.put("type", material.getMaterialType());
        data.put("categoryId", material.getCategoryId());
        data.put("categoryName", material.getCategoryName());
        data.put("spec", material.getSpec());
        data.put("model", material.getModel());
        data.put("unit", material.getUnit());
        data.put("version", material.getMaterialVersion());
        data.put("status", material.getStatus());
        return data;
    }

    /** 生成统一工具响应。 */
    private Map<String, Object> result(boolean success, String message, List<Map<String, Object>> data)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", success);
        result.put("message", message);
        result.put("data", data);
        result.put("total", data.size());
        return result;
    }

    /** Dify 工具请求参数。 */
    public record MaterialQueryRequest(String keyword, Long categoryId, String materialType, Boolean includeDisabled)
    {
        public MaterialQueryRequest()
        {
            this(null, null, null, false);
        }
    }
}
