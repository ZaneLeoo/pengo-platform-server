-- 类型：结构 / 菜单 / 基础数据
-- 适用版本：V2.0 基线与变更 → V2 预算与成本阶段 1
-- 前置条件：项目管理菜单、sys_menu、sys_role_menu 已存在
-- 可重复执行：是
-- 数据风险：无（仅新增表、预置数据、菜单和授权）
-- 回退方式：确认未被后续预算/成本表引用后删除菜单和 pm_cost_category

create table if not exists pm_cost_category (
  cost_category_id bigint not null auto_increment comment '成本类别ID',
  parent_id bigint not null default 0 comment '上级类别ID，0为根',
  ancestors varchar(500) not null default '0' comment '祖级列表',
  category_code varchar(50) not null comment '类别编码',
  category_name varchar(100) not null comment '类别名称',
  level_no tinyint not null comment '层级1-3',
  allow_manual_entry char(1) not null default '1' comment '是否允许手工填报：0否1是',
  finance_account_code varchar(64) null comment '财务科目编码',
  finance_account_name varchar(128) null comment '财务科目名称',
  system_flag char(1) not null default '0' comment '系统预置：0否1是',
  status char(1) not null default '0' comment '状态：0启用1停用',
  sort_order int not null default 0 comment '同级排序',
  description varchar(500) null comment '类别说明',
  create_by varchar(64) null default '',
  create_time datetime null,
  update_by varchar(64) null default '',
  update_time datetime null,
  primary key (cost_category_id),
  unique key uk_pm_cost_category_code (category_code),
  unique key uk_pm_cost_category_parent_name (parent_id, category_name),
  key idx_pm_cost_category_parent (parent_id, sort_order),
  key idx_pm_cost_category_status (status)
) engine=InnoDB comment='项目成本类别';

insert into pm_cost_category(parent_id,ancestors,category_code,category_name,level_no,allow_manual_entry,
                             system_flag,status,sort_order,description,create_by,create_time)
select 0,'0',seed.category_code,seed.category_name,1,seed.allow_manual_entry,'1','0',seed.sort_order,
       seed.description,'system',sysdate()
from (
  select 'LABOR' category_code,'人工成本' category_name,'0' allow_manual_entry,10 sort_order,'项目人工投入汇总' description union all
  select 'MATERIAL','材料成本','0',20,'项目材料投入汇总' union all
  select 'OUTSOURCING','外协成本','0',30,'项目外部协作投入汇总' union all
  select 'TEST_CERTIFICATION','测试与认证','1',40,'测试、检验和认证费用' union all
  select 'EQUIPMENT_TOOL','设备及工具','1',50,'项目专用设备、工装和工具费用' union all
  select 'TRAVEL','差旅','1',60,'项目差旅费用' union all
  select 'SOFTWARE_SERVICE','软件与技术服务','1',70,'软件许可和技术服务费用' union all
  select 'OTHER','其他成本','1',99,'不能归入其他类别的项目成本'
) seed
where not exists(select 1 from pm_cost_category c where c.category_code=seed.category_code);

insert into pm_cost_category(parent_id,ancestors,category_code,category_name,level_no,allow_manual_entry,
                             system_flag,status,sort_order,description,create_by,create_time)
select parent.cost_category_id,concat('0,',parent.cost_category_id),seed.category_code,seed.category_name,2,'1',
       '1','0',seed.sort_order,seed.description,'system',sysdate()
from (
  select 'LABOR' parent_code,'INTERNAL_LABOR' category_code,'内部人工' category_name,10 sort_order,'内部员工工时形成的人工成本' description union all
  select 'LABOR','EXTERNAL_LABOR','外部人工',20,'外聘或劳务人员的人工成本' union all
  select 'MATERIAL','PROTOTYPE_MATERIAL','样机材料',10,'样机设计、制造和验证材料' union all
  select 'MATERIAL','TRIAL_MATERIAL','试生产材料',20,'试生产阶段使用的材料' union all
  select 'OUTSOURCING','OUTSOURCED_DESIGN','外协设计',10,'委托外部单位完成设计' union all
  select 'OUTSOURCING','OUTSOURCED_PROCESSING','外协加工',20,'委托外部单位完成加工' union all
  select 'OUTSOURCING','OUTSOURCED_TESTING','外协测试',30,'委托外部单位完成测试'
) seed
join pm_cost_category parent on parent.category_code=seed.parent_code
where not exists(select 1 from pm_cost_category c where c.category_code=seed.category_code);

set @pm_menu_id=(
  select menu_id from sys_menu where menu_name='项目管理' and menu_type='M' order by menu_id desc limit 1
);

insert into sys_menu(menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,
                     visible,status,perms,icon,create_by,create_time,remark)
select '成本类别',@pm_menu_id,9,'cost-category','projectManagement/costCategory/index',null,
       'CostCategory',1,0,'C','0','0','projectManagement:costCategory:list','money',
       'admin',sysdate(),'V2预算与成本分类配置'
where @pm_menu_id is not null
  and not exists(select 1 from sys_menu where parent_id=@pm_menu_id and path='cost-category');

set @cost_category_menu_id=(
  select menu_id from sys_menu where parent_id=@pm_menu_id and path='cost-category' order by menu_id desc limit 1
);

update sys_menu set menu_name='成本类别',order_num=9,component='projectManagement/costCategory/index',
  route_name='CostCategory',menu_type='C',visible='0',status='0',perms='projectManagement:costCategory:list',
  icon='money',update_by='admin',update_time=sysdate()
where menu_id=@cost_category_menu_id;

insert into sys_menu(menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,
                     visible,status,perms,icon,create_by,create_time,remark)
select action_name,@cost_category_menu_id,sort_no,'','',null,'',1,0,'F','0','0',permission,'#','admin',sysdate(),''
from (
  select '成本类别查询' action_name,1 sort_no,'projectManagement:costCategory:query' permission union all
  select '成本类别新增',2,'projectManagement:costCategory:add' union all
  select '成本类别修改',3,'projectManagement:costCategory:edit' union all
  select '成本类别启停',4,'projectManagement:costCategory:changeStatus' union all
  select '成本类别删除',5,'projectManagement:costCategory:remove'
) actions
where @cost_category_menu_id is not null
  and not exists(select 1 from sys_menu existing where existing.perms=actions.permission);

-- 复用专业角色配置的授权范围；这只是菜单授权，不产生管理员业务绕过。
insert ignore into sys_role_menu(role_id,menu_id)
select grant_role.role_id,@cost_category_menu_id
from sys_role_menu grant_role
join sys_menu origin on origin.menu_id=grant_role.menu_id
where origin.perms='projectManagement:professionalRole:list' and @cost_category_menu_id is not null;

insert ignore into sys_role_menu(role_id,menu_id)
select grant_role.role_id,target.menu_id
from sys_role_menu grant_role
join sys_menu origin on origin.menu_id=grant_role.menu_id
join sys_menu target on target.perms=case origin.perms
  when 'projectManagement:professionalRole:query' then 'projectManagement:costCategory:query'
  when 'projectManagement:professionalRole:add' then 'projectManagement:costCategory:add'
  when 'projectManagement:professionalRole:edit' then 'projectManagement:costCategory:edit'
  when 'projectManagement:professionalRole:remove' then 'projectManagement:costCategory:remove'
end
where origin.perms in ('projectManagement:professionalRole:query','projectManagement:professionalRole:add',
                       'projectManagement:professionalRole:edit','projectManagement:professionalRole:remove');

insert ignore into sys_role_menu(role_id,menu_id)
select grant_role.role_id,target.menu_id
from sys_role_menu grant_role
join sys_menu origin on origin.menu_id=grant_role.menu_id
join sys_menu target on target.perms='projectManagement:costCategory:changeStatus'
where origin.perms='projectManagement:professionalRole:edit';
