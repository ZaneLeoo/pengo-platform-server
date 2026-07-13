package com.ruoyi.agent.tool.supplier;

import com.ruoyi.agent.tool.shared.AgentToolMeta;
import com.ruoyi.agent.tool.shared.AgentToolResult;
import com.ruoyi.agent.tool.shared.AgentToolResults;
import com.ruoyi.mes.base.domain.Supplier;
import com.ruoyi.mes.base.service.ISupplierService;
import java.util.List;
import org.springframework.stereotype.Service;

/** 将供应商主数据服务适配为稳定的 Agent 工具协议。 */
@Service
public class SupplierToolService {
    private static final String ACTIVE_STATUS = "NORMAL";
    private static final int MAX_RESULT_SIZE = 50;
    private final ISupplierService supplierService;

    public SupplierToolService(ISupplierService supplierService) {
        this.supplierService = supplierService;
    }

    /** 查询供应商并限制模型上下文中的返回数量。 */
    public AgentToolResult<List<SupplierToolItem>> query(SupplierToolQuery request) {
        SupplierToolQuery query = request == null ? new SupplierToolQuery(null, false) : request;
        String status = query.getIncludeDisabled() ? null : ACTIVE_STATUS;
        List<Supplier> matched = supplierService.selectListForAgent(query.getKeyword(), status);
        List<SupplierToolItem> data = matched.stream().limit(MAX_RESULT_SIZE).map(this::toItem).toList();
        AgentToolMeta meta = AgentToolMeta.collection(matched.size(), matched.size() > MAX_RESULT_SIZE);
        if (data.isEmpty()) {
            return AgentToolResults.noResult(SupplierToolResultCode.SUPPLIER_NOT_FOUND,
                    "未找到符合条件的供应商", data, meta);
        }
        return AgentToolResults.success(SupplierToolResultCode.SUPPLIER_QUERY_SUCCESS,
                "供应商查询成功", data, meta);
    }

    /** 转换为工具最小输出模型。 */
    private SupplierToolItem toItem(Supplier supplier) {
        return new SupplierToolItem(supplier.getId(), supplier.getSupplierCode(), supplier.getSupplierName(),
                supplier.getCurrency(), supplier.getTaxRate(), supplier.getStatus());
    }
}
