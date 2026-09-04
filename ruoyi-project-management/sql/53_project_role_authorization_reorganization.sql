-- 类型：角色与菜单授权
-- 适用版本：项目管理 V2 分组菜单（52_project_menu_reorganization.sql 之后）
-- 可重复执行：是
-- 数据风险：重置业务角色的 sys_role_menu 和指定测试账号的 sys_user_role；不删除用户、角色或业务数据
-- 回退方式：恢复执行前 pm 数据库备份，或按本脚本开头的角色矩阵重新授予

START TRANSACTION;

-- 1. 角色边界。common 仅作为登录基础角色，不再携带系统管理或业务菜单。
UPDATE sys_role
SET role_name = '基础账号',
    role_sort = 90,
    remark = '仅登录和个人中心；不授予系统管理、项目管理或制造运营菜单',
    update_by = 'admin',
    update_time = NOW()
WHERE role_key = 'common' AND del_flag = '0';

INSERT INTO sys_role (
    role_name, role_key, role_sort, data_scope, menu_check_strictly,
    dept_check_strictly, status, del_flag, create_by, create_time, remark
) SELECT '项目成员', 'project_member', 35, '1', 1, 1, '0', '0', 'admin', NOW(),
         '项目团队成员：任务、交付物、问题和个人工时；具体项目范围由服务层按成员关系控制'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'project_member' AND del_flag = '0');

INSERT INTO sys_role (
    role_name, role_key, role_sort, data_scope, menu_check_strictly,
    dept_check_strictly, status, del_flag, create_by, create_time, remark
) SELECT '采购专员', 'procurement_operator', 60, '1', 1, 1, '0', '0', 'admin', NOW(),
         '采购闭环：供应商查询、采购订单、采购到货与采购入库；不授予系统管理和项目管理权限'
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'procurement_operator' AND del_flag = '0');

-- 2. 清空可控业务角色的旧菜单。admin 是超级管理员，不在此处收口。
DELETE rm
FROM sys_role_menu rm
JOIN sys_role r ON r.role_id = rm.role_id
WHERE r.role_key IN (
    'common', 'project_manager', 'project_member', 'cost_manager',
    'work_hours_approver', 'procurement_operator'
);

-- 3. 项目成员：项目执行中的任务、交付物、问题，以及个人工时。
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (
    2121, 2138, 2181, 2139, 2140, 2141, 2182, 2176,
    2142, 2144, 2156, 2157, 2158, 2179
)
WHERE r.role_key = 'project_member' AND r.del_flag = '0';

-- 4. 项目经理：全项目执行和工时管理；基础配置由超级管理员统一维护。
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (
    2121, 2138, 2181, 2182,
    2133, 2139, 2140, 2141, 2167,
    2176, 2177,
    2134, 2135, 2136, 2137,
    2142, 2143, 2144, 2145,
    2156, 2157, 2158, 2159,
    2164, 2165, 2166,
    2179
)
WHERE r.role_key = 'project_manager' AND r.del_flag = '0';

-- 5. 成本管理员：只读项目入口、工时台账和人工单价维护；不授予项目写操作或基础配置维护。
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (
    2121, 2181, 2133, 2182, 2176, 2177, 2178,
    2134, 2179, 2180
)
WHERE r.role_key = 'cost_manager' AND r.del_flag = '0';

-- 6. 工时审批人：个人填报和工时管理/审批，不授予项目、成本或系统配置权限。
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (2121, 2182, 2176, 2177, 2179)
WHERE r.role_key = 'work_hours_approver' AND r.del_flag = '0';

-- 7. 采购专员：物料和供应商查询，以及订单、到货、入库闭环。
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (
    2003, 2004, 2011, 2064, 2079, 2045, 2174, 2175,
    2046, 2047, 2048, 2049,
    2050, 2051, 2052, 2053, 2054,
    2055, 2056, 2057, 2058, 2059,
    2067
)
WHERE r.role_key = 'procurement_operator' AND r.del_flag = '0';

-- 8. 测试账号映射。lps 仅保留项目经理；核心成员改用项目成员角色。
DELETE ur
FROM sys_user_role ur
JOIN sys_user u ON u.user_id = ur.user_id
JOIN sys_role r ON r.role_id = ur.role_id
WHERE u.user_name IN ('lps', 'csy', 'zxl', 'qa_engineer', 'procurement_officer')
  AND r.role_key IN ('common', 'project_manager', 'project_member', 'procurement_operator');

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.user_id, r.role_id
FROM sys_user u
JOIN sys_role r ON r.role_key = 'project_manager' AND r.del_flag = '0'
WHERE u.user_name = 'lps' AND u.del_flag = '0';

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.user_id, r.role_id
FROM sys_user u
JOIN sys_role r ON r.role_key = 'project_member' AND r.del_flag = '0'
WHERE u.user_name IN ('csy', 'zxl', 'qa_engineer') AND u.del_flag = '0';

INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.user_id, r.role_id
FROM sys_user u
JOIN sys_role r ON r.role_key = 'procurement_operator' AND r.del_flag = '0'
WHERE u.user_name = 'procurement_officer' AND u.del_flag = '0';

COMMIT;

-- 目标授权矩阵：
-- admin                  超级管理员：系统管理、项目管理、制造运营全部能力。
-- lps / pm_manager       项目经理：项目执行、变更、项目团队与工时管理。
-- csy / zxl / qa_engineer 项目成员：任务、交付物、问题、个人工时。
-- cost_manager           成本管理员：项目只读、工时台账、人工单价。
-- hours_approver         工时审批人：个人工时、工时管理/审批。
-- procurement_officer    采购专员：供应商查询、订单、到货、入库。
-- common                 基础账号：不分配业务菜单。
