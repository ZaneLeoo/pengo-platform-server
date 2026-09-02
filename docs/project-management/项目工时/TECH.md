# V2 预算与成本 05：项目工时技术方案

## 1. 技术原则

工时事实、审批状态、单价快照和实际成本必须分别可追溯。有效人员单价与可用内部人工预算是工时保存的前置条件；审批归档的有效工时必须同步形成实际成本，不能进入待处理队列。

人工成本不能复用采购来源专用字段。它与采购成本同属实际成本结果，但来源必须使用通用、可扩展的来源关联模型。

## 2. 表设计

### 2.1 周工时单 pm_project_work_hours_sheet

字段：sheet_id、user_id、人员快照、week_start_date、week_end_date、status、late_report_reason、workflow_instance_id、submit_time、archive_time、标准审计字段。

- 唯一索引 (user_id, week_start_date)，保证一人一周一单。
- 状态：DRAFT、IN_APPROVAL、RETURNED、ARCHIVED。
- week_start_date 必须是周一，week_end_date 固定为其后六日。

### 2.2 工时明细 pm_project_work_hours_entry

字段分组：

- 关联：entry_id、sheet_id、project_id、work_package_id、task_id、report_user_id；
- 快照：项目、工作包、任务、人员的编码、名称、部门；
- 业务：work_date、hours decimal(6,1)、overtime_flag、work_description、achievement_description；
- 更正：source_entry_id、correction_reason、entry_status、reversed_at、reversed_by；
- 计价：cost_status、rate_id_snapshot、rate_amount_snapshot、rate_effective_start_snapshot、rate_effective_end_snapshot、actual_cost_id；
- 标准审计字段。

entry_status 使用 DRAFT、IN_APPROVAL、ARCHIVED、REVERSED；cost_status 使用 POSTED、REVERSED。建立 sheet_id、task_id+work_date、project_id+work_package_id、report_user_id+work_date、cost_status、source_entry_id 索引。

### 2.3 人工单价 pm_project_labor_rate

字段：rate_id、user_id、人员快照、effective_start_date、effective_end_date（可空）、hourly_rate decimal(16,2)、status、标准审计字段。建立 (user_id, effective_start_date) 索引；服务层以人员锁校验有效期不重叠。

### 2.4 通用实际成本来源 pm_project_actual_cost_source

| 字段 | 说明 |
| --- | --- |
| cost_source_id | 主键。 |
| actual_cost_id | 对应 pm_project_actual_cost。 |
| source_type | 首期支持 WORK_HOURS，并预留 PURCHASE_INBOUND。 |
| source_record_id | 工时 entry_id 或其他来源的最小事实主键。 |
| source_record_no | 周单号/明细号等来源快照。 |
| reversed_source_id | 冲销时关联原来源。 |
| source_snapshot_json | 仅审计用途，页面不得原样展示。 |

唯一索引 (source_type, source_record_id) 防止同一工时重复入账。采购来源迁移另行评估；本期采购兼容字段保留，工时只写此关联表。

## 3. 服务和事务设计

建议新增 workhours 模块，包含 domain、mapper、service、controller、listener；复用任务、WBS、项目预算、工作包预算、实际成本、审批流和数据范围服务。

核心服务：ProjectWorkHoursService（周单、明细、提交、撤回、更正、任务聚合）、LaborRateService（单价维护及按日期解析）、LaborCostPostingService（自动归集、冲销、幂等）、WorkHoursWorkflowListener（WORK_HOURS 回调）。

### 3.1 审批归档回调

1. 校验实例类型并按周单加锁，确保重复回调幂等。
2. 将周单和明细转为 ARCHIVED。
3. 逐条复核工时日期的有效人员单价、项目/工作包内部人工预算和余额；使用保存时已校验的单价写入快照。
4. 对每条明细在同一事务调用实际成本服务创建工作包级 INTERNAL_LABOR 成本和来源关联，成功后置 POSTED。
6. 重新汇总受影响任务 actualHours。

归档事务对预算余额加锁并保持原子性：任一明细的最终校验或成本创建失败，整张周单不得归档，也不得产生部分任务工时或部分成本。

### 3.2 更正与冲销

创建更正仅生成新草稿，不立即失效原记录。新明细归档时，事务中将原明细改为 REVERSED，原 actual_cost_id 对应成本改为冲销/失效并建立反向来源关联；随后处理新明细计价归集，最后重算原/新任务 actualHours。对于同一来源已有审批中或归档替换记录时拒绝重复更正。

## 4. 接口与授权

| 接口 | 说明 |
| --- | --- |
| GET /projectManagement/work-hours/my-sheets | 当前用户周单查询。 |
| GET/POST/PUT /projectManagement/work-hours/my-sheets | 查询、新建、保存草稿。 |
| GET /projectManagement/work-hours/eligible-tasks | 返回当前用户可填报执行任务。 |
| POST /projectManagement/work-hours/sheets/{id}/submit | 提交并创建 WORK_HOURS 流程。 |
| POST /projectManagement/work-hours/sheets/{id}/withdraw | 撤回本人审批中周单。 |
| POST /projectManagement/work-hours/entries/{id}/correction | 从归档明细创建更正草稿。 |
| GET /projectManagement/work-hours/manage | 工时管理及成本状态筛选。 |
| GET/POST/PUT /projectManagement/labor-rates | 单价分页、创建、编辑、停用。 |

预留 projectManagement:workHours:* 与 projectManagement:laborRate:* 权限。当前接口注解临时关闭状态不在本阶段恢复，写操作必须服务层校验任务执行人、项目数据范围和成本范围。

## 5. 校验与汇总

- 以 Asia/Shanghai 计算自然周；当前周可填、上一周补报必须有原因、更早周仅更正。
- 校验任务归属、本人执行人资格、项目状态 ACTIVE/PAUSED、工作包可执行。
- 同一人员同日汇总普通小时不超过 8、全部小时不超过 12，hours * 2 必须为整数。
- 填报时使用直接时长，不录入起止时间；hours 大于 0 且 `hours * 2` 必须为整数。前端实时校验并由保存草稿接口执行同一套后端校验。
- 保存时必须解析到金额大于 0 的有效人员单价（同人有效期间不重叠），并校验 INTERNAL_LABOR 项目分类预算、工作包预算及剩余余额。
- 提交时仅复核周单状态、明细非空、数据未被篡改与审批流程；归档时作为并发保护再次加锁校验单价和预算。
- 审批回调、自动归集和冲销均依赖状态机、来源唯一索引和行锁幂等处理。

## 6. 前端与迁移

- 个人中心新增我的工时周历/日明细页面，任务使用远程搜索下拉，快捷入口通过 taskId 路由参数预填且后端二次校验。
- 工时管理、人工单价均使用既有 ProTable；预算页面展示已归集内部人工成本和剩余预算。
- 审批详情和所有页面只展示字段化信息，不渲染 source_snapshot_json 或原始 JSON。
- SQL 顺序：建周单/明细/单价/来源表和索引；接入任务 actualHours 聚合并关闭手工入口；新增流程类型、菜单权限；扩展预算执行统计；在 SQL README 登记幂等迁移。
- 首发不迁移历史工时。旧 actualHours 手工值如何处置须在上线前明确为保留或运维重算，不能静默覆盖。

## 7. 测试与实施顺序

1. 建表、枚举、单价维护和基础查询。
2. 我的工时、任务快捷入口、周单保存和审批闭环。
3. 归档任务实际工时聚合及工时管理查询。
4. 单价快照、保存阶段预算校验与归档自动归集。
5. 更正冲销、预算汇总、端到端验收与性能检查。

测试覆盖：日期窗口、执行人、0.5 小时精度、日累计、退回重提、回调重试、无单价、缺预算、预算不足、单价历史追溯、一次/多次更正及冲销后的任务和预算汇总。
