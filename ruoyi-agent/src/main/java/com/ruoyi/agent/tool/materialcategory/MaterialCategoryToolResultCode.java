package com.ruoyi.agent.tool.materialcategory;

import com.ruoyi.agent.tool.shared.AgentToolResultCode;

/** 物料分类查询工具结果码。 */
public enum MaterialCategoryToolResultCode implements AgentToolResultCode {
    MATERIAL_CATEGORY_QUERY_SUCCESS, MATERIAL_CATEGORY_NOT_FOUND;

    /** 返回枚举名称作为稳定结果码。 */
    @Override
    public String code() {
        return name();
    }
}
