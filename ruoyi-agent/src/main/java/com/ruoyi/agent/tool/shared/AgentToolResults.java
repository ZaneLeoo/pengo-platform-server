package com.ruoyi.agent.tool.shared;

import java.util.List;

/** Agent 工具统一结果工厂，集中维护模型行为约定。 */
public final class AgentToolResults {
    private AgentToolResults() {
    }

    /** 返回可供 Agent 继续推理的成功结果。 */
    public static <T> AgentToolResult<T> success(AgentToolResultCode code, String message, T data,
            AgentToolMeta meta) {
        return build(AgentToolStatus.SUCCESS, code, message, AgentToolNextAction.CONTINUE, false,
                "使用 data 回答用户或继续完成当前任务；不要用相同参数重复调用工具。", List.of(), data, meta, "");
    }

    /** 返回应直接展示给用户的成功结果。 */
    public static <T> AgentToolResult<T> present(AgentToolResultCode code, String message, T data,
            AgentToolMeta meta, String detailInstruction) {
        return build(AgentToolStatus.SUCCESS, code, message, AgentToolNextAction.PRESENT_RESULT, false,
                "向用户清晰展示 data 中的结果；不要用相同参数重复调用工具。", List.of(), data, meta,
                detailInstruction);
    }

    /** 返回需要用户补充字段的结果。 */
    public static <T> AgentToolResult<T> needInput(AgentToolResultCode code, String message,
            List<AgentToolIssue> issues, T data) {
        return build(AgentToolStatus.NEED_INPUT, code, message, AgentToolNextAction.ASK_USER, false,
                "不要用相同参数重试。仅询问 issues 中列出的字段，收到补充信息后再重新调用。", issues,
                data, null, "");
    }

    /** 返回需要用户从候选项中明确选择的结果。 */
    public static <T> AgentToolResult<T> ambiguous(AgentToolResultCode code, String message,
            List<AgentToolIssue> issues, T data) {
        return build(AgentToolStatus.AMBIGUOUS, code, message, AgentToolNextAction.SELECT_CANDIDATE, false,
                "不要用相同参数重试。展示 issues.candidates 并等待用户明确选择 value。", issues, data,
                null, "");
    }

    /** 返回查询成功但没有业务数据的结果。 */
    public static <T> AgentToolResult<T> noResult(AgentToolResultCode code, String message, T data,
            AgentToolMeta meta) {
        return build(AgentToolStatus.NO_RESULT, code, message, AgentToolNextAction.ASK_USER, false,
                "说明当前条件没有查询到结果，并询问用户是否调整查询条件；不要用相同参数重试。", List.of(),
                data, meta, "");
    }

    /** 返回业务规则拒绝继续执行的结果。 */
    public static <T> AgentToolResult<T> rejected(AgentToolResultCode code, String message,
            List<AgentToolIssue> issues, T data) {
        return build(AgentToolStatus.REJECTED, code, message, AgentToolNextAction.ASK_USER, false,
                "说明业务校验未通过，并依据 issues 请求用户修正；不要用相同参数重试。", issues, data,
                null, "");
    }

    /** 返回必须由前端等待用户确认的业务草稿。 */
    public static <T> AgentToolResult<T> confirm(AgentToolResultCode code, String message, T data,
            String detailInstruction) {
        return build(AgentToolStatus.SUCCESS, code, message, AgentToolNextAction.CONFIRM_ACTION, false,
                "不要执行真实写操作。等待前端展示确认界面并由用户明确确认。", List.of(), data, null,
                detailInstruction);
    }

    /** 返回工具鉴权失败结果。 */
    public static AgentToolResult<Void> unauthorized() {
        return build(AgentToolStatus.FAILED, CommonAgentToolResultCode.TOOL_UNAUTHORIZED, "工具鉴权失败",
                AgentToolNextAction.STOP, false, "停止调用并提示系统管理员检查工具鉴权配置。", List.of(),
                null, null, "");
    }

    /** 返回无法由模型修复的内部错误结果。 */
    public static AgentToolResult<Void> failed(String message) {
        return build(AgentToolStatus.FAILED, CommonAgentToolResultCode.INTERNAL_ERROR, message,
                AgentToolNextAction.STOP, false, "停止调用，不要使用相同参数重复尝试。", List.of(), null,
                null, "");
    }

    private static <T> AgentToolResult<T> build(AgentToolStatus status, AgentToolResultCode code,
            String message, AgentToolNextAction nextAction, boolean retryable, String instruction,
            List<AgentToolIssue> issues, T data, AgentToolMeta meta, String detailInstruction) {
        String fullInstruction = appendInstruction(instruction, detailInstruction);
        return new AgentToolResult<>(status, code.code(), message, nextAction, retryable, fullInstruction,
                issues, data, meta);
    }

    private static String appendInstruction(String instruction, String detailInstruction) {
        if (detailInstruction == null || detailInstruction.isBlank())
            return instruction;
        return instruction + " " + detailInstruction.trim();
    }
}
