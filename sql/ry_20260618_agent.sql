-- ----------------------------
-- 1、Agent对话会话表
-- ----------------------------
drop table if exists agent_conversation;
create table agent_conversation (
  id                    bigint(20)      not null auto_increment    comment '主键',
  user_id               bigint(20)      not null                   comment '用户ID',
  dify_conversation_id  varchar(64)     default null               comment 'Dify侧对话ID',
  title                 varchar(200)    default ''                 comment '会话标题',
  status                char(1)         default '0'                comment '状态（0进行中 1已结束）',
  message_count         int             default 0                  comment '消息数量',
  create_by             varchar(64)     default ''                 comment '创建者',
  create_time           datetime                                   comment '创建时间',
  update_by             varchar(64)     default ''                 comment '更新者',
  update_time           datetime                                   comment '更新时间',
  remark                varchar(500)    default null               comment '备注',
  primary key (id)
) engine=innodb auto_increment=1 comment = 'Agent对话会话表';

-- ----------------------------
-- 2、Agent对话消息表
-- ----------------------------
drop table if exists agent_message;
create table agent_message (
  id                bigint(20)      not null auto_increment    comment '主键',
  conversation_id   bigint(20)      not null                   comment '会话ID',
  dify_message_id   varchar(64)     default null               comment 'Dify侧消息ID',
  role              varchar(20)     default ''                 comment '角色（user/assistant）',
  content           longtext                                   comment '消息内容（Markdown）',
  thinking          longtext                                   comment '思考过程JSON',
  message_metadata  longtext                                   comment '元数据JSON（工具调用、图表等）',
  token_count       int             default 0                  comment 'Token消耗',
  feedback          char(1)         default '0'                comment '用户反馈（1赞 0无 -1踩）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (id),
  key idx_conversation_id (conversation_id)
) engine=innodb auto_increment=1 comment = 'Agent对话消息表';

-- ----------------------------
-- 3、Dify连接配置（写入sys_config）
-- ----------------------------
insert into sys_config (config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark)
values ('Dify API基础地址', 'agent.dify.api_base_url', 'https://api.dify.ai', 'Y', 'admin', sysdate(), '', null, 'Dify平台API基础地址，如 https://api.dify.ai');

insert into sys_config (config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark)
values ('Dify API Key', 'agent.dify.api_key', '', 'Y', 'admin', sysdate(), '', null, 'Dify应用API Key，在Dify应用->API访问中获取');

insert into sys_config (config_name, config_key, config_value, config_type, create_by, create_time, update_by, update_time, remark)
values ('Dify应用类型', 'agent.dify.app_type', 'chat', 'Y', 'admin', sysdate(), '', null, 'Dify应用类型：chat（聊天助手）/ workflow（工作流）/ completion（文本生成）');
