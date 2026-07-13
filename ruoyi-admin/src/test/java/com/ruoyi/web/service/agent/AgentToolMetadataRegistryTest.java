package com.ruoyi.web.service.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class AgentToolMetadataRegistryTest {
    @Test
    void loadsOpenApiAndExtraToolMetadata() {
        AgentToolMetadataRegistry registry = new AgentToolMetadataRegistry();

        registry.load();

        AgentToolMetadataRegistry.ToolMetadata material = registry.find("queryMaterials");
        assertNotNull(material);
        assertEquals("查询物料", material.label());
        assertEquals("根据物料编码、名称、规格、分类或物料类型查询物料。", material.description());

        AgentToolMetadataRegistry.ToolMetadata chart = registry.find("bar_chart");
        assertNotNull(chart);
        assertEquals("生成柱状图", chart.label());
    }
}
