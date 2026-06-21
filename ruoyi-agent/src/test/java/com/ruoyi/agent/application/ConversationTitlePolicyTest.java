package com.ruoyi.agent.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConversationTitlePolicyTest
{
    private final ConversationTitlePolicy policy = new ConversationTitlePolicy();

    @Test
    void shouldUseDefaultTitleWhenQueryIsBlank()
    {
        assertEquals("新对话", policy.fromQuery("  "));
    }

    @Test
    void shouldCollapseWhitespaceAndLimitTitleLength()
    {
        String query = "请帮我\n分析   2026 年第二季度的销售情况，并给出重点结论和改进建议";

        assertEquals("请帮我 分析 2026 年第二季度的销售情况，并给出重点结论", policy.fromQuery(query));
    }
}
