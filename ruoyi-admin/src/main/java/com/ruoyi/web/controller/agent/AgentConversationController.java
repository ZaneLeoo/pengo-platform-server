package com.ruoyi.web.controller.agent;

import com.ruoyi.agent.application.AgentConversationService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 当前用户的 Agent 本地会话接口。 */
@RestController
@RequestMapping("/agent/conversations")
public class AgentConversationController extends BaseController {
    private final AgentConversationService conversationService;
    public AgentConversationController(AgentConversationService service) {
        this.conversationService = service;
    }

    /** 查询当前用户会话。 */
    @GetMapping
    public AjaxResult list() {
        return success(conversationService.list(getUserId()));
    }

    /** 创建空会话。 */
    @PostMapping
    public AjaxResult create(@RequestBody(required = false) Map<String, String> body) {
        String title = body == null ? null : body.get("title");
        return success(conversationService.create(getUserId(), title, getUsername()));
    }

    /** 查询会话消息。 */
    @GetMapping("/{id}/messages")
    public AjaxResult messages(@PathVariable Long id) {
        return success(conversationService.messages(id, getUserId()));
    }

    /** 修改会话标题。 */
    @PostMapping("/{id}/rename")
    public AjaxResult rename(@PathVariable Long id, @RequestBody Map<String, String> body) {
        conversationService.rename(id, getUserId(), body.get("title"), getUsername());
        return success();
    }

    /** 删除会话及本地消息。 */
    @DeleteMapping("/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        conversationService.delete(id, getUserId());
        return success();
    }
}
