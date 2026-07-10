package com.ruoyi.web.controller.agent;

import com.ruoyi.agent.api.v2.AgentToolInvokeRequest;
import com.ruoyi.agent.application.tool.AgentToolExecutionService;
import com.ruoyi.agent.application.tool.AgentToolResult;
import com.ruoyi.agent.domain.enums.AgentToolCode;
import com.ruoyi.web.service.agent.tool.AgentToolGatewayAuthenticator;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dify Supervisor 专用工具网关，不向浏览器前端开放。 */
@RestController
@RequestMapping("/internal/agent-tools")
public class AgentToolGatewayController
{
    public static final String GATEWAY_HEADER = "X-Agent-Tool-Key";

    private final AgentToolGatewayAuthenticator authenticator;
    private final AgentToolExecutionService executionService;

    public AgentToolGatewayController(AgentToolGatewayAuthenticator authenticator,
        AgentToolExecutionService executionService)
    {
        this.authenticator = authenticator;
        this.executionService = executionService;
    }

    @PostMapping("/query-business-data")
    public AgentToolResult queryBusinessData(@RequestHeader(GATEWAY_HEADER) String gatewayKey,
        @Valid @RequestBody AgentToolInvokeRequest request)
    {
        return invoke(gatewayKey, AgentToolCode.QUERY_BUSINESS_DATA, request);
    }

    @PostMapping("/analyze-dataset")
    public AgentToolResult analyzeDataset(@RequestHeader(GATEWAY_HEADER) String gatewayKey,
        @Valid @RequestBody AgentToolInvokeRequest request)
    {
        return invoke(gatewayKey, AgentToolCode.ANALYZE_DATASET, request);
    }

    @PostMapping("/render-chart")
    public AgentToolResult renderChart(@RequestHeader(GATEWAY_HEADER) String gatewayKey,
        @Valid @RequestBody AgentToolInvokeRequest request)
    {
        return invoke(gatewayKey, AgentToolCode.RENDER_CHART, request);
    }

    @PostMapping("/request-clarification")
    public AgentToolResult requestClarification(@RequestHeader(GATEWAY_HEADER) String gatewayKey,
        @Valid @RequestBody AgentToolInvokeRequest request)
    {
        return invoke(gatewayKey, AgentToolCode.REQUEST_CLARIFICATION, request);
    }

    private AgentToolResult invoke(String gatewayKey, AgentToolCode toolCode, AgentToolInvokeRequest request)
    {
        authenticator.requireValid(gatewayKey);
        return executionService.execute(request.getRunId(), request.getRunToken(), toolCode.getCode(),
            request.getInput(), request.getIdempotencyKey());
    }
}
