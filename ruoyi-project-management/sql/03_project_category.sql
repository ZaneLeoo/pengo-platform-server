create table if not exists pm_project_category (
 category_id bigint not null auto_increment comment '分类ID', parent_id bigint not null default 0 comment '父分类ID',
 ancestors varchar(500) not null default '0' comment '祖级路径', category_code varchar(32) not null comment '分类编码',
 category_name varchar(50) not null comment '分类名称', order_num int not null default 0 comment '排序', status char(1) not null default '0' comment '状态（0启用 1停用）',
 create_by varchar(64) default '', create_time datetime default null, update_by varchar(64) default '', update_time datetime default null, remark varchar(500) default null,
 primary key(category_id), unique key uk_pm_project_category_code(category_code), key idx_pm_project_category_parent(parent_id)
) engine=innodb comment='项目分类';

