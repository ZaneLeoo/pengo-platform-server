# V2 BC05 技术设计：采购入库自动归集项目实际成本

## 数据改动

采购订单头增加 `project_id`、`project_code`、`project_name`、`cost_category_id` 及类别编码/名称/路径快照；到货/入库明细增加相同归集快照以及来源单价、行金额。入库行金额是自动计算的含税金额。

`pm_project_actual_cost` 增加来源和冲销字段，并以 `(source_type, source_line_id)` 保证采购入库重复审核不会重复归集。

## 模块边界

`ruoyi-mes` 不直接依赖项目管理模块。它发布两个同步领域事件：订单项目归集校验事件、入库审核/弃审事件；`ruoyi-project-management` 依赖 `ruoyi-mes` 并监听事件，完成预算类别校验、成本写入和冲销。异常在同一事务中抛出，采购审批/弃审整体回滚。

## 成本归集算法

1. 入库审核循环每条明细，若 `project_id` 为空则跳过。
2. 校验项目处于执行中或暂停中、类别仍为项目有效预算类别、入库金额大于零且分类预算不超额。
3. 写入一条 `PURCHASE_INBOUND` / `EFFECTIVE` 实际成本：项目级（`work_package_id = null`）、发生日期为入库日期、金额取入库行金额、说明为“采购入库：{入库单号} / {物料编码}”。
4. 入库弃审按入库明细 ID 将对应有效成本更新为 `REVERSED`，写入操作人与原因“入库单弃审”。
5. 所有总额 SQL 过滤 `cost_status = 'EFFECTIVE'`。

## 接口

- `GET /projectManagement/project/{projectId}/budget/category-options`：采购订单选择可用类别。
- 采购订单保存/审核通过同步事件做服务端项目预算校验，不能依赖前端。
- 实际成本查询响应补充来源字段；采购来源返回入库单号和行号。

## 迁移与兼容

迁移脚本为 `Backend/sql/ry_20260901_purchase_project_cost.sql`，使用 information_schema 条件判断，支持重复执行。存量实际成本回填为 `MANUAL` / `EFFECTIVE`。存量采购单据不回填项目归集字段。
