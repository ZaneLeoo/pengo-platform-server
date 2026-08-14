-- V1：交付物类型、支持格式与提交方式配置。
create table if not exists pm_project_deliverable_type (
  type_id bigint not null auto_increment,
  type_code varchar(32) not null,
  type_name varchar(100) not null,
  submission_mode varchar(20) not null comment 'FILE/LINK，BUSINESS_OBJECT预留',
  default_approval_required char(1) not null default '0',
  status char(1) not null default '0',
  sort_order int not null default 0,
  create_by varchar(64) default '', create_time datetime,
  update_by varchar(64) default '', update_time datetime,
  primary key(type_id), unique key uk_pm_deliverable_type_code(type_code)
) engine=innodb comment='项目交付物类型配置';
create table if not exists pm_project_deliverable_type_format (
  id bigint not null auto_increment,
  type_id bigint not null,
  file_extension varchar(20) not null,
  primary key(id), unique key uk_pm_deliverable_type_format(type_id,file_extension)
) engine=innodb comment='交付物类型允许的文件格式';

insert into pm_project_deliverable_type(type_code,type_name,submission_mode,default_approval_required,status,sort_order,create_by,create_time)
select 'DOCUMENT','文档','FILE','0','0',10,'admin',sysdate() where not exists(select 1 from pm_project_deliverable_type where type_code='DOCUMENT');
insert into pm_project_deliverable_type(type_code,type_name,submission_mode,default_approval_required,status,sort_order,create_by,create_time)
select 'DRAWING','图纸/设计数据','FILE','1','0',20,'admin',sysdate() where not exists(select 1 from pm_project_deliverable_type where type_code='DRAWING');
insert into pm_project_deliverable_type(type_code,type_name,submission_mode,default_approval_required,status,sort_order,create_by,create_time)
select 'BOM','BOM','FILE','1','0',30,'admin',sysdate() where not exists(select 1 from pm_project_deliverable_type where type_code='BOM');
insert into pm_project_deliverable_type(type_code,type_name,submission_mode,default_approval_required,status,sort_order,create_by,create_time)
select 'PROCESS','工艺资料','FILE','1','0',40,'admin',sysdate() where not exists(select 1 from pm_project_deliverable_type where type_code='PROCESS');
insert into pm_project_deliverable_type(type_code,type_name,submission_mode,default_approval_required,status,sort_order,create_by,create_time)
select 'REPORT','测试/验收报告','FILE','1','0',50,'admin',sysdate() where not exists(select 1 from pm_project_deliverable_type where type_code='REPORT');
insert into pm_project_deliverable_type(type_code,type_name,submission_mode,default_approval_required,status,sort_order,create_by,create_time)
select 'FORM','表单记录','FILE','0','0',60,'admin',sysdate() where not exists(select 1 from pm_project_deliverable_type where type_code='FORM');
insert into pm_project_deliverable_type(type_code,type_name,submission_mode,default_approval_required,status,sort_order,create_by,create_time)
select 'EXTERNAL_LINK','外部链接','LINK','0','0',70,'admin',sysdate() where not exists(select 1 from pm_project_deliverable_type where type_code='EXTERNAL_LINK');
insert into pm_project_deliverable_type(type_code,type_name,submission_mode,default_approval_required,status,sort_order,create_by,create_time)
select 'OTHER','其他','FILE','0','0',90,'admin',sysdate() where not exists(select 1 from pm_project_deliverable_type where type_code='OTHER');

insert ignore into pm_project_deliverable_type_format(type_id,file_extension)
select type_id, extension from (
  select type_id,'doc' extension from pm_project_deliverable_type where type_code='DOCUMENT' union all
  select type_id,'docx' from pm_project_deliverable_type where type_code='DOCUMENT' union all
  select type_id,'pdf' from pm_project_deliverable_type where type_code='DOCUMENT' union all
  select type_id,'xls' from pm_project_deliverable_type where type_code='DOCUMENT' union all
  select type_id,'xlsx' from pm_project_deliverable_type where type_code='DOCUMENT' union all
  select type_id,'ppt' from pm_project_deliverable_type where type_code='DOCUMENT' union all
  select type_id,'pptx' from pm_project_deliverable_type where type_code='DOCUMENT' union all
  select type_id,'pdf' from pm_project_deliverable_type where type_code='DRAWING' union all
  select type_id,'dwg' from pm_project_deliverable_type where type_code='DRAWING' union all
  select type_id,'dxf' from pm_project_deliverable_type where type_code='DRAWING' union all
  select type_id,'step' from pm_project_deliverable_type where type_code='DRAWING' union all
  select type_id,'stp' from pm_project_deliverable_type where type_code='DRAWING' union all
  select type_id,'xlsx' from pm_project_deliverable_type where type_code='BOM' union all
  select type_id,'csv' from pm_project_deliverable_type where type_code='BOM' union all
  select type_id,'pdf' from pm_project_deliverable_type where type_code='BOM' union all
  select type_id,'docx' from pm_project_deliverable_type where type_code='PROCESS' union all
  select type_id,'xlsx' from pm_project_deliverable_type where type_code='PROCESS' union all
  select type_id,'pdf' from pm_project_deliverable_type where type_code='PROCESS' union all
  select type_id,'docx' from pm_project_deliverable_type where type_code='REPORT' union all
  select type_id,'xlsx' from pm_project_deliverable_type where type_code='REPORT' union all
  select type_id,'pdf' from pm_project_deliverable_type where type_code='REPORT' union all
  select type_id,'pdf' from pm_project_deliverable_type where type_code='FORM'
) seeded;
