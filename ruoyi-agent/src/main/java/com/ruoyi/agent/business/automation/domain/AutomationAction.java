package com.ruoyi.agent.business.automation.domain;

import java.util.Date;
import lombok.Data;

/** AI 自动化动作幂等记录。 */
@Data
public class AutomationAction
{
    private Long id;
    private String actionKey;
    private String actionType;
    private Long userId;
    private String status;
    private Long targetId;
    private String targetCode;
    private Date createTime;
    private Date updateTime;
}
