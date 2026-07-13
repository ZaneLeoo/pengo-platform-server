package com.ruoyi.web.controller.agent;

import com.ruoyi.agent.api.AgentChatRequest;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.web.service.agent.AgentChatService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 基础 Dify 流式聊天入口。 */
@RestController
@RequestMapping("/agent/chat")
public class AgentChatController extends BaseController {
    private final AgentChatService chatService;

    public AgentChatController(AgentChatService chatService) {
        this.chatService = chatService;
    }

    /** 转发当前用户消息到 Dify，并透传安全的流式文本事件。 */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@Valid @RequestBody AgentChatRequest request) {
        return chatService.stream(request, getUserId());
    }
}
