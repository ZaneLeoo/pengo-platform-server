package com.ruoyi.agent.application.stream;

/**
 * 过滤流式文本中的模型推理标签，只返回可展示的正式答案。
 * 每次回答必须使用独立实例。
 */
public class ReasoningContentFilter
{
    private static final String OPEN_TAG = "<think>";
    private static final String CLOSE_TAG = "</think>";

    private final StringBuilder pending = new StringBuilder();
    private boolean reasoning;

    /**
     * 接收一个文本分片并返回其中已经确定安全的正文增量。
     *
     * @param chunk Dify 文本分片
     * @return 不包含推理标签及推理内容的正文增量
     */
    public String accept(String chunk)
    {
        if (chunk == null || chunk.isEmpty())
        {
            return "";
        }
        pending.append(chunk);
        StringBuilder visible = new StringBuilder();
        while (!pending.isEmpty())
        {
            String tag = reasoning ? CLOSE_TAG : OPEN_TAG;
            int tagIndex = pending.indexOf(tag);
            if (tagIndex >= 0)
            {
                if (!reasoning)
                {
                    visible.append(pending, 0, tagIndex);
                }
                pending.delete(0, tagIndex + tag.length());
                reasoning = !reasoning;
                continue;
            }

            int retained = matchingSuffixLength(pending, tag);
            int resolvedLength = pending.length() - retained;
            if (!reasoning && resolvedLength > 0)
            {
                visible.append(pending, 0, resolvedLength);
            }
            pending.delete(0, resolvedLength);
            break;
        }
        return visible.toString();
    }

    /**
     * 结束流并返回尚未输出的普通文本；未闭合推理内容会被丢弃。
     *
     * @return 最后可安全展示的正文
     */
    public String finish()
    {
        String tail = reasoning ? "" : pending.toString();
        pending.setLength(0);
        return tail;
    }

    /** 计算缓冲区尾部与目标标签开头相同的最长长度。 */
    private int matchingSuffixLength(StringBuilder value, String tag)
    {
        int max = Math.min(value.length(), tag.length() - 1);
        for (int length = max; length > 0; length--)
        {
            boolean matches = true;
            for (int index = 0; index < length; index++)
            {
                if (value.charAt(value.length() - length + index) != tag.charAt(index))
                {
                    matches = false;
                    break;
                }
            }
            if (matches)
            {
                return length;
            }
        }
        return 0;
    }
}
