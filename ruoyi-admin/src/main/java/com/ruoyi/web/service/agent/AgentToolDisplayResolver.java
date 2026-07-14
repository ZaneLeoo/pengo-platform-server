package com.ruoyi.web.service.agent;

import com.ruoyi.agent.infrastructure.dify.model.DifyStreamEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/** 负责将 Dify 工具机器标识转换为适合前端展示的名称和描述。 */
@Component
public class AgentToolDisplayResolver {
    private static final String TOOL_SEPARATOR = ";";
    private final AgentToolMetadataRegistry metadataRegistry;

    public AgentToolDisplayResolver(AgentToolMetadataRegistry metadataRegistry) {
        this.metadataRegistry = metadataRegistry;
    }

    /**
     * 解析工具展示名称；Dify 将并行调用拼成分号字符串时，按工具名称聚合并显示调用次数。
     */
    public String resolveLabel(DifyStreamEvent event) {
        List<String> toolNames = splitToolNames(event.getTool());
        if (toolNames.isEmpty()) {
            return "";
        }
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String toolName : toolNames) {
            counts.merge(toolName, 1, Integer::sum);
        }
        List<String> labels = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            String label = resolveSingleLabel(entry.getKey(), event.getToolLabels());
            labels.add(entry.getValue() > 1 ? label + " × " + entry.getValue() : label);
        }
        return String.join("、", labels);
    }

    /** 返回批量工具涉及的所有非重复描述。 */
    public String resolveDescription(String toolNames) {
        Set<String> descriptions = new LinkedHashSet<>();
        for (String toolName : splitToolNames(toolNames)) {
            AgentToolMetadataRegistry.ToolMetadata metadata = metadataRegistry.find(toolName);
            if (metadata != null && metadata.getDescription() != null && !metadata.getDescription().isBlank()) {
                descriptions.add(metadata.getDescription());
            }
        }
        return String.join("；", descriptions);
    }

    /** 解析单个工具展示名称，供图表等已拆分事件使用。 */
    public String resolveSingleLabel(String toolName) {
        return resolveSingleLabel(toolName, Map.of());
    }

    private String resolveSingleLabel(String toolName, Map<String, Object> difyLabels) {
        AgentToolMetadataRegistry.ToolMetadata metadata = metadataRegistry.find(toolName);
        if (metadata != null) {
            return metadata.getLabel();
        }
        Object label = difyLabels.get(toolName);
        if (label instanceof Map<?, ?> labels) {
            Object zh = labels.get("zh_Hans");
            if (isMeaningfulChineseLabel(zh, toolName)) {
                return zh.toString().trim();
            }
            Object en = labels.get("en_US");
            if (isMeaningfulLabel(en, toolName)) {
                return en.toString().trim();
            }
        }
        return toolName;
    }

    private List<String> splitToolNames(String toolNames) {
        if (toolNames == null || toolNames.isBlank()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (String toolName : toolNames.split(TOOL_SEPARATOR)) {
            String normalized = toolName.trim();
            if (!normalized.isBlank()) {
                values.add(normalized);
            }
        }
        return values;
    }

    private boolean isMeaningfulChineseLabel(Object value, String toolName) {
        if (!isMeaningfulLabel(value, toolName)) {
            return false;
        }
        return value.toString().codePoints().anyMatch(this::isChineseCodePoint);
    }

    private boolean isMeaningfulLabel(Object value, String toolName) {
        return value != null && !value.toString().isBlank()
                && !value.toString().trim().equalsIgnoreCase(toolName);
    }

    private boolean isChineseCodePoint(int codePoint) {
        return (codePoint >= 0x4E00 && codePoint <= 0x9FFF)
                || (codePoint >= 0x3400 && codePoint <= 0x4DBF);
    }
}
