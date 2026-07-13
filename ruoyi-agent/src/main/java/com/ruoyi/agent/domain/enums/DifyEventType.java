package com.ruoyi.agent.domain.enums;

/** Dify Chatflow 流式事件。 */
public enum DifyEventType {
    MESSAGE("message"), MESSAGE_REPLACE("message_replace"), MESSAGE_END("message_end"), ERROR(
            "error"), WORKFLOW_STARTED("workflow_started"), WORKFLOW_FINISHED("workflow_finished"), WORKFLOW_PAUSED(
                    "workflow_paused"), HUMAN_INPUT_REQUIRED("human_input_required"), NODE_STARTED(
                            "node_started"), NODE_FINISHED("node_finished"), NODE_RETRY(
                                    "node_retry"), ITERATION_STARTED("iteration_started"), ITERATION_NEXT(
                                            "iteration_next"), ITERATION_COMPLETED("iteration_completed"), LOOP_STARTED(
                                                    "loop_started"), LOOP_NEXT("loop_next"), LOOP_COMPLETED(
                                                            "loop_completed"), AGENT_LOG("agent_log"), MESSAGE_FILE(
                                                                    "message_file"), AGENT_MESSAGE(
                                                                            "agent_message"), AGENT_THOUGHT(
                                                                                    "agent_thought"), UNKNOWN(
                                                                                            "unknown");
    private final String code;
    DifyEventType(String code) {
        this.code = code;
    }
    /** 返回协议值。 */
    public String getCode() {
        return code;
    }

    /** 将协议值转换为枚举，未知事件交由编排层忽略。 */
    public static DifyEventType fromCode(String code) {
        for (DifyEventType type : values())
            if (type.code.equals(code))
                return type;
        return UNKNOWN;
    }

    /** 判断是否属于工作流级展示事件。 */
    public boolean isWorkflowEvent() {
        return this == WORKFLOW_STARTED || this == WORKFLOW_FINISHED || this == WORKFLOW_PAUSED
                || this == HUMAN_INPUT_REQUIRED;
    }

    /** 判断是否属于节点或迭代级展示事件。 */
    public boolean isNodeEvent() {
        return this == NODE_STARTED || this == NODE_FINISHED || this == NODE_RETRY || this == ITERATION_STARTED
                || this == ITERATION_NEXT || this == ITERATION_COMPLETED || this == LOOP_STARTED || this == LOOP_NEXT
                || this == LOOP_COMPLETED || this == AGENT_LOG || this == AGENT_THOUGHT;
    }
}
