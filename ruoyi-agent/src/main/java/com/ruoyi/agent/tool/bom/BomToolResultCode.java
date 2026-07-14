package com.ruoyi.agent.tool.bom;

import com.ruoyi.agent.tool.shared.AgentToolResultCode;

/** BOM 查询工具结果码。 */
public enum BomToolResultCode implements AgentToolResultCode {
    BOM_QUERY_SUCCESS, BOM_NOT_FOUND, BOM_AMBIGUOUS, BOM_VERSION_NOT_FOUND, BOM_VERSION_AMBIGUOUS, BOM_STRUCTURE_SUCCESS;

    @Override
    public String code() {
        return name();
    }
}
