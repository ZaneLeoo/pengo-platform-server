package com.ruoyi.agent.application.source;

/** 可发送给前端并可落库的知识来源摘要。 */
public class AgentSource
{
    private final String id;
    private final String title;
    private final String sourceType;
    private final String documentName;
    private final String datasetName;
    private final String content;
    private final Double score;
    private final Integer position;
    private final String segmentId;
    private final Integer page;

    public AgentSource(String id, String title, String sourceType, String documentName, String datasetName,
        String content, Double score, Integer position, String segmentId, Integer page)
    {
        this.id = id;
        this.title = title;
        this.sourceType = sourceType;
        this.documentName = documentName;
        this.datasetName = datasetName;
        this.content = content;
        this.score = score;
        this.position = position;
        this.segmentId = segmentId;
        this.page = page;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSourceType() { return sourceType; }
    public String getDocumentName() { return documentName; }
    public String getDatasetName() { return datasetName; }
    public String getContent() { return content; }
    public Double getScore() { return score; }
    public Integer getPosition() { return position; }
    public String getSegmentId() { return segmentId; }
    public Integer getPage() { return page; }
}
