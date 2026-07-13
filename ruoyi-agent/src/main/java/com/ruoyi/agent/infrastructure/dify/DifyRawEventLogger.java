package com.ruoyi.agent.infrastructure.dify;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** 安全记录 Dify 返回的原始 SSE 行。 */
@Component
public class DifyRawEventLogger {
    static final int MAX_LOG_LENGTH = 32 * 1024;
    private static final String TRUNCATED_SUFFIX = "...[TRUNCATED]";
    private static final Pattern SECRET_FIELD = Pattern.compile(
            "(?i)(\\\"(?:api[_-]?key|authorization|access[_-]?token|token)\\\"\\s*:\\s*\\\")[^\\\"]*(\\\")");
    private static final Pattern AUTHORIZATION = Pattern.compile("(?i)(Bearer\\s+)[A-Za-z0-9._~+\\-/]+=*");
    private static final Pattern LARGE_BASE64_FIELD = Pattern.compile(
            "(\\\"(?:audio|content|data|base64)\\\"\\s*:\\s*\\\")[A-Za-z0-9+/=]{128,}(\\\")",
            Pattern.CASE_INSENSITIVE);
    private static final Logger LOGGER = LoggerFactory.getLogger(DifyRawEventLogger.class);

    private final Consumer<String> logConsumer;
    private final BooleanSupplier debugEnabled;

    /** 创建使用 SLF4J DEBUG 级别的原始事件日志器。 */
    public DifyRawEventLogger() {
        this(message -> LOGGER.debug("Dify SSE raw: {}", message), LOGGER::isDebugEnabled);
    }

    /** 创建可测试的日志器实例。 */
    DifyRawEventLogger(Consumer<String> logConsumer, BooleanSupplier debugEnabled) {
        this.logConsumer = logConsumer;
        this.debugEnabled = debugEnabled;
    }

    /** 在 DEBUG 开启时安全记录单个非空 SSE 原始行。 */
    public void log(String rawLine) {
        if (!debugEnabled.getAsBoolean() || rawLine == null || rawLine.isBlank()) {
            return;
        }
        try {
            logConsumer.accept(truncate(mask(rawLine)));
        } catch (RuntimeException ignored) {
            // 调试日志不得中断 Dify 流式响应。
        }
    }

    /** 脱敏凭证字段和大体积 Base64 字段。 */
    private String mask(String rawLine) {
        String masked = SECRET_FIELD.matcher(rawLine).replaceAll("$1***$2");
        masked = AUTHORIZATION.matcher(masked).replaceAll("$1***");
        return LARGE_BASE64_FIELD.matcher(masked).replaceAll("$1[BASE64_REDACTED]$2");
    }

    /** 将单条日志限制在 32KB 字符范围内。 */
    private String truncate(String message) {
        if (message.length() <= MAX_LOG_LENGTH) {
            return message;
        }
        return message.substring(0, MAX_LOG_LENGTH - TRUNCATED_SUFFIX.length()) + TRUNCATED_SUFFIX;
    }
}
