package com.ruoyi.projectmanagement.budget.listener;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mes.purchase.domain.PurchaseInboundLine;
import com.ruoyi.mes.purchase.event.PurchaseInboundCostEvent;
import com.ruoyi.mes.purchase.event.PurchaseOrderProjectValidationEvent;
import com.ruoyi.projectmanagement.budget.domain.ProjectActualCost;
import com.ruoyi.projectmanagement.budget.domain.ProjectBudgetLine;
import com.ruoyi.projectmanagement.budget.mapper.ProjectActualCostMapper;
import com.ruoyi.projectmanagement.budget.mapper.ProjectBudgetMapper;
import com.ruoyi.projectmanagement.project.domain.ProjectInfo;
import com.ruoyi.projectmanagement.project.mapper.ProjectInfoMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** 将采购入库行同步归集为项目级实际成本。 */
@Component
public class PurchaseInboundCostListener {
    private static final String SOURCE = "PURCHASE_INBOUND";
    private final ProjectActualCostMapper actualCostMapper;
    private final ProjectBudgetMapper budgetMapper;
    private final ProjectInfoMapper projectMapper;

    public PurchaseInboundCostListener(
            ProjectActualCostMapper actualCostMapper,
            ProjectBudgetMapper budgetMapper,
            ProjectInfoMapper projectMapper) {
        this.actualCostMapper = actualCostMapper;
        this.budgetMapper = budgetMapper;
        this.projectMapper = projectMapper;
    }

    @EventListener
    public void validateOrder(PurchaseOrderProjectValidationEvent event) {
        var order = event.order();
        if (order.getProjectId() == null) {
            if (order.getCostCategoryId() != null) throw new ServiceException("未关联项目时不能选择成本类别");
            return;
        }
        ProjectInfo project = projectMapper.selectProjectInfoById(order.getProjectId());
        if (project == null
                || !("ACTIVE".equals(project.getStatus()) || "PAUSED".equals(project.getStatus())))
            throw new ServiceException("采购订单关联项目必须处于执行中或暂停中");
        ProjectBudgetLine budget =
                budgetMapper.selectByProjectId(order.getProjectId()).stream()
                        .filter(item -> item.getCostCategoryId().equals(order.getCostCategoryId()))
                        .findFirst()
                        .orElseThrow(() -> new ServiceException("采购订单成本类别必须是项目有效分类预算类别"));
        order.setProjectCode(project.getProjectCode());
        order.setProjectName(project.getProjectName());
        order.setCategoryCode(budget.getCategoryCode());
        order.setCategoryName(budget.getCategoryName());
        order.setCategoryPath(budget.getCategoryPath());
    }

    @EventListener
    public void handle(PurchaseInboundCostEvent event) {
        for (PurchaseInboundLine line : event.lines()) {
            if (line.getProjectId() == null) continue;
            if (event.reversed()) {
                actualCostMapper.reverseBySourceLineId(
                        SOURCE, line.getId(), event.operator(), "采购入库单弃审");
            } else {
                post(event, line);
            }
        }
    }

    private void post(PurchaseInboundCostEvent event, PurchaseInboundLine line) {
        ProjectActualCost existing = actualCostMapper.selectBySourceLineId(SOURCE, line.getId());
        if (existing != null && !"REVERSED".equals(existing.getCostStatus()))
            throw new ServiceException("采购入库明细已归集项目成本");
        ProjectInfo project = projectMapper.selectProjectInfoById(line.getProjectId());
        if (project == null
                || !("ACTIVE".equals(project.getStatus()) || "PAUSED".equals(project.getStatus())))
            throw new ServiceException("采购入库关联项目必须处于执行中或暂停中");
        ProjectBudgetLine budget =
                budgetMapper.selectByProjectId(line.getProjectId()).stream()
                        .filter(item -> item.getCostCategoryId().equals(line.getCostCategoryId()))
                        .findFirst()
                        .orElseThrow(() -> new ServiceException("采购入库成本类别不是项目有效分类预算"));
        BigDecimal amount = line.getInboundAmount();
        if (amount == null || amount.signum() <= 0) throw new ServiceException("采购入库金额必须大于0");
        BigDecimal categoryActual =
                actualCostMapper.categoryTotal(line.getProjectId(), line.getCostCategoryId());
        if (categoryActual.add(amount).compareTo(budget.getBudgetAmount()) > 0)
            throw new ServiceException("采购入库归集后将超过项目分类预算");
        if (existing != null) {
            if (actualCostMapper.restoreBySourceLineId(SOURCE, line.getId(), event.operator()) != 1)
                throw new ServiceException("采购入库历史成本恢复失败");
            return;
        }
        ProjectActualCost cost = new ProjectActualCost();
        cost.setProjectId(line.getProjectId());
        cost.setCostCategoryId(line.getCostCategoryId());
        cost.setCategoryCode(line.getCategoryCode());
        cost.setCategoryName(line.getCategoryName());
        cost.setCategoryPath(line.getCategoryPath());
        cost.setActualAmount(amount);
        cost.setOccurDate(LocalDate.parse(event.inbound().getInboundDate()));
        cost.setDescription(
                "采购入库：" + event.inbound().getInboundCode() + " / " + line.getMaterialCode());
        cost.setSourceType(SOURCE);
        cost.setSourceLineId(line.getId());
        cost.setSourceDocumentNo(event.inbound().getInboundCode());
        cost.setSourceLineNo(String.valueOf(line.getLineNo()));
        cost.setCostStatus("EFFECTIVE");
        cost.setCreateBy(event.operator());
        actualCostMapper.insert(cost);
    }
}
