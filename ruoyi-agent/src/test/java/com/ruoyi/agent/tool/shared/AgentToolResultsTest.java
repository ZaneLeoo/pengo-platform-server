package com.ruoyi.agent.tool.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Agent 工具协议工厂测试。 */
class AgentToolResultsTest
{
    @Test
    void confirmResultShouldRequireFrontendConfirmation()
    {
        AgentToolResult<String> result = AgentToolResults.confirm(TestCode.DRAFT_READY,
            "草稿已准备", "draft", "由前端展示确认卡片。");

        assertEquals(AgentToolStatus.SUCCESS, result.status());
        assertEquals(AgentToolNextAction.CONFIRM_ACTION, result.nextAction());
        assertEquals("DRAFT_READY", result.resultCode());
        assertFalse(result.retryable());
        assertTrue(result.agentInstruction().contains("不要执行真实写操作"));
        assertTrue(result.agentInstruction().contains("由前端展示确认卡片"));
    }

    @Test
    void needInputShouldExposeIssuesAndForbidBlindRetry()
    {
        AgentToolIssue issue = AgentToolIssue.of("MISSING", "lines[0].quantity", "缺少数量", "大于 0");
        AgentToolResult<Void> result = AgentToolResults.needInput(TestCode.MISSING,
            "信息不完整", List.of(issue), null);

        assertEquals(AgentToolStatus.NEED_INPUT, result.status());
        assertEquals(AgentToolNextAction.ASK_USER, result.nextAction());
        assertEquals(List.of(issue), result.issues());
        assertFalse(result.retryable());
        assertNull(result.data());
    }

    private enum TestCode implements AgentToolResultCode
    {
        DRAFT_READY,
        MISSING;

        @Override
        public String code()
        {
            return name();
        }
    }
}
