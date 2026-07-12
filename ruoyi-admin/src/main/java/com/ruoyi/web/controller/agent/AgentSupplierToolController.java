package com.ruoyi.web.controller.agent;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.base.domain.Supplier;
import com.ruoyi.mes.base.service.ISupplierService;
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

/** 面向 Dify Agent 的供应商查询工具适配器。 */
@RestController
@RequestMapping("/agent/tools/suppliers")
public class AgentSupplierToolController
{
    private static final String TOOL_KEY_CONFIG = "agent.tool.key";
    private static final String TOOL_KEY_HEADER = "X-Agent-Tool-Key";
    private static final String ACTIVE_STATUS = "NORMAL";
    private static final int MAX_RESULT_SIZE = 50;

    private final ISupplierService supplierService;
    private final ISysConfigService configService;

    public AgentSupplierToolController(ISupplierService supplierService, ISysConfigService configService)
    {
        this.supplierService = supplierService;
        this.configService = configService;
    }

    /** 查询供应商，以便回答供应商信息或辅助准备采购订单。 */
    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> query(
        @RequestHeader(value = TOOL_KEY_HEADER, required = false) String toolKey,
        @RequestBody(required = false) SupplierQueryRequest request)
    {
        if (!isAuthorized(toolKey))
        {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(unauthorized());
        }
        SupplierQueryRequest query = request == null ? new SupplierQueryRequest() : request;
        String status = Boolean.FALSE.equals(query.includeDisabled()) ? ACTIVE_STATUS : null;
        List<Supplier> matched = supplierService.selectListForAgent(query.keyword(), status);
        boolean truncated = matched.size() > MAX_RESULT_SIZE;
        List<Map<String, Object>> data = matched.stream().limit(MAX_RESULT_SIZE).map(this::toToolData)
            .collect(Collectors.toList());
        return ResponseEntity.ok(result(data, matched.size(), truncated));
    }

    /** 校验 Dify 专用工具密钥。 */
    private boolean isAuthorized(String toolKey)
    {
        String configuredKey = configService.selectConfigByKey(TOOL_KEY_CONFIG);
        return StringUtils.isNotBlank(configuredKey) && configuredKey.equals(toolKey);
    }

    /** 供应商工具只返回采购与识别所需字段，不暴露联系方式和地址。 */
    private Map<String, Object> toToolData(Supplier supplier)
    {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", supplier.getId());
        data.put("code", supplier.getSupplierCode());
        data.put("name", supplier.getSupplierName());
        data.put("currency", supplier.getCurrency());
        data.put("taxRate", supplier.getTaxRate());
        data.put("status", supplier.getStatus());
        return data;
    }

    /** 返回统一的只读工具响应。 */
    private Map<String, Object> result(List<Map<String, Object>> data, int matchedSize, boolean truncated)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", data);
        result.put("total", truncated ? MAX_RESULT_SIZE + 1 : matchedSize);
        result.put("meta", Map.of("truncated", truncated));
        return result;
    }

    /** 返回统一的鉴权失败结构。 */
    private Map<String, Object> unauthorized()
    {
        return Map.of("success", false, "message", "工具鉴权失败", "data", List.of());
    }

    /** Dify 工具请求参数。默认仅查询启用供应商。 */
    public record SupplierQueryRequest(String keyword, Boolean includeDisabled)
    {
        public SupplierQueryRequest()
        {
            this(null, false);
        }
    }
}
