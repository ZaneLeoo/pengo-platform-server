# V2 预算与成本 04：实际成本登记技术设计

## 1. 数据模型

新增 `pm_project_actual_cost`（实现时对应 `sql/46_project_actual_cost.sql`）：

- 主键 `actual_cost_id`；
- 归属字段 `project_id`、`work_package_id`（为空表示项目级成本）、`cost_category_id`；
- 快照字段 `category_code`、`category_name`、`category_path`；
- 业务字段 `actual_amount`（decimal(16,2)）、`occur_date`（date）、`description`；
- 审计字段：标准创建、修改，以及 `correction_no`（更正次数，供前端回显）。
- 索引：`project_id`、`cost_category_id`、`work_package_id`、`occur_date`；
- 外键：`pm_project(project_id)`、`pm_project_wbs_node(wbs_id)`（可空）、`pm_cost_category(cost_category_id)`。

金额、发生日期、事由在服务层校验；数据库约束负责来源与删除一致性。

## 2. 服务规则

`ProjectActualCostService` 负责查询、登记、更正、删除与统计：

- 登记与更正均校验：项目状态、类别有效末级且项目存在该项目分类预算、工作包归属合法性、金额与日期、预算上限、停用类别限制。
- 统计：项目级合计、按成本类别的项目合计、工作包级合计；`ProjectBudgetSummary` 向后兼容新增 `actualCostAmount`、`remainingBudgetAmount`、`executionRate`。
- 删除与调减写审计，不触发审批。
- 成本类别引用计数扩展：`CostCategoryServiceImpl.referenceCount` 增加 `actualCostMapper.countByCategoryId`。
- 与变更服务联动（`ProjectPlanChangeServiceImpl.validateFinalBudget`）：
  - 项目分类预算调减后不得低于该类别已发生成本；
  - 删除项目分类预算前该类别无实际成本；
  - 取消项目预算前无任何实际成本；
  - 工作包预算调减后不得低于该工作包该类别已发生成本；
  - 删除工作包预算前该工作包无实际成本。

## 3. 接口

- `GET /projectManagement/project/{id}/actual-costs`：项目实际成本明细，支持类别与工作包过滤。
- `POST /projectManagement/project/{id}/actual-costs`：登记项目级或工作包级成本。
- `PUT /projectManagement/project/{projectId}/actual-costs/{id}`：更正金额、发生日期、事由、工作包归属。
- `DELETE /projectManagement/project/{projectId}/actual-costs/{id}`：删除。
- `GET /projectManagement/project/{id}/budget-execution`：分类执行汇总（项目分类预算、已分配、待分配、实际成本、剩余、执行率）。
- 现有 `GET /projectManagement/project/{id}/budget` 保持契约，仅新增统计字段。

## 4. 菜单与权限

- 页面与按钮权限编码：`projectManagement:actualCost:query/add/edit/remove`。
- 当前项目管理控制器权限注解统一临时关闭，保留对应注解注释与权限编码，后续恢复无需重构接口。
- 服务层数据范围与业务能力校验不可绕过。

## 5. 兼容性

- 新表独立，无存量数据迁移。
- 计划基线快照不包含实际成本；版本比较仍只比较计划预算。
- 既有预算、工作包预算与成本类别接口契约不变，仅新增字段与错误分支。
- 历史项目不需要实际成本时无需补数据；未启动项目不可登记。
