package com.ruoyi.agent.application.artifact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ArtifactExtractorTest
{
    private final ArtifactExtractor extractor = new ArtifactExtractor(new ArtifactValidator());

    @Test
    void shouldExtractValidChartAndTableArtifacts()
    {
        Map<String, Object> chart = Map.of(
            "type", "chart", "version", "1.0", "title", "月度销售趋势",
            "payload", Map.of("chartType", "line", "categories", List.of("1月", "2月"),
                "series", List.of(Map.of("name", "销售额", "data", List.of(120, 180)))));
        Map<String, Object> table = Map.of(
            "type", "table", "version", "1.0", "title", "销售明细",
            "payload", Map.of("columns", List.of("月份", "销售额"),
                "rows", List.of(List.of("1月", 120))));
        Map<String, Object> data = Map.of("outputs", Map.of("artifacts", List.of(chart, table)));

        List<AgentArtifact> artifacts = extractor.extract(data);

        assertEquals(2, artifacts.size());
        assertEquals("chart", artifacts.get(0).getType());
        assertEquals("table", artifacts.get(1).getType());
    }

    @Test
    void shouldDiscardUnsupportedOrUnsafeArtifacts()
    {
        Map<String, Object> unsafeChart = Map.of(
            "type", "chart", "version", "1.0", "title", "非法图表",
            "payload", Map.of("chartType", "custom", "option", "alert(1)"));
        Map<String, Object> unknown = Map.of(
            "type", "html", "version", "1.0", "title", "危险内容",
            "payload", Map.of("html", "<script>alert(1)</script>"));

        List<AgentArtifact> artifacts = extractor.extract(
            Map.of("outputs", Map.of("artifacts", List.of(unsafeChart, unknown))));

        assertTrue(artifacts.isEmpty());
    }

    @Test
    void shouldAcceptJsonStringArtifactsFromDifyOutput()
    {
        String json = "[{\"type\":\"table\",\"version\":\"1.0\",\"title\":\"结果\","
            + "\"payload\":{\"columns\":[\"name\"],\"rows\":[[\"RuoYi\"]]}}]";

        List<AgentArtifact> artifacts = extractor.extract(Map.of("outputs", Map.of("artifacts", json)));

        assertEquals(1, artifacts.size());
        assertEquals("结果", artifacts.get(0).getTitle());
    }

    @Test
    void shouldNormalizeXAxisToCanonicalCategories()
    {
        Map<String, Object> chart = Map.of(
            "type", "chart", "version", "1.0", "title", "季度销售趋势",
            "payload", Map.of("chartType", "bar", "xAxis", List.of("第一季度", "第二季度"),
                "series", List.of(Map.of("name", "营业额", "data", List.of(128.5, 256.2)))));

        AgentArtifact artifact = extractor.extract(
            Map.of("outputs", Map.of("artifacts", List.of(chart)))).get(0);

        assertEquals(List.of("第一季度", "第二季度"), artifact.getPayload().get("categories"));
    }

    @Test
    void shouldExtractArtifactsFromDifyToolOutputsKey()
    {
        Map<String, Object> chart = Map.of(
            "type", "chart", "version", "1.0", "title", "季度销售BI柱状图 (测试)",
            "payload", Map.of("chartType", "bar", "xAxis", List.of("第一季度", "第二季度", "第三季度"),
                "series", List.of(Map.of("name", "营业额(万)", "data", List.of(128.5, 256.2, 198.4)))));

        List<AgentArtifact> artifacts = extractor.extract(Map.of("outputs", Map.of("outputs", List.of(chart))));

        assertEquals(1, artifacts.size());
        assertEquals("季度销售BI柱状图 (测试)", artifacts.get(0).getTitle());
        assertEquals(List.of("第一季度", "第二季度", "第三季度"), artifacts.get(0).getPayload().get("categories"));
    }
}
