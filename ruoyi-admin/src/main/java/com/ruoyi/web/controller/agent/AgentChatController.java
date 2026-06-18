package com.ruoyi.web.controller.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.agent.domain.AgentConversation;
import com.ruoyi.agent.domain.AgentMessage;
import com.ruoyi.agent.dto.ChatRequest;
import com.ruoyi.agent.dto.DifyChatRequest;
import com.ruoyi.agent.dto.DifyEvent;
import com.ruoyi.agent.service.IAgentConversationService;
import com.ruoyi.agent.service.IAgentMessageService;
import com.ruoyi.agent.service.IDifyApiService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.service.ISysConfigService;

/**
 * Agent聊天 SSE流式端点
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/agent/chat")
public class AgentChatController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(AgentChatController.class);

    @Autowired
    private IAgentConversationService conversationService;

    @Autowired
    private IAgentMessageService messageService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private IDifyApiService difyApiService;

    @Autowired
    @Qualifier("threadPoolTaskExecutor")
    private ThreadPoolTaskExecutor executor;

    /** 追踪活跃的SSE流，key=conversationId，value=取消标志 */
    private final ConcurrentHashMap<Long, AtomicBoolean> activeStreams = new ConcurrentHashMap<>();

    /**
     * SSE流式聊天
     */
    @PostMapping("/stream")
    public SseEmitter streamChat(@RequestBody ChatRequest request)
    {
        Long userId = getUserId();
        String username = getUsername();

        // 1. 获取或创建会话
        AgentConversation conversation = conversationService.getOrCreateConversation(
                request.getConversationId(), request.getQuery(), username, userId);

        Long convId = conversation.getId();

        // 2. 读取Dify配置（trim 防止 sys_config 中有不可见字符）
        String apiBaseUrl = configService.selectConfigByKey("agent.dify.api_base_url").trim();
        String apiKey = configService.selectConfigByKey("agent.dify.api_key").trim();

        log.info("Dify config - baseUrl: [{}], apiKey: [{}...]", apiBaseUrl,
                apiKey.length() > 8 ? apiKey.substring(0, 8) : apiKey);

        if (apiBaseUrl == null || apiBaseUrl.isEmpty() || apiKey == null || apiKey.isEmpty())
        {
            throw new RuntimeException("Dify配置未设置，请在参数配置中设置 agent.dify.api_base_url 和 agent.dify.api_key");
        }

        // 3. 取消该会话已有的流（如果有）
        AtomicBoolean existing = activeStreams.get(convId);
        if (existing != null)
        {
            existing.set(true);
        }

        // 4. 创建SseEmitter（5分钟超时）
        SseEmitter emitter = new SseEmitter(300000L);
        AtomicBoolean cancelled = new AtomicBoolean(false);
        activeStreams.put(convId, cancelled);

        // 5. 构建Dify请求
        DifyChatRequest difyReq = new DifyChatRequest();
        difyReq.setQuery(request.getQuery());
        difyReq.setConversationId(conversation.getDifyConversationId());
        difyReq.setUser(String.valueOf(userId));

        // 6. 异步调用Dify
        executor.execute(() -> processDifyStream(
                convId, apiBaseUrl, apiKey, difyReq, request.getQuery(),
                username, emitter, cancelled));

        // 7. 清理回调
        emitter.onTimeout(() -> {
            cancelled.set(true);
            activeStreams.remove(convId);
        });
        emitter.onError(ex -> {
            cancelled.set(true);
            activeStreams.remove(convId);
        });

        return emitter;
    }

    /**
     * 停止生成
     */
    @PostMapping("/stop")
    public AjaxResult stopGeneration(@RequestBody Map<String, Long> body)
    {
        Long conversationId = body.get("conversationId");
        if (conversationId != null)
        {
            AtomicBoolean flag = activeStreams.get(conversationId);
            if (flag != null)
            {
                flag.set(true);
            }
        }
        return success();
    }

    // ================ 私有方法 ================

    /**
     * 核心：异步处理Dify SSE流
     */
    private void processDifyStream(Long convId, String apiBaseUrl, String apiKey,
                                   DifyChatRequest difyReq, String userQuery,
                                   String username, SseEmitter emitter, AtomicBoolean cancelled)
    {
        // 内容累积器
        StringBuilder contentBuffer = new StringBuilder();
        List<Map<String, Object>> thinkingSteps = new ArrayList<>();
        List<Map<String, Object>> toolCallList = new ArrayList<>();
        String[] difyMessageId = {null};
        String[] difyConversationId = {null};
        int[] tokenCount = {0};
        boolean[] hasError = {false};  // 标记Dify是否返回了错误

        try
        {
            difyApiService.streamChat(apiBaseUrl, apiKey, difyReq, cancelled, event ->
            {
                try
                {
                    String eventType = event.getEvent();

                    switch (eventType)
                    {
                        case "message":
                            // LLM增量文本
                            contentBuffer.append(event.getAnswer());
                            Map<String, Object> msgData = new HashMap<>();
                            msgData.put("content", event.getAnswer());
                            emitter.send(SseEmitter.event().name("message").data(msgData));
                            break;

                        case "agent_message":
                            // Agent思考消息
                            addThinkingStep(thinkingSteps, "thought", event.getThought(), null, null, null);
                            sendThinkingEvent(emitter, thinkingSteps);
                            break;

                        case "agent_thought":
                            // Agent思考过程详情（thought / tool / observation）
                            handleAgentThought(event, thinkingSteps, toolCallList, emitter);
                            break;

                        case "message_end":
                            // 消息结束，提取元数据
                            difyMessageId[0] = event.getMessageId();
                            difyConversationId[0] = event.getConversationId();
                            if (event.getTotalTokens() != null)
                            {
                                tokenCount[0] = event.getTotalTokens();
                            }
                            if (event.getMetadata() != null)
                            {
                                // metadata可能包含usage
                                @SuppressWarnings("unchecked")
                                Map<String, Object> usage = (Map<String, Object>) event.getMetadata().get("usage");
                                if (usage != null && usage.get("total_tokens") != null)
                                {
                                    tokenCount[0] = ((Number) usage.get("total_tokens")).intValue();
                                }
                            }
                            break;

                        case "error":
                            log.error("Dify returned error: {}", event.getMessage());
                            hasError[0] = true;
                            Map<String, Object> errData = new HashMap<>();
                            errData.put("message", event.getMessage() != null ? event.getMessage() : "Dify服务错误");
                            emitter.send(SseEmitter.event().name("error").data(errData));
                            break;

                        default:
                            log.debug("Unknown Dify event type: {}", eventType);
                            break;
                    }
                }
                catch (IOException e)
                {
                    // 客户端断开连接
                    cancelled.set(true);
                }
            });

            // Dify流结束 → 落库 + 通知前端
            if (hasError[0])
            {
                // Dify返回了错误，不落库，不发送done
                log.warn("Dify stream ended with error for conversation {}, skipping persist", convId);
                emitter.complete();
            }
            else
            {
                persistMessages(convId, userQuery, contentBuffer.toString(),
                        thinkingSteps, toolCallList, difyMessageId[0], tokenCount[0], username);

                // 更新会话状态
                AgentConversation conv = conversationService.selectConversationById(convId);
                if (conv != null)
                {
                    if (difyConversationId[0] != null)
                    {
                        conv.setDifyConversationId(difyConversationId[0]);
                    }
                    conv.setStatus("1"); // 已结束
                    conv.setMessageCount((conv.getMessageCount() != null ? conv.getMessageCount() : 0) + 2);
                    conv.setUpdateBy(username);
                    conversationService.updateConversation(conv);
                }

                // 发送done事件
                Map<String, Object> doneData = new HashMap<>();
                doneData.put("messageId", difyMessageId[0]);
                doneData.put("difyMessageId", difyMessageId[0]);
                doneData.put("tokenCount", tokenCount[0]);
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("toolCalls", toolCallList);
                doneData.put("metadata", metadata);
                emitter.send(SseEmitter.event().name("done").data(doneData));
                emitter.complete();
            }

        }
        catch (Exception e)
        {
            log.error("Dify stream error for conversation {}: {}", convId, e.getMessage(), e);
            try
            {
                Map<String, Object> errData = new HashMap<>();
                errData.put("message", e.getMessage() != null ? e.getMessage() : "服务内部错误");
                emitter.send(SseEmitter.event().name("error").data(errData));
            }
            catch (IOException ignored) {}
            emitter.completeWithError(e);
        }
        finally
        {
            activeStreams.remove(convId);
        }
    }

    /**
     * 处理 agent_thought 事件
     */
    private void handleAgentThought(DifyEvent event, List<Map<String, Object>> steps,
                                     List<Map<String, Object>> toolCalls, SseEmitter emitter) throws IOException
    {
        // thought字段
        if (event.getThought() != null && !event.getThought().isEmpty())
        {
            addThinkingStep(steps, "thought", event.getThought(), null, null, null);
        }

        // tool字段 → 工具调用
        if (event.getTool() != null && !event.getTool().isEmpty())
        {
            Map<String, Object> input = null;
            if (event.getToolInput() != null)
            {
                try { input = JSON.parseObject(event.getToolInput()); }
                catch (Exception e) { input = Map.of("raw", event.getToolInput()); }
            }

            addThinkingStep(steps, "tool_call", "调用工具：" + event.getTool(),
                    event.getTool(), event.getToolLabel(), input);

            // 添加到toolCalls列表（给done事件的metadata用）
            Map<String, Object> tc = new HashMap<>();
            tc.put("id", "tc_" + (toolCalls.size() + 1));
            tc.put("name", event.getTool());
            tc.put("label", event.getToolLabel() != null ? event.getToolLabel() : event.getTool());
            tc.put("input", input);
            tc.put("status", "success");
            toolCalls.add(tc);

            // 单独发送tool_call事件给前端
            emitter.send(SseEmitter.event().name("tool_call").data(tc));
        }

        // observation字段 → 工具返回结果
        if (event.getObservation() != null && !event.getObservation().isEmpty())
        {
            Object result = null;
            try { result = JSON.parse(event.getObservation()); }
            catch (Exception e) { result = event.getObservation(); }

            addThinkingStep(steps, "tool_result", "工具返回结果",
                    event.getTool(), null, Map.of("result", result));
        }

        sendThinkingEvent(emitter, steps);
    }

    /**
     * 添加思考步骤到累积列表
     */
    private void addThinkingStep(List<Map<String, Object>> steps, String type, String content,
                                  String toolName, String toolLabel, Object input)
    {
        Map<String, Object> step = new HashMap<>();
        step.put("type", type);
        step.put("content", content != null ? content : "");
        step.put("status", "done");
        if (toolName != null) step.put("toolName", toolName);
        if (toolLabel != null) step.put("toolLabel", toolLabel);
        if (input != null) step.put("input", input);
        steps.add(step);
    }

    /**
     * 发送thinking SSE事件
     */
    private void sendThinkingEvent(SseEmitter emitter, List<Map<String, Object>> steps) throws IOException
    {
        int idx = steps.size() - 1;
        Map<String, Object> data = new HashMap<>();
        data.put("step", steps.get(idx));
        data.put("index", idx);
        data.put("total", steps.size());
        emitter.send(SseEmitter.event().name("thinking").data(data));
    }

    /**
     * 持久化用户消息和助手消息
     */
    private void persistMessages(Long convId, String userQuery, String assistantContent,
                                  List<Map<String, Object>> thinkingSteps,
                                  List<Map<String, Object>> toolCalls,
                                  String difyMsgId, int tokens, String username)
    {
        Date now = new Date();

        // 用户消息
        AgentMessage userMsg = new AgentMessage();
        userMsg.setConversationId(convId);
        userMsg.setRole("user");
        userMsg.setContent(userQuery);
        userMsg.setTokenCount(0);
        userMsg.setFeedback("0");
        userMsg.setCreateBy(username);
        userMsg.setCreateTime(now);
        messageService.insertMessage(userMsg);

        // 助手消息
        AgentMessage assistantMsg = new AgentMessage();
        assistantMsg.setConversationId(convId);
        assistantMsg.setDifyMessageId(difyMsgId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(assistantContent);
        assistantMsg.setThinking(JSON.toJSONString(Map.of("steps", thinkingSteps)));
        assistantMsg.setMessageMetadata(JSON.toJSONString(Map.of("toolCalls", toolCalls)));
        assistantMsg.setTokenCount(tokens);
        assistantMsg.setFeedback("0");
        assistantMsg.setCreateBy(username);
        assistantMsg.setCreateTime(now);
        messageService.insertMessage(assistantMsg);
    }
}
