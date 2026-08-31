# V2 预算与成本 02：项目分类预算技术设计

## 1. 数据模型

新增 `pm_project_budget_line`，唯一键为 `(project_id, cost_category_id)`。保存类别编码、名称、路径快照、人民币含税预算金额、测算依据、排序和审计字段。预算头继续使用 `pm_project.budget_required/budget_amount/budget_description`。

项目预算版本依托立项审批快照和 `pm_project_plan_baseline.snapshot_json`，不增加独立版本表。

## 2. 服务边界

- `ProjectBudgetService` 负责查询、草稿替换、提交校验、变更模拟和应用。
- 草稿替换与项目主表修改处于同一事务。
- 提交校验使用数据库当前成本类别；历史展示使用预算行快照。
- 成本类别引用计数接入预算明细表。

## 3. 接口

- `GET /projectManagement/project/{id}/budget`：项目预算详情及统计。
- `PUT /projectManagement/project/{id}/budget`：仅草稿/已退回立项材料保存。
- 项目新增、编辑响应和立项快照增加 `budgetLines`。
- 项目变更接口增加 `PROJECT_BUDGET` 模块，沿用现有变更项数据结构。

## 4. 变更与基线

- `PROJECT_BUDGET_HEADER/UPDATE` 保存预算头差异。
- `PROJECT_BUDGET_LINE` 支持 ADD、UPDATE、DELETE；目标 ID 为预算行 ID，新增目标使用成本类别 ID。
- 保存、提交和应用均验证字段白名单；提交和应用模拟最终集合并执行总额、重复类别、类别状态校验。
- 基线快照增加 `projectBudget`，比较时以 `costCategoryId` 为标识。

## 5. 兼容性

存量项目不自动生成分类明细；需要预算但没有明细的项目展示“待补充”，下一次预算变更时补齐。当前临时关闭的项目管理权限注解保持不变。

