create table if not exists pm_person (
    person_id bigint not null auto_increment comment '人员档案ID',
    person_code varchar(32) not null comment '工号',
    person_name varchar(50) not null comment '姓名',
    dept_id bigint not null comment '所属部门ID',
    position_name varchar(100) not null comment '岗位或专业角色',
    email varchar(100) default null comment '邮箱',
    mobile varchar(20) default null comment '联系电话',
    status char(1) not null default '0' comment '状态（0启用 1停用）',
    create_by varchar(64) default '' comment '创建者',
    create_time datetime default null comment '创建时间',
    update_by varchar(64) default '' comment '更新者',
    update_time datetime default null comment '更新时间',
    remark varchar(500) default null comment '备注',
    primary key (person_id),
    unique key uk_pm_person_code (person_code),
    key idx_pm_person_dept (dept_id),
    key idx_pm_person_name (person_name)
) engine=innodb comment='项目人员档案';

