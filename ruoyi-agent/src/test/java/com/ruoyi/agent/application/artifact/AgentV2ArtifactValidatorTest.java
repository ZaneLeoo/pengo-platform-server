package com.ruoyi.agent.application.artifact;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AgentV2ArtifactValidatorTest
{
    private final AgentV2ArtifactValidator validator = new AgentV2ArtifactValidator();

    @Test
    void shouldAcceptSemanticChartPayload()
    {
        AgentV2Artifact artifact = chart(Map.of(
            "chartType", "bar",
            "categories", List.of("成品", "半成品"),
            "series", List.of(Map.of("name", "数量", "data", List.of(8, 12)))
        ));

        assertTrue(validator.isValid(artifact));
    }

    @Test
    void shouldRejectRawEchartsOption()
    {
        AgentV2Artifact artifact = chart(Map.of(
            "chartType", "bar",
            "option", Map.of("xAxis", Map.of()),
            "series", List.of(Map.of("name", "数量", "data", List.of(8)))
        ));

        assertFalse(validator.isValid(artifact));
    }

    @Test
    void shouldRequireFileReferenceForDocument()
    {
        AgentV2Artifact artifact = new AgentV2Artifact();
        artifact.setType("DOCUMENT");
        artifact.setTitle("销售分析.xlsx");

        assertFalse(validator.isValid(artifact));
        artifact.setFileId(99L);
        assertTrue(validator.isValid(artifact));
    }

    @Test
    void shouldRejectUnsafeDocumentUrls()
    {
        AgentV2Artifact artifact = new AgentV2Artifact();
        artifact.setType("DOCUMENT");
        artifact.setTitle("分析报告");
        artifact.setDownloadUrl("javascript:alert(1)");

        assertFalse(validator.isValid(artifact));
        artifact.setDownloadUrl("/agent/artifacts/9/download");
        artifact.setPreviewUrl("https://office.example.com/preview/9");
        assertTrue(validator.isValid(artifact));
    }

    private AgentV2Artifact chart(Map<String, Object> payload)
    {
        AgentV2Artifact artifact = new AgentV2Artifact();
        artifact.setType("CHART");
        artifact.setTitle("物料分类统计");
        artifact.setPayload(payload);
        return artifact;
    }
}
