-- ============================================================
-- 轻量线性审批流程引擎：数据表
-- 说明：pm_flow_* 表；流程 = 定义 + 有序节点链（或签/会签）
-- ============================================================

-- 流程定义
create table if not exists pm_flow_definition (
  flow_id bigint not null auto_increment,
  flow_key varchar(64) not null comment '流程编码',
  flow_name varchar(100) not null comment '流程名称',
  status varchar(20) not null default '0' comment '状态：0草稿，1启用',
  remark varchar(500) null,
  create_by varchar(64) default '',
  create_time datetime,
  update_by varchar(64) default '',
  update_time datetime,
  primary key (flow_id),
  unique key uk_pm_flow_key (flow_key)
) engine=innodb comment='审批流程定义';

-- 流程节点（线性审批链，按 sort_order 依次执行）
create table if not exists pm_flow_definition_node (
  node_id bigint not null auto_increment,
  flow_id bigint not null comment '流程定义ID',
  node_name varchar(100) not null comment '节点名称',
  assign_type varchar(20) not null default 'user' comment '审批人配置类型：user指定人员，role指定角色',
  assign_value varchar(1000) not null comment '审批人配置值：登录名或角色编码，逗号分隔',
  sign_type varchar(20) not null default 'OR' comment '审批方式：OR或签（任一同意即通过），AND会签（全部同意才通过）',
  sort_order int not null default 0 comment '节点顺序，从小到大依次审批',
  create_by varchar(64) default '',
  create_time datetime,
  update_by varchar(64) default '',
  update_time datetime,
  primary key (node_id),
  key idx_pm_flow_node_flow (flow_id, sort_order)
) engine=innodb comment='审批流程节点';

-- 流程与业务模块绑定（一个业务类型绑定一个启用流程）
create table if not exists pm_flow_binding (
  binding_id bigint not null auto_increment,
  biz_type varchar(64) not null comment '业务类型编码',
  flow_id bigint not null comment '流程定义ID',
  create_by varchar(64) default '',
  create_time datetime,
  primary key (binding_id),
  unique key uk_pm_flow_binding (biz_type)
) engine=innodb comment='流程与业务模块绑定';

-- 流程实例
create table if not exists pm_flow_instance (
  instance_id bigint not null auto_increment,
  flow_id bigint not null comment '流程定义ID',
  flow_key varchar(64) not null comment '流程编码（冗余）',
  flow_name varchar(100) not null comment '流程名称（冗余）',
  biz_type varchar(64) not null comment '业务类型编码',
  biz_id bigint not null comment '业务记录ID',
  biz_code varchar(100) null comment '业务编码（展示用）',
  biz_name varchar(200) null comment '业务名称（展示用）',
  current_node_id bigint null comment '当前节点ID',
  current_node_name varchar(100) null comment '当前节点名称',
  status varchar(20) not null default 'RUNNING' comment '状态：RUNNING审批中，APPROVED已通过，REJECTED已驳回，CANCELLED已撤销',
  submit_by varchar(64) not null comment '发起人登录名',
  submit_name varchar(64) null comment '发起人姓名',
  submit_time datetime not null,
  finish_time datetime null,
  create_time datetime,
  update_time datetime,
  primary key (instance_id),
  key idx_pm_flow_inst_biz (biz_type, biz_id),
  key idx_pm_flow_inst_status (status),
  key idx_pm_flow_inst_submit (submit_by)
) engine=innodb comment='审批流程实例';

-- 待办任务（每审批人一条；或签任一通过即推进，会签全部通过才推进）
create table if not exists pm_flow_task (
  task_id bigint not null auto_increment,
  instance_id bigint not null comment '流程实例ID',
  node_id bigint not null comment '节点ID',
  node_name varchar(100) not null comment '节点名称（冗余）',
  assignee varchar(64) not null comment '审批人登录名',
  assignee_name varchar(64) null comment '审批人姓名',
  status varchar(20) not null default 'PENDING' comment '状态：PENDING待审批，APPROVED已同意，REJECTED已驳回，SKIPPED已跳过',
  comment varchar(1000) null comment '审批意见',
  approve_time datetime null,
  create_time datetime,
  update_time datetime,
  primary key (task_id),
  key idx_pm_flow_task_assignee (assignee, status),
  key idx_pm_flow_task_instance (instance_id)
) engine=innodb comment='审批待办任务';

-- 审批历史（审批链条）
create table if not exists pm_flow_history (
  history_id bigint not null auto_increment,
  instance_id bigint not null comment '流程实例ID',
  node_id bigint null comment '节点ID',
  node_name varchar(100) null comment '节点名称',
  action varchar(20) not null comment '动作：SUBMIT提交，APPROVE同意，REJECT驳回，CANCEL撤销',
  operator varchar(64) not null comment '操作人登录名',
  operator_name varchar(64) null comment '操作人姓名',
  comment varchar(1000) null comment '意见',
  operate_time datetime not null,
  primary key (history_id),
  key idx_pm_flow_history_instance (instance_id, operate_time)
) engine=innodb comment='审批流转历史';

-- 流程站内消息
create table if not exists pm_flow_message (
  message_id bigint not null auto_increment,
  receiver varchar(64) not null comment '接收人登录名',
  title varchar(200) not null comment '消息标题',
  content varchar(1000) null comment '消息内容',
  biz_type varchar(64) null comment '业务类型',
  biz_id bigint null comment '业务记录ID',
  instance_id bigint null comment '流程实例ID',
  read_flag varchar(1) not null default '0' comment '已读：0未读，1已读',
  create_time datetime not null,
  primary key (message_id),
  key idx_pm_flow_message_receiver (receiver, read_flag)
) engine=innodb comment='流程站内消息';
