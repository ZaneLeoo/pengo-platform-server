-- V2：轻量串行审批流。定义版本发布后不可修改。
create table pm_workflow_definition (
  definition_id bigint not null auto_increment,
  definition_name varchar(100) not null,
  business_type varchar(40) not null comment 'PROJECT_INITIATION、DELIVERABLE_APPROVAL',
  active_version_id bigint null,
  status varchar(20) not null default 'ENABLED',
  create_by varchar(64) default '', create_time datetime,
  update_by varchar(64) default '', update_time datetime,
  primary key (definition_id), unique key uk_pm_workflow_business (business_type)
) engine=innodb comment='审批流程定义';

create table pm_workflow_definition_version (
  version_id bigint not null auto_increment,
  definition_id bigint not null,
  version_no int not null,
  version_status varchar(20) not null comment 'DRAFT、PUBLISHED',
  graph_json json not null,
  create_by varchar(64) default '', create_time datetime,
  publish_by varchar(64) null, publish_time datetime null,
  primary key (version_id), unique key uk_pm_workflow_version (definition_id, version_no)
) engine=innodb comment='审批流程不可变版本';

create table pm_workflow_instance (
  instance_id bigint not null auto_increment,
  business_type varchar(40) not null,
  business_id bigint not null,
  project_id bigint not null,
  definition_version_id bigint not null,
  title varchar(200) not null,
  initiator_user_id bigint not null,
  status varchar(20) not null comment 'RUNNING、APPROVED、REJECTED、WITHDRAWN',
  current_node_key varchar(64) null,
  business_snapshot_json json null,
  finish_time datetime null,
  create_by varchar(64) default '', create_time datetime,
  update_by varchar(64) default '', update_time datetime,
  primary key (instance_id), key idx_pm_workflow_business (business_type, business_id),
  key idx_pm_workflow_initiator (initiator_user_id, status)
) engine=innodb comment='审批流程实例';

create table pm_workflow_task (
  task_id bigint not null auto_increment,
  instance_id bigint not null,
  node_key varchar(64) not null,
  node_name varchar(100) not null,
  node_order int not null,
  status varchar(20) not null comment 'WAITING、PENDING、APPROVED、REJECTED、CANCELLED',
  acted_by_user_id bigint null,
  opinion varchar(1000) null,
  acted_time datetime null,
  create_time datetime not null,
  primary key (task_id), key idx_pm_workflow_task_instance (instance_id, node_order),
  key idx_pm_workflow_task_status (status)
) engine=innodb comment='审批任务';

create table pm_workflow_task_candidate (
  task_id bigint not null,
  user_id bigint not null,
  read_time datetime null,
  primary key (task_id, user_id), key idx_pm_workflow_candidate_user (user_id, read_time)
) engine=innodb comment='审批任务候选人';

create table pm_workflow_event_log (
  event_id bigint not null auto_increment,
  instance_id bigint not null,
  task_id bigint null,
  event_type varchar(30) not null,
  operator_user_id bigint not null,
  content varchar(1000) null,
  create_time datetime not null,
  primary key (event_id), key idx_pm_workflow_event (instance_id, create_time)
) engine=innodb comment='审批流事件';

alter table pm_project_initiation_approval add column workflow_instance_id bigint null after approval_id;
alter table pm_project_deliverable_submission add column workflow_instance_id bigint null after submission_id;

-- 配置菜单；业务审批入口由“我的审批”和页头铃铛提供。
insert into sys_menu(menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,
                     visible,status,perms,icon,create_by,create_time,remark)
select '审批流程配置', m.menu_id, 90, 'workflow-definition', 'projectManagement/workflow/definition/index', null,
       'WorkflowDefinition', 1, 0, 'C', '0', '0', 'projectManagement:workflow:config', 'apartment', 'admin', sysdate(),
       '项目管理V2轻量审批流配置'
from sys_menu m where m.path = 'projectManagement'
  and not exists(select 1 from sys_menu where perms = 'projectManagement:workflow:config');

insert ignore into sys_role_menu(role_id, menu_id)
select r.role_id, m.menu_id from sys_role r
join sys_menu m on m.perms='projectManagement:workflow:config'
where r.role_key='admin';
