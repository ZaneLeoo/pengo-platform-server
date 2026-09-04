-- 类型：菜单
-- 适用版本：当前项目管理 V2 菜单 → 分组化项目管理菜单
-- 前置条件：已执行 48_project_work_hours.sql，且存在项目管理菜单（menu_id=2121）
-- 可重复执行：是
-- 数据风险：更新 sys_menu、sys_role_menu；不删除业务数据或菜单记录
-- 回退方式：恢复执行前 pm 数据库备份，或按本脚本的菜单归属和排序反向调整

START TRANSACTION;

-- 1. 建立项目管理二级分组。显式菜单 ID 仅用于当前项目管理数据库基线。
INSERT INTO sys_menu (
    menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, update_by, update_time, remark
) VALUES
    (2181, '项目执行', 2121, 2, 'execution', NULL, NULL, '', 1, 0, 'M', '0', '0', '', 'project', 'admin', NOW(), 'admin', NOW(), '项目管理执行菜单分组'),
    (2182, '工时与成本', 2121, 3, 'work-cost', NULL, NULL, '', 1, 0, 'M', '0', '0', '', 'money-collect', 'admin', NOW(), 'admin', NOW(), '项目管理工时与成本菜单分组'),
    (2183, '基础配置', 2121, 4, 'configuration', NULL, NULL, '', 1, 0, 'M', '0', '0', '', 'setting', 'admin', NOW(), 'admin', NOW(), '项目管理基础配置菜单分组')
ON DUPLICATE KEY UPDATE
    menu_name = VALUES(menu_name), parent_id = VALUES(parent_id), order_num = VALUES(order_num),
    path = VALUES(path), component = VALUES(component), query = VALUES(query), route_name = VALUES(route_name),
    is_frame = VALUES(is_frame), is_cache = VALUES(is_cache), menu_type = VALUES(menu_type),
    visible = VALUES(visible), status = VALUES(status), perms = VALUES(perms), icon = VALUES(icon),
    update_by = 'admin', update_time = NOW(), remark = VALUES(remark);

-- 2. 工作台保留为项目管理入口；其余能力按业务语义归组并排序。
UPDATE sys_menu
SET parent_id = CASE menu_id
        WHEN 2133 THEN 2181
        WHEN 2139 THEN 2181
        WHEN 2140 THEN 2181
        WHEN 2141 THEN 2181
        WHEN 2167 THEN 2181
        WHEN 2176 THEN 2182
        WHEN 2177 THEN 2182
        WHEN 2178 THEN 2182
        WHEN 2128 THEN 2183
        WHEN 2151 THEN 2183
        WHEN 2146 THEN 2183
        WHEN 2168 THEN 2183
        WHEN 2163 THEN 2183
        ELSE parent_id
    END,
    order_num = CASE menu_id
        WHEN 2138 THEN 1
        WHEN 2133 THEN 1
        WHEN 2139 THEN 2
        WHEN 2140 THEN 3
        WHEN 2141 THEN 4
        WHEN 2167 THEN 5
        WHEN 2176 THEN 1
        WHEN 2177 THEN 2
        WHEN 2178 THEN 3
        WHEN 2128 THEN 1
        WHEN 2151 THEN 2
        WHEN 2146 THEN 3
        WHEN 2168 THEN 4
        WHEN 2163 THEN 5
        ELSE order_num
    END,
    visible = '0', status = '0', update_by = 'admin', update_time = NOW()
WHERE menu_id IN (2128, 2133, 2138, 2139, 2140, 2141, 2146, 2151, 2163, 2167, 2168, 2176, 2177, 2178);

UPDATE sys_menu
SET order_num = CASE menu_id
        WHEN 2121 THEN 1
        WHEN 2003 THEN 2
        WHEN 1 THEN 3
        ELSE order_num
    END,
    update_by = 'admin', update_time = NOW()
WHERE menu_id IN (2121, 2003, 1);

-- 3. 让拥有分组内任一菜单的角色自动获得对应分组的访问权。
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, 2181
FROM sys_role_menu rm
WHERE rm.menu_id IN (2133, 2139, 2140, 2141, 2167);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, 2182
FROM sys_role_menu rm
WHERE rm.menu_id IN (2176, 2177, 2178);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, 2183
FROM sys_role_menu rm
WHERE rm.menu_id IN (2128, 2146, 2151, 2163, 2168);

-- 4. 项目采购保留：物料、供应商、采购订单、采购到货、采购入库保持显示并重排。
UPDATE sys_menu
SET order_num = CASE menu_id
        WHEN 2004 THEN 1
        WHEN 2079 THEN 2
        WHEN 2005 THEN 1
        WHEN 2011 THEN 2
        WHEN 2064 THEN 3
        WHEN 2045 THEN 1
        WHEN 2174 THEN 2
        WHEN 2175 THEN 3
        ELSE order_num
    END,
    visible = '0', status = '0', update_by = 'admin', update_time = NOW()
WHERE menu_id IN (2003, 2004, 2079, 2005, 2011, 2064, 2045, 2174, 2175);

-- 5. 当前项目范围外的制造菜单仅隐藏，保留数据和菜单记录，后续可恢复。
UPDATE sys_menu
SET visible = '1', status = '1', update_by = 'admin', update_time = NOW()
WHERE menu_id IN (2088, 2018, 2065, 2066, 2080, 2044, 2081, 2039);

COMMIT;
