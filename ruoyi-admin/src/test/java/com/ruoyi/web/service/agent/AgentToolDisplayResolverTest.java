package com.ruoyi.web.service.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ruoyi.agent.infrastructure.dify.model.DifyStreamEvent;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgentToolDisplayResolverTest {
    private AgentToolDisplayResolver resolver;

    @BeforeEach
    void setUp() {
        AgentToolMetadataRegistry registry = new AgentToolMetadataRegistry();
        registry.load();
        resolver = new AgentToolDisplayResolver(registry);
    }

    @Test
    void aggregatesRepeatedToolNamesWithLocalizedLabel() {
        DifyStreamEvent event = new DifyStreamEvent();
        event.setTool("queryMaterials;queryMaterials;queryMaterials");
        event.setToolLabels(Map.of("queryMaterials",
                Map.of("en_US", "queryMaterials", "zh_Hans", "queryMaterials")));

        assertEquals("查询物料 × 3", resolver.resolveLabel(event));
    }

    @Test
    void localizesAndCountsDifferentToolsInOriginalOrder() {
        DifyStreamEvent event = new DifyStreamEvent();
        event.setTool("queryMaterials;queryBoms;queryMaterials");

        assertEquals("查询物料 × 2、查询BOM和版本", resolver.resolveLabel(event));
    }
}
