package com.ruoyi.mes.base.domain;

import java.io.Serializable;

/**
 * BOM完整性检查问题。
 *
 * @author ruoyi
 */
public class BomCheckIssue implements Serializable {

    private static final long serialVersionUID = 1L;

    private String level;
    private String code;
    private String message;
    private Long itemId;
    private Integer lineNo;
    private String componentItemCode;

    public BomCheckIssue() {
    }

    public BomCheckIssue(String level, String code, String message) {
        this.level = level;
        this.code = code;
        this.message = message;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Integer getLineNo() {
        return lineNo;
    }

    public void setLineNo(Integer lineNo) {
        this.lineNo = lineNo;
    }

    public String getComponentItemCode() {
        return componentItemCode;
    }

    public void setComponentItemCode(String componentItemCode) {
        this.componentItemCode = componentItemCode;
    }
}
