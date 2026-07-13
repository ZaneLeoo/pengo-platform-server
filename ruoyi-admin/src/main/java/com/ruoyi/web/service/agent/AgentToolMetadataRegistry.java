package com.ruoyi.web.service.agent;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * 从 OpenAPI YAML 和非 OpenAPI 工具配置中加载 Agent 工具展示元数据。
 *
 * <p>
 * 工具的 operationId、展示名称和描述属于版本化技术元数据，不写入业务数据库。 OpenAPI 的 summary 和 description
 * 是主要来源，图表插件等非 OpenAPI 工具使用 agent-tool-metadata.yaml 作为补充配置。
 * </p>
 */
@Component
public class AgentToolMetadataRegistry {
    private static final Logger log = LoggerFactory.getLogger(AgentToolMetadataRegistry.class);
    private static final String OPENAPI_PATTERN = "classpath*:openapi/*.yaml";
    private static final String OPENAPI_YML_PATTERN = "classpath*:openapi/*.yml";
    private static final String EXTRA_METADATA_PATTERN = "classpath*:agent-tool-metadata.yaml";

    private ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private Map<String, ToolMetadata> definitions = new ConcurrentHashMap<>();
    private PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    /** 应用启动时加载所有工具元数据。 */
    @PostConstruct
    public void load() {
        definitions.clear();
        loadOpenApiResources(findResources(OPENAPI_PATTERN));
        loadOpenApiResources(findResources(OPENAPI_YML_PATTERN));
        loadExtraMetadata(findResources(EXTRA_METADATA_PATTERN));
        log.info("Loaded {} agent tool metadata definitions", definitions.size());
    }

    /** 按 operationId 查询工具元数据。 */
    public ToolMetadata find(String operationId) {
        if (operationId == null || operationId.isBlank())
            return null;
        return definitions.get(operationId);
    }

    private Resource[] findResources(String pattern) {
        try {
            return resourceResolver.getResources(pattern);
        } catch (IOException e) {
            log.warn("Unable to scan agent tool metadata resources: {}", pattern, e);
            return new Resource[0];
        }
    }

    private void loadOpenApiResources(Resource[] resources) {
        for (Resource resource : resources) {
            try (InputStream input = resource.getInputStream()) {
                Map<String, Object> document = yamlMapper.readValue(input,
                        new TypeReference<Map<String, Object>>() {
                        });
                Object paths = document.get("paths");
                if (!(paths instanceof Map<?, ?> pathMap))
                    continue;
                for (Object pathValue : pathMap.values()) {
                    if (!(pathValue instanceof Map<?, ?> methodMap))
                        continue;
                    for (Object operationValue : methodMap.values()) {
                        if (operationValue instanceof Map<?, ?> operation) {
                            register(operation, resource.getFilename());
                        }
                    }
                }
            } catch (IOException | RuntimeException e) {
                log.warn("Unable to load agent OpenAPI metadata: {}", resource.getDescription(), e);
            }
        }
    }

    private void loadExtraMetadata(Resource[] resources) {
        for (Resource resource : resources) {
            try (InputStream input = resource.getInputStream()) {
                Map<String, Object> document = yamlMapper.readValue(input,
                        new TypeReference<Map<String, Object>>() {
                        });
                Object tools = document.get("tools");
                if (!(tools instanceof List<?> toolList))
                    continue;
                for (Object toolValue : toolList) {
                    if (toolValue instanceof Map<?, ?> tool) {
                        register(tool, resource.getFilename());
                    }
                }
            } catch (IOException | RuntimeException e) {
                log.warn("Unable to load extra agent tool metadata: {}", resource.getDescription(), e);
            }
        }
    }

    private void register(Map<?, ?> operation, String source) {
        String operationId = text(operation.get("operationId"));
        String label = text(operation.get("summary"));
        if (label.isBlank())
            label = text(operation.get("label"));
        String description = text(operation.get("description"));
        if (operationId.isBlank() || label.isBlank())
            return;
        ToolMetadata metadata = new ToolMetadata(operationId, label, description, source);
        ToolMetadata previous = definitions.putIfAbsent(operationId, metadata);
        if (previous != null) {
            log.error("Duplicate agent tool operationId '{}' in {} and {}", operationId,
                    previous.getSource(), source);
        }
    }

    private String text(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    /** 工具展示元数据。 */
    @Data
    @NoArgsConstructor
    public static class ToolMetadata {
        private String operationId;
        private String label;
        private String description;
        private String source;

        public ToolMetadata(String operationId, String label, String description, String source) {
            this.operationId = operationId;
            this.label = label == null ? "" : label.trim();
            this.description = description == null ? "" : description.trim();
            this.source = source == null ? "" : source.trim();
        }
    }
}
