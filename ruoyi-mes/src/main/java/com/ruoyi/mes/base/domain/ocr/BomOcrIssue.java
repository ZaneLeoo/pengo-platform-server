package com.ruoyi.mes.base.domain.ocr;

/** BOM OCR 识别问题。 */
public class BomOcrIssue
{
    /** 问题等级：warning 警告，error 错误。 */
    private String level;

    /** 问题编码，例如 PARTIAL_ROWS、MISSING_QUANTITY。 */
    private String code;

    /** 问题描述。 */
    private String message;

    /** 关联的明细行序号；全局问题为空。 */
    private Integer lineNo;

    /** 关联字段名，例如 items、quantity、drawingNo。 */
    private String field;

    public BomOcrIssue() { }

    public BomOcrIssue(String level, String code, String message, Integer lineNo, String field)
    {
        this.level = level;
        this.code = code;
        this.message = message;
        this.lineNo = lineNo;
        this.field = field;
    }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getLineNo() { return lineNo; }
    public void setLineNo(Integer lineNo) { this.lineNo = lineNo; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
}
