-- 类型：个人待办/通知中心、项目启动任务通知、菜单权限
-- 适用数据库：pm
-- 可重复执行：是
-- 数据风险：新增通知表与权限菜单，不修改现有业务数据

START TRANSACTION;

CREATE TABLE IF NOT EXISTS pm_user_notification (
    notification_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    notification_type VARCHAR(32) NOT NULL COMMENT '通知类型',
    business_type VARCHAR(32) NOT NULL COMMENT '业务类型',
    business_id BIGINT DEFAULT NULL COMMENT '业务主键',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content VARCHAR(1000) NOT NULL COMMENT '通知内容',
    target_path VARCHAR(500) DEFAULT NULL COMMENT '前端跳转路径',
    read_flag CHAR(1) NOT NULL DEFAULT '0' COMMENT '是否已读：0否、1是',
    dedupe_key VARCHAR(160) DEFAULT NULL COMMENT '业务去重键',
    read_time DATETIME DEFAULT NULL COMMENT '阅读时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (notification_id),
    UNIQUE KEY uk_pm_user_notification_dedupe (dedupe_key),
    KEY idx_pm_user_notification_user_read (user_id, read_flag, create_time),
    KEY idx_pm_user_notification_business (business_type, business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目管理个人站内通知';

-- 权限记录挂在项目管理根菜单下；页面入口由前端个人中心统一展示。
INSERT INTO sys_menu(
    menu_name, parent_id, order_num, path, component, query, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, remark
)
SELECT '查看待办通知', root.menu_id, 96, '#', NULL, NULL, NULL,
       1, 0, 'F', '1', '0', 'projectManagement:notification:list', '#',
       'admin', NOW(), '查看本人待办、通知及未读数量'
FROM sys_menu root
WHERE root.path = 'projectManagement'
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE perms = 'projectManagement:notification:list'
  )
LIMIT 1;

INSERT INTO sys_menu(
    menu_name, parent_id, order_num, path, component, query, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, remark
)
SELECT '更新通知已读状态', root.menu_id, 97, '#', NULL, NULL, NULL,
       1, 0, 'F', '1', '0', 'projectManagement:notification:edit', '#',
       'admin', NOW(), '仅可更新本人通知的已读状态'
FROM sys_menu root
WHERE root.path = 'projectManagement'
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu WHERE perms = 'projectManagement:notification:edit'
  )
LIMIT 1;

-- 待办通知是所有登录角色的个人能力，统一授予全部启用角色；超级管理员仍由通配权限兜底。
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT role.role_id, menu.menu_id
FROM sys_role role
JOIN sys_menu menu
  ON menu.perms IN (
      'projectManagement:notification:list',
      'projectManagement:notification:edit'
  )
WHERE role.status = '0' AND role.del_flag = '0';

COMMIT;

SELECT menu_id, menu_name, perms, status
FROM sys_menu
WHERE perms LIKE 'projectManagement:notification:%'
ORDER BY menu_id;

SELECT role.role_key, COUNT(*) notification_permission_count
FROM sys_role role
LEFT JOIN sys_role_menu role_menu ON role_menu.role_id = role.role_id
LEFT JOIN sys_menu menu
  ON menu.menu_id = role_menu.menu_id
 AND menu.perms LIKE 'projectManagement:notification:%'
WHERE role.status = '0' AND role.del_flag = '0'
GROUP BY role.role_id, role.role_key
ORDER BY role.role_sort;
