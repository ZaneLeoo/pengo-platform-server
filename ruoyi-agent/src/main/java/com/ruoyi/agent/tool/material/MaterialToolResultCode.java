package com.ruoyi.agent.tool.material;

import com.ruoyi.agent.tool.shared.AgentToolResultCode;

/** 物料查询工具结果码。 */
public enum MaterialToolResultCode implements AgentToolResultCode {
    MATERIAL_QUERY_SUCCESS, MATERIAL_NOT_FOUND;

    /** 返回枚举名称作为稳定结果码。 */
    @Override
    public String code() {
        return name();
    }
}
