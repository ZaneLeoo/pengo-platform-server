-- AI 自动化业务：请求幂等与审计记录。
CREATE TABLE IF NOT EXISTS agent_automation_action (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    action_key VARCHAR(64) NOT NULL COMMENT '客户端请求幂等键',
    action_type VARCHAR(64) NOT NULL COMMENT '动作类型',
    user_id BIGINT NOT NULL COMMENT '确认执行的RuoYi用户ID',
    status VARCHAR(32) NOT NULL COMMENT '状态：PROCESSING/COMPLETED',
    target_id BIGINT DEFAULT NULL COMMENT '业务单据ID',
    target_code VARCHAR(64) DEFAULT NULL COMMENT '业务单据编号',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_agent_automation_action_key (action_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI自动化动作记录';
