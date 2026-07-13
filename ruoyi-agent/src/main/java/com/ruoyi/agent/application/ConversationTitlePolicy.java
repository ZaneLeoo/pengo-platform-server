package com.ruoyi.agent.application;

import java.util.regex.Pattern;

/**
 * 根据首轮问题生成本地会话标题。
 */
@org.springframework.stereotype.Component
public class ConversationTitlePolicy {
    private static final int MAX_TITLE_LENGTH = 30;
    private static final String DEFAULT_TITLE = "新对话";
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    /**
     * 将用户问题转换为适合会话列表展示的标题。
     *
     * @param query
     *            用户问题
     * @return 去除多余空白且不超过三十个字符的标题
     */
    public String fromQuery(String query) {
        if (query == null || query.isBlank()) {
            return DEFAULT_TITLE;
        }
        String normalized = WHITESPACE.matcher(query.trim()).replaceAll(" ");
        return normalized.length() <= MAX_TITLE_LENGTH
                ? normalized
                : normalized.substring(0, MAX_TITLE_LENGTH);
    }
}
