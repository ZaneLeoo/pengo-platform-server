package com.ruoyi.agent.application.source;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SourceExtractorTest
{
    private final SourceExtractor extractor = new SourceExtractor();

    @Test
    void shouldExtractSourcesFromMessageEndRetrieverResources()
    {
        Map<String, Object> metadata = Map.of("retriever_resources", List.of(Map.of(
            "position", 1,
            "dataset_name", "sale.txt...",
            "document_name", "sale.txt",
            "segment_id", "seg-1",
            "score", 0.94,
            "content", "二、客户跟进要求\n销售人员应在客户首次咨询后 24 小时内完成首次跟进。")));

        List<AgentSource> sources = extractor.extractFromMetadata(metadata);

        assertEquals(1, sources.size());
        assertEquals("seg-1", sources.get(0).getId());
        assertEquals("sale.txt", sources.get(0).getTitle());
        assertEquals("knowledge", sources.get(0).getSourceType());
        assertEquals(0.94, sources.get(0).getScore());
        assertEquals(1, sources.get(0).getPosition());
    }

    @Test
    void shouldExtractSourcesFromKnowledgeRetrievalNodeOutputs()
    {
        Map<String, Object> item = Map.of(
            "title", "sale.txt",
            "content", "客户跟进要求",
            "metadata", Map.of(
                "document_id", "doc-1",
                "document_name", "sale.txt",
                "dataset_name", "销售制度",
                "segment_id", "seg-2",
                "position", 2));
        Map<String, Object> data = Map.of("outputs", Map.of("result", List.of(item)));

        List<AgentSource> sources = extractor.extractFromNodeData(data);

        assertEquals(1, sources.size());
        assertEquals("seg-2", sources.get(0).getId());
        assertEquals("销售制度", sources.get(0).getDatasetName());
        assertEquals("客户跟进要求", sources.get(0).getContent());
    }
}
