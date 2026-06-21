drop table if exists agent_message;
drop table if exists agent_conversation;

create table agent_conversation (
  id bigint not null auto_increment comment '本地会话ID', user_id bigint not null comment '用户ID',
  dify_conversation_id varchar(128) default null comment 'Dify会话ID', title varchar(100) not null,
  status varchar(20) not null default 'active', last_task_id varchar(128) default null,
  last_workflow_run_id varchar(128) default null, message_count int not null default 0,
  create_by varchar(64) default '', create_time datetime default current_timestamp,
  update_by varchar(64) default '', update_time datetime default current_timestamp,
  primary key(id), key idx_agent_conv_user_update(user_id,update_time), key idx_agent_conv_dify(dify_conversation_id)
) engine=InnoDB comment='Agent本地会话';

create table agent_message (
  id bigint not null auto_increment, conversation_id bigint not null, dify_message_id varchar(128) default null,
  task_id varchar(128) default null, workflow_run_id varchar(128) default null, role varchar(20) not null,
  content longtext, event_log longtext, metadata longtext, token_count int default 0,
  status varchar(20) not null, error_message varchar(1000) default null,
  create_by varchar(64) default '', create_time datetime default current_timestamp,
  primary key(id), key idx_agent_msg_conversation_time(conversation_id,create_time), key idx_agent_msg_dify(dify_message_id)
) engine=InnoDB comment='Agent本地消息';

insert into sys_config(config_name,config_key,config_value,config_type,create_by,create_time,remark)
select 'Dify API地址','agent.dify.api_base_url','https://api.dify.ai/v1','Y','admin',sysdate(),'Agent Chatflow API地址'
where not exists(select 1 from sys_config where config_key='agent.dify.api_base_url');
insert into sys_config(config_name,config_key,config_value,config_type,create_by,create_time,remark)
select 'Dify API密钥','agent.dify.api_key','','Y','admin',sysdate(),'请填写Dify Chatflow App API Key'
where not exists(select 1 from sys_config where config_key='agent.dify.api_key');

insert into sys_menu
select 2000,'智能助手',0,5,'agent',null,'','',1,0,'M','0','0','','message','admin',sysdate(),'',null,'Agent功能目录'
where not exists(select 1 from sys_menu where menu_id=2000);
insert into sys_menu
select 2001,'Agent 对话',2000,1,'chat','agent/index','','',1,0,'C','0','0','','message','admin',sysdate(),'',null,'Dify Chatflow 对话'
where not exists(select 1 from sys_menu where menu_id=2001);
insert into sys_menu
select 2002,'Dify 配置说明',2000,2,'dify-config','agent/config','AgentDifyConfig','',1,0,'C','0','0','','setting','admin',sysdate(),'',null,'Dify参数配置说明'
where not exists(select 1 from sys_menu where menu_id=2002);
insert into sys_role_menu(role_id,menu_id)
select 1,2000 where not exists(select 1 from sys_role_menu where role_id=1 and menu_id=2000);
insert into sys_role_menu(role_id,menu_id)
select 1,2001 where not exists(select 1 from sys_role_menu where role_id=1 and menu_id=2001);
insert into sys_role_menu(role_id,menu_id)
select 1,2002 where not exists(select 1 from sys_role_menu where role_id=1 and menu_id=2002);
