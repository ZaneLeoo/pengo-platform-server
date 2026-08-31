# 项目管理数据库脚本

本目录记录项目管理模块的数据库演进历史，目前不是一套可以在空库中按编号全部执行的一键安装脚本。

## 执行原则

- 执行前记录应用提交和数据库现状，并完成可恢复备份。
- 已部署环境只执行从当前版本到目标版本所需的脚本。
- 新环境优先使用经过验证的数据库基线或备份恢复，不拼接全部历史脚本。
- 演示数据只用于本地或测试环境。
- 已发布脚本不直接修改；修复通过新增脚本完成。

## 高风险提示

| 脚本 | 风险 |
| --- | --- |
| `16_project_phase.sql` | 已废弃的阶段模型，当前环境不要执行 |
| `20_project_plan_demo.sql` | 旧人员身份下的演示项目，生产不要执行 |
| `30_project_management_test_accounts.sql` | 创建测试账号，生产不要执行 |
| `31_ivs_project_team_accounts.sql` | 包含固定项目和测试账号假设，生产不要执行 |
| `33_ivs_project_plan_demo_layers.sql` | 使用固定 `project_id=3`，生产不要执行 |
| `35_project_user_identity_migration.sql` | 一次性身份迁移第一步，不是普通安装脚本 |
| `36_remove_project_person_archive.sql` | 会删除项目管理业务数据，必须专项备份和审批 |
| `37_drop_project_person_archive.sql` | 删除旧 `pm_person` 表，只能在迁移验收后执行 |
| `38_seed_product_rnd_initiation_demo.sql` | 产品研发立项样例，生产不要执行 |

## 演进阶段

| 阶段 | 脚本 | 说明 |
| --- | --- | --- |
| 基础档案 | 01–06 | 旧人员档案、项目分类、项目主表和菜单 |
| 早期执行模型 | 07–17 | 统一执行项、正式交付、生命周期、团队和已废弃阶段模型 |
| 当前领域模型 | 18–28 | 立项、WBS、工作包、任务、交付物类型、专业角色和附件 |
| 身份过渡与联调 | 29–34 | 人员账号绑定、测试账号、菜单恢复、样例计划和任务操作日志 |
| 系统用户统一 | 35–38 | `sys_user` 身份迁移、旧人员表清理和新版样例项目 |

## 脚本分类

### 当前结构与功能演进

- `03_project_category.sql`、`05_project_info.sql`
- `13_project_deliverable_closure.sql`–`15_project_team.sql`
- `18_project_initiation.sql`–`19_project_plan_domain_refactor.sql`
- `21_work_package_deliverable_required.sql`–`23_deliverable_type_snapshot.sql`
- `25_professional_role.sql`
- `28_project_initiation_attachment.sql`
- `34_task_operation_log.sql`

这些脚本也可能依赖更早结构，不能仅凭此列表在空库中执行。

### 菜单与权限

- `02_project_management_menu.sql`
- `04_project_category_menu.sql`
- `06_project_info_menu.sql`
- `08_project_execution_menu.sql`–`09_rename_project_menu.sql`
- `17_menu_cleanup.sql`
- `24_deliverable_type_menu.sql`
- `26_professional_role_menu.sql`–`27_project_menu_order.sql`
- `32_enable_task_management_menu.sql`

菜单脚本中存在历史 ID 和环境状态假设，执行前应按菜单名称、路径和权限编码核对目标环境。

### 历史或已废弃

- `01_project_person.sql`：旧 `pm_person` 人员档案，当前业务已使用 `sys_user`。
- `07_project_execution.sql`、`11_work_item_task_link.sql`、`12_task_deliverable_requirement.sql`：旧统一执行项模型。
- `16_project_phase.sql`：阶段模型，已被 `19_project_plan_domain_refactor.sql` 取消。

保留这些脚本是为了追溯已部署环境的历史，不代表当前模型仍使用对应表。

### 演示与测试数据

- `10_automation_equipment_demo.sql`
- `20_project_plan_demo.sql`
- `30_project_management_test_accounts.sql`
- `31_ivs_project_team_accounts.sql`
- `33_ivs_project_plan_demo_layers.sql`
- `38_seed_product_rnd_initiation_demo.sql`

### 一次性身份迁移

`29` 是旧人员档案与系统用户绑定的过渡方案；`35 → 36 → 37` 将项目身份彻底切换到 `sys_user`。这组脚本只能根据目标环境的实际迁移阶段选择，不能重复用于普通升级。

## 已部署环境升级

V2 新增脚本按顺序执行：

- `39_project_issue_closure.sql`：问题提出人、逾期和活动时间线，并补齐问题按钮权限。
- `40_project_workflow.sql`：审批定义版本、实例、任务、候选人与事件表，并接入立项和交付物。
- `41_project_plan_change.sql`：项目计划基线、变更单、变更项、附件和业务审计；依赖 `40` 的审批实例表。
- `42_project_plan_change_permissions.sql`：项目变更查询、维护、确认应用按钮权限；依赖 `41` 的 V2 能力。
- `43_project_cost_category.sql`：V2 预算与成本阶段 1 的成本类别、预置数据、菜单和权限；可重复执行。
- `44_project_budget.sql`：V2 预算与成本阶段 2 的项目分类预算明细和预备费类别；可重复执行。

这些脚本面向新的 V2 结构，不承担旧问题或旧审批数据迁移；执行前必须备份。`41` 不可重复执行，`42` 可重复执行。

1. 记录当前 schema、应用提交和已执行脚本。
2. 备份并验证恢复能力。
3. 在同版本数据库副本上演练目标脚本。
4. 核对影响表、影响行数、菜单和权限。
5. 执行项目管理 V1 验收中的相关回归。
6. 在维护窗口升级；失败时按备份恢复，不在生产现场修改历史脚本后重跑。

## 待补工作

需要建立一份“当前结构基线脚本”，用于空库安装。它应包含：

- 当前全部项目管理表、索引和字段注释
- 基于 `sys_user` 的人员身份，不包含旧 `pm_person`
- 当前菜单和按钮权限
- 必要的分类、项目角色、专业角色和交付物类型基础数据
- 不包含测试账号、固定项目 ID 或演示项目

基线脚本只有在空库自动执行并与 Mapper 字段逐项核对通过后，才能作为正式安装入口。

## 新脚本头部要求

```sql
-- 类型：结构 / 菜单 / 迁移 / 样例
-- 适用版本：当前版本 → 目标版本
-- 前置条件：依赖表、字段或脚本
-- 可重复执行：是 / 否
-- 数据风险：无 / 更新 / 删除
-- 回退方式：回退 SQL 或备份恢复
```
