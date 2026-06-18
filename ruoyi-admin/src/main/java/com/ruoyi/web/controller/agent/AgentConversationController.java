package com.ruoyi.web.controller.agent;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.agent.domain.AgentConversation;
import com.ruoyi.agent.domain.AgentMessage;
import com.ruoyi.agent.service.IAgentConversationService;
import com.ruoyi.agent.service.IAgentMessageService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * Agent会话管理
 *
 * @author Dylan
 */
@RestController
@RequestMapping("/agent/conversations")
public class AgentConversationController extends BaseController
{
    @Autowired
    private IAgentConversationService conversationService;

    @Autowired
    private IAgentMessageService messageService;

    /**
     * 获取当前用户的会话列表
     */
    @GetMapping
    public AjaxResult listConversations()
    {
        Long userId = getUserId();
        List<AgentConversation> list = conversationService.selectConversationList(userId);
        return success(list);
    }

    /**
     * 获取会话的消息列表
     */
    @GetMapping("/{id}/messages")
    public AjaxResult listMessages(@PathVariable Long id)
    {
        List<AgentMessage> messages = messageService.selectMessagesByConversationId(id);
        return success(messages);
    }

    /**
     * 创建新会话
     */
    @PostMapping
    public AjaxResult createConversation(@RequestBody AgentConversation conversation)
    {
        conversation.setUserId(getUserId());
        conversation.setCreateBy(getUsername());
        conversation.setStatus("0");
        conversation.setMessageCount(0);
        conversationService.insertConversation(conversation);
        return success(conversation);
    }

    /**
     * 删除会话（同时删除消息）
     */
    @DeleteMapping("/{id}")
    public AjaxResult deleteConversation(@PathVariable Long id)
    {
        messageService.deleteMessagesByConversationId(id);
        conversationService.deleteConversationById(id);
        return success();
    }
}
