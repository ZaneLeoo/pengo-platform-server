package com.ruoyi.agent.tool.bom;

import java.math.BigDecimal;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** BOM 版本工具输出。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BomVersionToolItem {
    private Long id;
    private String code;
    private String name;
    private String description;
    private BigDecimal baseQuantity;
    private String usageType;
    private Date effectiveDate;
    private Date expireDate;
    private String status;
    private String approvalStatus;
    private Boolean defaultVersion;
}
