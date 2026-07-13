package com.ruoyi.agent.business.automation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 主数据候选项的安全展示字段。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutomationCandidateOption {
    private Long id;
    private String code;
    private String name;
    private String spec;
    private String model;
    private String unit;

}
