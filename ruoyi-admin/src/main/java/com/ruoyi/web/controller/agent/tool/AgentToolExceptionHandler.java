package com.ruoyi.web.controller.agent.tool;

import com.ruoyi.agent.tool.shared.AgentToolIssue;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import com.ruoyi.agent.tool.shared.AgentToolResults;
import com.ruoyi.agent.tool.shared.CommonAgentToolResultCode;
import java.util.List;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 统一工具入口的参数错误结构，避免 Dify 只能看到通用 AjaxResult。 */
@RestControllerAdvice(basePackages = "com.ruoyi.web.controller.agent.tool")
public class AgentToolExceptionHandler
{
    /** 返回字段路径明确的 Bean Validation 错误。 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public AgentToolResult<Void> handleValidation(MethodArgumentNotValidException exception)
    {
        List<AgentToolIssue> issues = exception.getBindingResult().getFieldErrors().stream()
            .map(this::toIssue).toList();
        return AgentToolResults.rejected(CommonAgentToolResultCode.INVALID_REQUEST,
            "工具请求参数校验失败", issues, null);
    }

    /** 返回 JSON 类型或格式错误，明确要求模型修正参数。 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public AgentToolResult<Void> handleUnreadableBody(HttpMessageNotReadableException exception)
    {
        AgentToolIssue issue = AgentToolIssue.of("INVALID_JSON", "requestBody",
            "请求 JSON 的字段类型或格式不正确", "严格按照 OpenAPI schema 传参");
        return AgentToolResults.rejected(CommonAgentToolResultCode.INVALID_REQUEST,
            "无法解析工具请求参数", List.of(issue), null);
    }

    /** 转换字段校验错误。 */
    private AgentToolIssue toIssue(FieldError error)
    {
        return AgentToolIssue.of("INVALID_FIELD", error.getField(), error.getDefaultMessage(),
            "符合 OpenAPI 中该字段的约束");
    }
}
