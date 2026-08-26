package com.ruoyi.agent.tool.purchasequote;

import com.ruoyi.agent.tool.shared.AgentToolResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 供应商报价比较工具结果码。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseQuoteToolResultCode implements AgentToolResultCode {
    private String code;

    /** 返回稳定结果码。 */
    @Override
    public String code() {
        return code;
    }
}
