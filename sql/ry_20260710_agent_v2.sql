-- 企业 Agent V2 运行时模型。
-- V2 通过 conversation_id 与现有会话关联；迁移完成后旧流式实现不再使用。

create table if not exists agent_run
(
    id                    bigint        not null auto_increment comment '运行ID',
    conversation_id       bigint        not null comment '本地会话ID',
    user_id               bigint        not null comment '发起用户ID',
    user_message_id       bigint                 default null comment '用户消息ID',
    assistant_message_id  bigint                 default null comment '助手消息ID',
    status                varchar(32)   not null comment '运行状态',
    active_agent          varchar(64)            default null comment '当前Agent编码',
    dify_task_id          varchar(128)           default null comment 'Dify任务ID',
    workflow_run_id       varchar(128)           default null comment 'Dify工作流运行ID',
    tool_token_hash       varchar(64)            default null comment '本次运行工具令牌摘要',
    tool_token_expire_at  datetime               default null comment '工具令牌过期时间',
    last_sequence         bigint        not null default 0 comment '最后事件序号',
    token_count           int           not null default 0 comment 'Token数量',
    started_at            datetime               default null comment '开始时间',
    finished_at           datetime               default null comment '完成时间',
    error_code            varchar(64)            default null comment '错误码',
    error_message         varchar(1000)          default null comment '错误信息',
    create_by             varchar(64)            default '' comment '创建者',
    create_time           datetime               default current_timestamp comment '创建时间',
    update_by             varchar(64)            default '' comment '更新者',
    update_time           datetime               default current_timestamp comment '更新时间',
    primary key (id),
    key idx_agent_run_conversation (conversation_id, id),
    key idx_agent_run_user_status (user_id, status),
    key idx_agent_run_dify_task (dify_task_id)
) engine=InnoDB comment='Agent V2运行实例';

create table if not exists agent_run_step
(
    id              bigint        not null auto_increment comment '步骤ID',
    run_id          bigint        not null comment '运行ID',
    sequence_no     int           not null comment '步骤序号',
    step_type       varchar(32)   not null comment '步骤类型',
    display_name    varchar(200)  not null comment '用户可见名称',
    status          varchar(32)   not null comment '步骤状态',
    summary         varchar(1000)          default null comment '安全执行摘要',
    started_at      datetime               default null comment '开始时间',
    finished_at     datetime               default null comment '完成时间',
    duration_ms     bigint                 default null comment '耗时毫秒',
    create_by       varchar(64)            default '' comment '创建者',
    create_time     datetime               default current_timestamp comment '创建时间',
    update_by       varchar(64)            default '' comment '更新者',
    update_time     datetime               default current_timestamp comment '更新时间',
    primary key (id),
    unique key uk_agent_run_step_sequence (run_id, sequence_no),
    key idx_agent_run_step_status (run_id, status)
) engine=InnoDB comment='Agent V2运行步骤';

create table if not exists agent_tool_call
(
    id                     bigint        not null auto_increment comment '工具调用ID',
    run_id                 bigint        not null comment '运行ID',
    step_id                bigint                 default null comment '步骤ID',
    external_call_id       varchar(128)           default null comment 'Dify外部调用ID',
    tool_code              varchar(64)   not null comment '工具编码',
    status                 varchar(32)   not null comment '调用状态',
    input_json             longtext               default null comment '脱敏输入快照',
    output_json            longtext               default null comment '脱敏输出快照',
    idempotency_key        varchar(128)           default null comment '幂等键',
    confirmation_required  tinyint       not null default 0 comment '是否需要用户确认',
    started_at             datetime               default null comment '开始时间',
    finished_at            datetime               default null comment '完成时间',
    duration_ms            bigint                 default null comment '耗时毫秒',
    error_code             varchar(64)            default null comment '错误码',
    error_message          varchar(1000)          default null comment '错误信息',
    create_by              varchar(64)            default '' comment '创建者',
    create_time            datetime               default current_timestamp comment '创建时间',
    update_by              varchar(64)            default '' comment '更新者',
    update_time            datetime               default current_timestamp comment '更新时间',
    primary key (id),
    unique key uk_agent_tool_idempotency (idempotency_key),
    key idx_agent_tool_run (run_id, id),
    key idx_agent_tool_external (external_call_id)
) engine=InnoDB comment='Agent V2工具调用';

create table if not exists agent_session_state
(
    conversation_id       bigint        not null comment '本地会话ID',
    state_version         bigint        not null default 0 comment '乐观锁版本',
    active_goal           varchar(500)           default null comment '当前业务目标',
    current_domain        varchar(64)            default null comment '当前业务域',
    conversation_summary  longtext               default null comment '对话摘要',
    last_dataset_id       bigint                 default null comment '最近数据集ID',
    last_artifact_id      bigint                 default null comment '最近产物ID',
    last_file_id          bigint                 default null comment '最近文件ID',
    pending_action_json   longtext               default null comment '待确认动作',
    context_json          longtext               default null comment '扩展工作状态',
    create_by             varchar(64)            default '' comment '创建者',
    create_time           datetime               default current_timestamp comment '创建时间',
    update_by             varchar(64)            default '' comment '更新者',
    update_time           datetime               default current_timestamp comment '更新时间',
    primary key (conversation_id),
    key idx_agent_state_dataset (last_dataset_id),
    key idx_agent_state_artifact (last_artifact_id)
) engine=InnoDB comment='Agent V2会话工作状态';

create table if not exists agent_dataset
(
    id                   bigint        not null auto_increment comment '数据集ID',
    owner_user_id        bigint        not null comment '所属用户ID',
    conversation_id      bigint        not null comment '本地会话ID',
    run_id               bigint        not null comment '来源运行ID',
    source_tool_call_id  bigint                 default null comment '来源工具调用ID',
    dataset_name         varchar(200)           default null comment '数据集名称',
    storage_type         varchar(32)   not null default 'INLINE_JSON' comment '存储类型',
    schema_json          longtext      not null comment '字段结构',
    data_json            longtext               default null comment '第一版内联数据',
    row_count            int           not null default 0 comment '数据行数',
    expire_time          datetime               default null comment '过期时间',
    create_by            varchar(64)            default '' comment '创建者',
    create_time          datetime               default current_timestamp comment '创建时间',
    update_by            varchar(64)            default '' comment '更新者',
    update_time          datetime               default current_timestamp comment '更新时间',
    primary key (id),
    key idx_agent_dataset_owner (owner_user_id, id),
    key idx_agent_dataset_conversation (conversation_id, id),
    key idx_agent_dataset_expire (expire_time)
) engine=InnoDB comment='Agent V2可复用数据集';

create table if not exists agent_artifact
(
    id               bigint        not null auto_increment comment '产物ID',
    conversation_id  bigint        not null comment '本地会话ID',
    run_id           bigint        not null comment '来源运行ID',
    message_id       bigint                 default null comment '关联消息ID',
    dataset_id       bigint                 default null comment '关联数据集ID',
    artifact_type    varchar(32)   not null comment '产物类型',
    version          varchar(16)   not null default '2.0' comment '协议版本',
    title            varchar(200)  not null comment '标题',
    status           varchar(32)   not null comment '产物状态',
    payload_json     longtext               default null comment '安全载荷',
    file_id          bigint                 default null comment '系统文件ID',
    mime_type        varchar(128)           default null comment 'MIME类型',
    preview_url      varchar(500)           default null comment '预览地址',
    download_url     varchar(500)           default null comment '下载地址',
    create_by        varchar(64)            default '' comment '创建者',
    create_time      datetime               default current_timestamp comment '创建时间',
    update_by        varchar(64)            default '' comment '更新者',
    update_time      datetime               default current_timestamp comment '更新时间',
    primary key (id),
    key idx_agent_artifact_conversation (conversation_id, id),
    key idx_agent_artifact_run (run_id, id),
    key idx_agent_artifact_dataset (dataset_id)
) engine=InnoDB comment='Agent V2结构化产物';

create table if not exists agent_run_event
(
    id           bigint       not null auto_increment comment '事件ID',
    run_id       bigint       not null comment '运行ID',
    sequence_no  bigint       not null comment '运行内事件序号',
    event_type   varchar(64)  not null comment 'V2事件类型',
    data_json    longtext              default null comment '安全事件载荷',
    create_time  datetime              default current_timestamp comment '发生时间',
    primary key (id),
    unique key uk_agent_run_event_sequence (run_id, sequence_no),
    key idx_agent_run_event_time (run_id, create_time)
) engine=InnoDB comment='Agent V2可恢复事件';

insert into sys_config(config_name, config_key, config_value, config_type, create_by, create_time, remark)
select 'Agent V2工具网关密钥', 'agent.v2.tool_gateway_key', 'qxh9VNAkN5SvOl8d_g_KPvAuZtlWZpJ9nV86j7EIqkc', 'Y', 'admin', sysdate(),
       'Dify调用Spring内部工具时使用；必须配置为高强度随机值'
where not exists (select 1 from sys_config where config_key='agent.v2.tool_gateway_key');

insert into sys_config(config_name, config_key, config_value, config_type, create_by, create_time, remark)
select 'Agent V2工具显示别名', 'agent.v2.tool_display_aliases',
       'jina_search=联网搜索,current_time=获取当前时间', 'Y', 'admin', sysdate(),
       'Dify外部工具前端展示别名，格式：tool_code=显示名，多项用逗号、分号或换行分隔'
where not exists (select 1 from sys_config where config_key='agent.v2.tool_display_aliases');

insert into agent_dify_app_config(app_code, app_name, app_type, api_base_url, api_key, enabled, remark,
    create_by, create_time)
select 'AGENT_SUPERVISOR', '企业Agent V2 Supervisor', 'agent', api_base_url, '', 'N',
       '负责多轮工具选择和最终回答；配置无问题分类器的Agent应用API Key后启用。', 'admin', sysdate()
from agent_dify_app_config source
where source.app_code='AGENT_CHAT'
  and not exists (select 1 from agent_dify_app_config where app_code='AGENT_SUPERVISOR')
limit 1;

insert into agent_dify_app_config(app_code, app_name, app_type, api_base_url, api_key, enabled, remark,
    create_by, create_time)
select 'AGENT_SUPERVISOR', '企业Agent V2 Supervisor', 'agent', 'https://api.dify.ai/v1', '', 'N',
       '请配置无问题分类器的Dify Supervisor API Key后启用。', 'admin', sysdate()
where not exists (select 1 from agent_dify_app_config where app_code='AGENT_SUPERVISOR');

-- V2 接管聊天入口后移除旧分类 Chatflow 配置。
delete from agent_dify_app_config where app_code='AGENT_CHAT';
