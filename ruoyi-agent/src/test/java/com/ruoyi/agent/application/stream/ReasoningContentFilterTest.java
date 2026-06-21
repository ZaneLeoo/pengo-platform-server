package com.ruoyi.agent.application.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ReasoningContentFilterTest
{
    @Test
    void shouldRemoveReasoningAndKeepAnswerInSameChunk()
    {
        ReasoningContentFilter filter = new ReasoningContentFilter();

        assertEquals("正式答案", filter.accept("<think>内部推理</think>正式答案"));
        assertEquals("", filter.finish());
    }

    @Test
    void shouldRecognizeTagsSplitAcrossChunks()
    {
        ReasoningContentFilter filter = new ReasoningContentFilter();

        assertEquals("", filter.accept("<thi"));
        assertEquals("", filter.accept("nk>不能展示</thi"));
        assertEquals("答案", filter.accept("nk>答案"));
        assertEquals("", filter.finish());
    }

    @Test
    void shouldStreamNormalTextWithoutWaitingForCompletion()
    {
        ReasoningContentFilter filter = new ReasoningContentFilter();

        assertEquals("你好", filter.accept("你好"));
        assertEquals("，世界", filter.accept("，世界"));
        assertEquals("", filter.finish());
    }

    @Test
    void shouldDiscardUnclosedReasoningAtEnd()
    {
        ReasoningContentFilter filter = new ReasoningContentFilter();

        assertEquals("前言", filter.accept("前言<think>未结束的推理"));
        assertEquals("", filter.finish());
    }

    @Test
    void shouldFlushOrdinaryTextThatLooksLikeTagPrefix()
    {
        ReasoningContentFilter filter = new ReasoningContentFilter();

        assertEquals("内容", filter.accept("内容<"));
        assertEquals("<", filter.finish());
    }
}
