package com.ruoyi.web.controller.agent;

import com.ruoyi.agent.api.v2.AgentV2RunRequest;
import com.ruoyi.agent.application.artifact.AgentV2ArtifactService;
import com.ruoyi.agent.application.run.AgentRunService;
import com.ruoyi.agent.domain.runtime.AgentRun;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.service.agent.AgentV2Orchestrator;
import com.ruoyi.web.service.agent.AgentV2SseService;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** Agent V2 运行创建、查询、事件订阅与取消入口。 */
@RestController
@RequestMapping("/agent/v2/runs")
public class AgentV2RunController extends BaseController
{
    private final AgentV2Orchestrator orchestrator;
    private final AgentV2SseService sseService;
    private final AgentRunService runService;
    private final AgentV2ArtifactService artifactService;

    public AgentV2RunController(AgentV2Orchestrator orchestrator, AgentV2SseService sseService,
        AgentRunService runService, AgentV2ArtifactService artifactService)
    {
        this.orchestrator = orchestrator;
        this.sseService = sseService;
        this.runService = runService;
        this.artifactService = artifactService;
    }

    /** 创建一次异步 Agent 运行。 */
    @PostMapping
    public AjaxResult create(@Valid @RequestBody AgentV2RunRequest request)
    {
        return success(orchestrator.create(request, getUserId(), getUsername()));
    }

    /** 查询运行状态与已生成制品，供页面刷新后恢复。 */
    @GetMapping("/{runId}")
    public AjaxResult detail(@PathVariable Long runId)
    {
        AgentRun run = runService.requireOwned(runId, getUserId());
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("run", run);
        detail.put("artifacts", artifactService.listViewsByRunId(runId));
        return success(detail);
    }

    /** 订阅标准事件；支持 afterSequence 或浏览器 Last-Event-ID 断线续传。 */
    @GetMapping(value = "/{runId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events(@PathVariable Long runId,
        @RequestParam(required = false) Long afterSequence,
        @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId)
    {
        return sseService.connect(runId, getUserId(), resolveCursor(afterSequence, lastEventId));
    }

    /** 停止仍在运行的 Agent 任务。 */
    @PostMapping("/{runId}/cancel")
    public AjaxResult cancel(@PathVariable Long runId)
    {
        orchestrator.cancel(runId, getUserId(), getUsername());
        return success();
    }

    private Long resolveCursor(Long afterSequence, String lastEventId)
    {
        long cursor = afterSequence == null ? 0L : Math.max(0L, afterSequence);
        if (lastEventId != null)
        {
            try
            {
                cursor = Math.max(cursor, Long.parseLong(lastEventId));
            }
            catch (NumberFormatException ignored)
            {
                // 无效 Last-Event-ID 按首次连接处理。
            }
        }
        return cursor;
    }
}
