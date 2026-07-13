package com.ruoyi.web.interceptor.agent;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.agent.tool.shared.AgentToolResults;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** 对所有 /agent/tools/** 请求统一校验 Dify 工具密钥。 */
@Component
public class AgentToolKeyInterceptor implements HandlerInterceptor {
    public static final String TOOL_KEY_HEADER = "X-Agent-Tool-Key";
    private static final String TOOL_KEY_CONFIG = "agent.tool.key";

    private final ISysConfigService configService;

    public AgentToolKeyInterceptor(ISysConfigService configService) {
        this.configService = configService;
    }

    /** 在进入任何工具 Controller 前执行统一鉴权。 */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()))
            return true;
        String configuredKey = configService.selectConfigByKey(TOOL_KEY_CONFIG);
        String providedKey = request.getHeader(TOOL_KEY_HEADER);
        if (matches(configuredKey, providedKey))
            return true;

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(JSON.toJSONString(AgentToolResults.unauthorized()));
        return false;
    }

    /** 使用常量时间比较，避免泄露工具密钥的前缀匹配信息。 */
    private boolean matches(String configuredKey, String providedKey) {
        if (StringUtils.isBlank(configuredKey) || StringUtils.isBlank(providedKey))
            return false;
        return MessageDigest.isEqual(configuredKey.getBytes(StandardCharsets.UTF_8),
                providedKey.getBytes(StandardCharsets.UTF_8));
    }
}
