create table if not exists pm_project_role (
  role_id bigint not null auto_increment, project_id bigint not null default 0, role_code varchar(32) not null,
  role_name varchar(64) not null, system_flag char(1) not null default '0', status char(1) not null default '0',
  sort_order int not null default 0, create_by varchar(64) default '', create_time datetime, update_by varchar(64) default '', update_time datetime,
  primary key(role_id), unique key uk_pm_project_role(project_id,role_code)
) engine=innodb comment='项目团队角色';

create table if not exists pm_project_member (
  member_id bigint not null auto_increment, project_id bigint not null, person_id bigint not null, role_id bigint not null,
  specialty_role varchar(100) default null, responsibility varchar(500) default null, join_date date not null, exit_date date default null,
  status varchar(20) not null default 'ACTIVE', remark varchar(500) default null,
  create_by varchar(64) default '', create_time datetime, update_by varchar(64) default '', update_time datetime,
  primary key(member_id), unique key uk_pm_project_member(project_id,person_id), key idx_pm_member_project_status(project_id,status)
) engine=innodb comment='项目团队成员';

insert into pm_project_role(project_id,role_code,role_name,system_flag,status,sort_order,create_by,create_time)
select 0,'PROJECT_MANAGER','项目负责人','1','0',10,'system',sysdate() where not exists(select 1 from pm_project_role where project_id=0 and role_code='PROJECT_MANAGER');
insert into pm_project_role(project_id,role_code,role_name,system_flag,status,sort_order,create_by,create_time)
select 0,'CORE_MEMBER','核心成员','1','0',20,'system',sysdate() where not exists(select 1 from pm_project_role where project_id=0 and role_code='CORE_MEMBER');
insert into pm_project_role(project_id,role_code,role_name,system_flag,status,sort_order,create_by,create_time)
select 0,'MEMBER','普通成员','1','0',30,'system',sysdate() where not exists(select 1 from pm_project_role where project_id=0 and role_code='MEMBER');

insert into pm_project_member(project_id,person_id,role_id,specialty_role,responsibility,join_date,status,create_by,create_time)
select p.project_id,p.manager_id,r.role_id,'项目经理','负责项目总体目标、计划与协调',coalesce(p.actual_start_date,p.start_date),'ACTIVE','system',sysdate()
from pm_project p join pm_project_role r on r.project_id=0 and r.role_code='PROJECT_MANAGER'
where not exists(select 1 from pm_project_member m where m.project_id=p.project_id and m.person_id=p.manager_id);
