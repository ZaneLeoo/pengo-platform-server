package com.ruoyi.web.controller.agent;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mes.purchase.domain.dto.InventoryBalanceQuery;
import com.ruoyi.mes.purchase.domain.dto.InventoryBalanceToolItem;
import com.ruoyi.mes.purchase.domain.dto.InventoryToolPage;
import com.ruoyi.mes.purchase.domain.dto.InventoryTransactionQuery;
import com.ruoyi.mes.purchase.domain.dto.InventoryTransactionToolItem;
import com.ruoyi.mes.purchase.service.IInventoryAssistantQueryService;
import com.ruoyi.system.service.ISysConfigService;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 面向 Dify Agent 的库存查询工具适配器。 */
@RestController
@RequestMapping("/agent/tools/inventory")
public class AgentInventoryToolController
{
    private static final String TOOL_KEY_CONFIG = "agent.tool.key";
    private static final String TOOL_KEY_HEADER = "X-Agent-Tool-Key";

    private final IInventoryAssistantQueryService inventoryQueryService;
    private final ISysConfigService configService;

    public AgentInventoryToolController(IInventoryAssistantQueryService inventoryQueryService,
                                        ISysConfigService configService)
    {
        this.inventoryQueryService = inventoryQueryService;
        this.configService = configService;
    }

    /** 查询当前库存余额，用于回答可用库存和仓库库存问题。 */
    @PostMapping("/balances/query")
    public ResponseEntity<Map<String, Object>> queryBalances(
        @RequestHeader(value = TOOL_KEY_HEADER, required = false) String toolKey,
        @Valid @RequestBody(required = false) InventoryBalanceQuery request)
    {
        if (!isAuthorized(toolKey))
        {
            return unauthorized();
        }
        InventoryBalanceQuery query = request == null ? emptyBalanceQuery() : request;
        return ResponseEntity.ok(pageResult(inventoryQueryService.queryBalances(query)));
    }

    /** 查询库存变动流水，用于解释库存增减原因和追溯来源单据。 */
    @PostMapping("/transactions/query")
    public ResponseEntity<Map<String, Object>> queryTransactions(
        @RequestHeader(value = TOOL_KEY_HEADER, required = false) String toolKey,
        @Valid @RequestBody(required = false) InventoryTransactionQuery request)
    {
        if (!isAuthorized(toolKey))
        {
            return unauthorized();
        }
        InventoryTransactionQuery query = request == null ? emptyTransactionQuery() : request;
        return ResponseEntity.ok(pageResult(inventoryQueryService.queryTransactions(query)));
    }

    /** 校验 Dify 专用工具密钥。 */
    private boolean isAuthorized(String toolKey)
    {
        String configuredKey = configService.selectConfigByKey(TOOL_KEY_CONFIG);
        return StringUtils.isNotBlank(configuredKey) && configuredKey.equals(toolKey);
    }

    /** 返回统一的鉴权失败结果。 */
    private ResponseEntity<Map<String, Object>> unauthorized()
    {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", false);
        result.put("message", "工具鉴权失败");
        result.put("data", List.of());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }

    /** 将服务分页结果转换为工具统一响应。 */
    private <T> Map<String, Object> pageResult(InventoryToolPage<T> page)
    {
        Map<String, Object> pageInfo = new LinkedHashMap<>();
        pageInfo.put("pageNum", page.pageNum());
        pageInfo.put("pageSize", page.pageSize());
        pageInfo.put("total", page.total());
        pageInfo.put("hasMore", page.hasMore());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("message", "查询成功");
        result.put("data", page.data());
        result.put("page", pageInfo);
        result.put("meta", Map.of("truncated", page.hasMore()));
        return result;
    }

    /** 创建无筛选条件的余额查询。 */
    private InventoryBalanceQuery emptyBalanceQuery()
    {
        return new InventoryBalanceQuery(null, null, null, null, null, null);
    }

    /** 创建无筛选条件的流水查询。 */
    private InventoryTransactionQuery emptyTransactionQuery()
    {
        return new InventoryTransactionQuery(null, null, null, null, null, null, null, null, null);
    }
}
