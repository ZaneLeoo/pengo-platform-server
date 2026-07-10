package com.ruoyi.agent.application.tool;

import com.ruoyi.agent.domain.enums.AgentToolCode;
import com.ruoyi.common.exception.ServiceException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** 启动时注册 Java 工具实现，并阻止编码重复。 */
@Component
public class AgentToolRegistry
{
    private final Map<AgentToolCode, AgentToolHandler> handlers;

    public AgentToolRegistry(List<AgentToolHandler> handlers)
    {
        Map<AgentToolCode, AgentToolHandler> values = new EnumMap<>(AgentToolCode.class);
        for (AgentToolHandler handler : handlers)
        {
            AgentToolHandler previous = values.put(handler.code(), handler);
            if (previous != null)
            {
                throw new IllegalStateException("Agent工具编码重复：" + handler.code().getCode());
            }
        }
        this.handlers = Map.copyOf(values);
    }

    /** 获取已注册实现；未实现的 Dify 调用按受控业务错误返回。 */
    public AgentToolHandler require(String code)
    {
        AgentToolCode toolCode;
        try
        {
            toolCode = AgentToolCode.fromCode(code);
        }
        catch (IllegalArgumentException e)
        {
            throw new ServiceException(e.getMessage());
        }
        AgentToolHandler handler = handlers.get(toolCode);
        if (handler == null)
        {
            throw new ServiceException("Agent工具尚未启用：" + code);
        }
        return handler;
    }

    /** 返回当前 Java 侧真正可执行的工具编码。 */
    public List<String> registeredCodes()
    {
        return handlers.keySet().stream().map(AgentToolCode::getCode).sorted().toList();
    }
}
