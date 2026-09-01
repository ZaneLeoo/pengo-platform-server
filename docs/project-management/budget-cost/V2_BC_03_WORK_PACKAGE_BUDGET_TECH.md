# V2 预算与成本 03：工作包预算分解技术设计

## 1. 数据模型

新增 `pm_work_package_budget_line`：

| 字段 | 说明 |
| --- | --- |
| `work_package_budget_line_id` | 主键 |
| `project_id`、`work_package_id` | 项目与工作包归属 |
| `cost_category_id` | 成本类别逻辑引用 |
| `category_code`、`category_name`、`category_path` | 类别历史快照 |
| `budget_amount` | 人民币含税预算金额，`decimal(16,2)` |
| `estimation_basis` | 测算依据 |
| `sort_order`、审计字段 | 常规排序与审计 |

唯一键为 `(work_package_id, cost_category_id)`；索引至少覆盖 `(project_id, cost_category_id)` 与 `work_package_id`。外键引用项目、工作包与成本类别。项目删除时级联删除；成本类别受引用保护。

不新增独立的工作包预算版本表。预算行随 `pm_project_plan_baseline.snapshot_json` 持久化，工作包预算当前态直接读本表。

## 2. 服务与校验

新增 `ProjectWorkPackageBudgetService`，职责：

- 查询项目分类预算的分配汇总、工作包预算明细与单工作包预算详情；
- 查询工作包预算候选对象与当前能力；
- 为 `ProjectPlanChangeService` 提供提交前及应用前的最终态模拟校验；
- 支撑成本类别引用计数。

校验在以下三个层面重复执行：

1. 编辑器候选过滤与即时提示；
2. 变更单保存/提交时的服务端白名单与最终态模拟；
3. 审批通过后的确认应用事务内最终校验。

模拟应用顺序固定为：`PROJECT_BUDGET` → `WBS` → `WORK_PACKAGE_BUDGET` → 其他模块。模拟集合以 `projectId + workPackageId + costCategoryId` 唯一识别，避免把尚未应用的同单变更误判为重复或超额。

## 3. 项目变更集成

新增模块 `WORK_PACKAGE_BUDGET`：

- 目标类型 `WORK_PACKAGE_BUDGET_LINE`；
- `ADD` 的 `afterJson` 白名单：`workPackageId`、`costCategoryId`、`budgetAmount`、`estimationBasis`；
- `UPDATE` 白名单：`budgetAmount`、`estimationBasis`；
- `DELETE` 的 `afterJson` 必须为空；
- `beforeJson` 从当前预算行取字段快照，`afterJson` 仅保留发生变化的业务字段。

`PROJECT_BUDGET` 的最终态校验需扩展：预算头取消、分类预算删除或金额调减时，必须连同工作包预算集合一起模拟，防止留下超分配或孤儿预算。

变更应用成功后，基线快照新增 `workPackageBudget` 集合；比较以 `(workPackageId, costCategoryId)` 作为业务键，显示工作包、类别、金额、依据的新增、修改、删除差异。

## 4. 接口

| 接口 | 用途 |
| --- | --- |
| `GET /projectManagement/project/{id}/budget` | 扩展返回项目分类预算分配汇总。 |
| `GET /projectManagement/project/{id}/work-package-budgets` | 查询项目全部工作包预算及汇总。 |
| `GET /projectManagement/project/{projectId}/work-package/{workPackageId}/budget` | 工作包详情抽屉预算页签。 |
| `GET /projectManagement/plan-change/{projectId}/work-package-budget/options` | 变更编辑器获取工作包、可用成本类别、项目分类预算和待分配信息。 |

不提供工作包预算的直接新增、修改或删除接口。所有写入继续复用 `POST /projectManagement/plan-change`、提交、撤回与确认应用接口。

## 5. 前端实现

- `ProjectBudgetManager` 增加分类分配汇总和工作包分配列表；
- 工作包详情抽屉新增“预算”页签组件，使用只读查询接口；
- 项目变更 `editor.vue` 增加 `WORK_PACKAGE_BUDGET` 分支：树选择工作包、按类别上限渲染可用额度与输入；
- `projectPlanChange.js` 补模块名、字段映射、差异展示标签；
- 审批详情、变更详情和基线比较统一复用字段级 diff 渲染。

## 6. 兼容性与迁移

- 新表和菜单/接口 SQL 必须幂等。
- 存量项目不自动生成工作包预算；项目预算页签显示“尚未分配到工作包”。
- 存量已完成工作包保持无预算并只读；不补历史金额。
- 成本类别的引用保护扩展为项目分类预算与工作包预算两类引用。
