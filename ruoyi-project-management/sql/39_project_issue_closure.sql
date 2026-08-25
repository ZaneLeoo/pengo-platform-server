-- V2：问题闭环。全新结构不承担历史数据迁移。
alter table pm_project_issue
  add column reporter_user_id bigint not null after description,
  add column resolved_time datetime null after resolution,
  add column closed_time datetime null after resolved_time;

create table pm_project_issue_activity (
  activity_id bigint not null auto_increment,
  issue_id bigint not null,
  activity_type varchar(20) not null comment 'CREATED、COMMENT、STATUS、ATTACHMENT',
  content varchar(2000) null,
  from_status varchar(20) null,
  to_status varchar(20) null,
  attachment_name varchar(255) null,
  attachment_url varchar(1000) null,
  operator_user_id bigint not null,
  create_by varchar(64) default '',
  create_time datetime not null,
  primary key (activity_id),
  key idx_pm_issue_activity (issue_id, create_time)
) engine=innodb comment='项目问题动态';

update sys_menu
set perms = 'projectManagement:issue:list'
where path = 'issue' and menu_name = '问题跟踪';

set @issue_menu=(select menu_id from sys_menu where path='issue' and menu_name='问题跟踪' limit 1);
insert into sys_menu(menu_name,parent_id,order_num,path,component,query_param,route_name,is_frame,is_cache,menu_type,
 visible,status,perms,icon,create_by,create_time,remark)
select action_name,@issue_menu,sort_no,'','',null,'',1,0,'F','0','0',permission,'#','admin',sysdate(),''
from (
 select '问题详情' action_name,1 sort_no,'projectManagement:issue:query' permission union all
 select '新增问题',2,'projectManagement:issue:add' union all
 select '维护问题',3,'projectManagement:issue:edit' union all
 select '删除问题',4,'projectManagement:issue:remove'
) actions
where not exists(select 1 from sys_menu existing where existing.perms=actions.permission);
insert ignore into sys_role_menu(role_id,menu_id)
select parent_grant.role_id,child.menu_id from sys_role_menu parent_grant
join sys_menu child on child.parent_id=@issue_menu and child.menu_type='F'
where parent_grant.menu_id=@issue_menu;
