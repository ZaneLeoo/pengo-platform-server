-- 执行任务生命周期操作记录。
-- 记录开始、暂停、恢复、完成等受控动作，保留操作人、状态变化和暂停原因。
create table if not exists pm_project_task_operation_log (
    log_id bigint not null auto_increment comment '记录ID',
    task_id bigint not null comment '任务ID',
    action varchar(20) not null comment '操作类型：START、PAUSE、RESUME、COMPLETE',
    from_status varchar(20) not null comment '操作前状态',
    to_status varchar(20) not null comment '操作后状态',
    remark varchar(500) null comment '操作说明或暂停原因',
    operator_user_id bigint null comment '操作用户ID',
    operator_name varchar(64) not null comment '操作人登录名',
    operation_time datetime not null comment '操作时间',
    primary key (log_id),
    key idx_pm_task_operation_log_task_time (task_id, operation_time)
) engine=InnoDB default charset=utf8mb4 comment='项目执行任务操作记录';
