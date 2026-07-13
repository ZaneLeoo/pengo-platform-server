package com.ruoyi.mes.base.domain.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** BOM OCR 草稿导入正式 BOM 后的结果。 */
public class BomImportApplyResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 导入草稿 ID。 */
    private Long importId;

    /** 目标 BOM 版本 ID。 */
    private Long bomVersionId;

    /** 成功导入行数。 */
    private int importedCount;

    /** 跳过行数。 */
    private int skippedCount;

    /** 跳过的明细行和原因。 */
    private List<BomImportSkippedItem> skippedItems = new ArrayList<>();

    public Long getImportId() {
        return importId;
    }
    public void setImportId(Long importId) {
        this.importId = importId;
    }

    public Long getBomVersionId() {
        return bomVersionId;
    }
    public void setBomVersionId(Long bomVersionId) {
        this.bomVersionId = bomVersionId;
    }

    public int getImportedCount() {
        return importedCount;
    }
    public void setImportedCount(int importedCount) {
        this.importedCount = importedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }
    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }

    public List<BomImportSkippedItem> getSkippedItems() {
        return skippedItems;
    }
    public void setSkippedItems(List<BomImportSkippedItem> skippedItems) {
        this.skippedItems = skippedItems;
    }

    public void addSkippedItem(Integer lineNo, String itemName, String reason) {
        BomImportSkippedItem item = new BomImportSkippedItem();
        item.setLineNo(lineNo);
        item.setItemName(itemName);
        item.setReason(reason);
        skippedItems.add(item);
        skippedCount = skippedItems.size();
    }

    /** 被跳过的 OCR 明细行。 */
    public static class BomImportSkippedItem implements Serializable {
        private static final long serialVersionUID = 1L;

        /** OCR 行号。 */
        private Integer lineNo;

        /** OCR 子件名称。 */
        private String itemName;

        /** 跳过原因。 */
        private String reason;

        public Integer getLineNo() {
            return lineNo;
        }
        public void setLineNo(Integer lineNo) {
            this.lineNo = lineNo;
        }

        public String getItemName() {
            return itemName;
        }
        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public String getReason() {
            return reason;
        }
        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
