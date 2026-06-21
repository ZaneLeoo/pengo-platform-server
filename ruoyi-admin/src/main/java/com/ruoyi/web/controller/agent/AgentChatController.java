package com.ruoyi.web.controller.agent;

import com.ruoyi.agent.api.AgentChatRequest;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.service.agent.AgentChatService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** Agent 流式聊天入口。 */
@RestController
@RequestMapping("/agent/chat")
public class AgentChatController extends BaseController
{
    private final AgentChatService chatService;
    public AgentChatController(AgentChatService service) { this.chatService = service; }

    /** 发起一次 Dify Chatflow 流式聊天。 */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@Valid @RequestBody AgentChatRequest request)
    {
        return chatService.stream(request, getUserId(), getUsername());
    }

    /** 停止当前会话最近一次生成。 */
    @PostMapping("/stop")
    public AjaxResult stop(@RequestBody AgentChatRequest request)
    {
        chatService.stop(request.getConversationId(), getUserId(), getUsername());
        return success();
    }
}
