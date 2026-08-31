# V2 预算与成本 01：成本类别技术设计

## 1. 数据模型

新增 `pm_cost_category`：

- 主键 `cost_category_id`；树字段 `parent_id`、`ancestors`、`level_no`。
- 业务字段 `category_code`、`category_name`、`allow_manual_entry`。
- 预留字段 `finance_account_code`、`finance_account_name`。
- 控制字段 `system_flag`、`status`、`sort_order`、`description`。
- 标准创建、修改审计字段。

数据库约束包含编码唯一索引、父级查询索引和状态索引。同级名称唯一由服务层以去空格后的名称校验，避免不同数据库排序规则造成行为差异。

`level_no`、`ancestors`、`leaf` 和有效状态不接受客户端任意指定：前两项由服务层计算，后两项由查询结果计算。

## 2. 服务规则

- 新增和修改均校验父级存在、父级链无循环、目标层级不超过 3。
- 编码统一转大写并校验 `^[A-Z][A-Z0-9_]*$`。
- 编码保存后锁定；系统预置记录的父级锁定。
- 移动自定义类别时同步更新自身及所有后代的 `ancestors` 和 `level_no`，移动后整棵子树不得超过三级。
- 已被引用的类别禁止移动和新增子级；引用检查通过统一服务入口，后续预算和成本表接入时扩展。
- 删除前检查系统预置、子级和业务引用。
- 有子级的类别强制按汇总类别处理，不能作为 options 返回。
- 有任一停用祖先的类别 `effectiveStatus=1`。

## 3. 接口

- `GET /projectManagement/cost-category/tree`：管理树及能力字段。
- `GET /projectManagement/cost-category/options`：有效末级选择项。
- `GET /projectManagement/cost-category/{id}`：详情。
- `GET /projectManagement/cost-category/{id}/usage`：引用和操作能力。
- `POST /projectManagement/cost-category`：新增。
- `PUT /projectManagement/cost-category`：修改。
- `PUT /projectManagement/cost-category/{id}/status`：启停。
- `DELETE /projectManagement/cost-category/{id}`：删除。

树节点额外返回 `leaf`、`effectiveStatus`、`fullPath`、`canEdit`、`canDelete`、`canAddChild`、`readonlyReason` 和 `children`。

## 4. 菜单与权限

- 页面权限 `projectManagement:costCategory:list`。
- 按钮权限 `query/add/edit/changeStatus/remove`。
- SQL 同时创建表、预置类别、菜单、按钮权限和默认项目管理角色授权；预置与菜单插入可重复执行。
- 当前项目管理控制器的权限注解处于统一临时关闭状态，仍保留对应注解注释与权限编码，后续统一恢复时无需重构接口。

## 5. 兼容性

- 本阶段没有既有业务引用和数据迁移。
- 财务映射仅保存文本，不校验外部财务主数据。
- 后续预算表通过 `cost_category_id` 逻辑引用；历史业务同时保存类别编码、名称和路径快照。

