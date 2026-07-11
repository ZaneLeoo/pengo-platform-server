package com.ruoyi.web.controller.agent;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.base.domain.MaterialCategory;
import com.ruoyi.mes.base.service.IMaterialCategoryService;
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

/** 面向 Dify Agent 的物料分类查询工具适配器。 */
@RestController
@RequestMapping("/agent/tools/material-categories")
public class AgentMaterialCategoryToolController
{
    private static final String TOOL_KEY_CONFIG = "agent.tool.key";
    private static final String TOOL_KEY_HEADER = "X-Agent-Tool-Key";

    private final IMaterialCategoryService categoryService;
    private final ISysConfigService configService;

    public AgentMaterialCategoryToolController(IMaterialCategoryService categoryService,
        ISysConfigService configService)
    {
        this.categoryService = categoryService;
        this.configService = configService;
    }

    /** 查询物料分类并返回适合大模型理解的精简结果。 */
    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> query(@RequestHeader(value = TOOL_KEY_HEADER, required = false) String toolKey,
        @RequestBody(required = false) CategoryQueryRequest request)
    {
        if (!isAuthorized(toolKey))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result(false, "工具鉴权失败", List.of()));
        }
        CategoryQueryRequest query = request == null ? new CategoryQueryRequest() : request;
        MaterialCategory condition = new MaterialCategory();
        String status = Boolean.FALSE.equals(query.includeDisabled()) ? "0" : null;
        List<Map<String, Object>> data = categoryService.selectCategoryListForAgent(query.keyword(), query.parentId(), status).stream()
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

    /** 转换为工具最小输出模型，避免暴露内部审计字段。 */
    private Map<String, Object> toToolData(MaterialCategory category)
    {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", category.getCategoryId());
        data.put("code", category.getCategoryCode());
        data.put("name", category.getCategoryName());
        data.put("parentId", category.getParentId());
        data.put("ancestors", category.getAncestors());
        data.put("level", category.getAncestors() == null ? 1 : category.getAncestors().split(",").length);
        data.put("status", category.getStatus());
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
    public record CategoryQueryRequest(String keyword, Long parentId, Boolean includeDisabled)
    {
        public CategoryQueryRequest()
        {
            this(null, null, false);
        }
    }
}
