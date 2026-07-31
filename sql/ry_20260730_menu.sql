-- 计量单位组菜单（挂在 基础数据/制造运营 下，parent_id=2004）
-- 最大menu_id目前是2108，使用2110+避免冲突
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, menu_type, visible, status, icon, create_by, create_time) VALUES (2110, '计量单位组', 2004, 3, 'unitGroup', 'mes/base/unitGroup/index', 'UnitGroup', 'C', '0', '0', '#', 'admin', NOW());

-- 权限按钮（F=功能按钮）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, perms, menu_type, visible, status, create_by, create_time) VALUES (2111, '计量单位组查询', 2110, 1, 'mes:unit:group:list', 'F', '0', '0', 'admin', NOW());
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, perms, menu_type, visible, status, create_by, create_time) VALUES (2112, '计量单位组新增', 2110, 2, 'mes:unit:group:add', 'F', '0', '0', 'admin', NOW());
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, perms, menu_type, visible, status, create_by, create_time) VALUES (2113, '计量单位组修改', 2110, 3, 'mes:unit:group:edit', 'F', '0', '0', 'admin', NOW());
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, perms, menu_type, visible, status, create_by, create_time) VALUES (2114, '计量单位组删除', 2110, 4, 'mes:unit:group:remove', 'F', '0', '0', 'admin', NOW());
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, perms, menu_type, visible, status, create_by, create_time) VALUES (2115, '计量单位换算', 2110, 5, 'mes:unit:calculate', 'F', '0', '0', 'admin', NOW());

-- 换算公式管理（可选，独立菜单挂在下面）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, menu_type, visible, status, icon, create_by, create_time) VALUES (2116, '换算公式', 2004, 4, 'conversionFormula', 'mes/base/unitGroup/formula', 'ConversionFormula', 'C', '0', '0', '#', 'admin', NOW());
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, perms, menu_type, visible, status, create_by, create_time) VALUES (2117, '换算公式查询', 2116, 1, 'mes:unit:formula:list', 'F', '0', '0', 'admin', NOW());
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, perms, menu_type, visible, status, create_by, create_time) VALUES (2118, '换算公式新增', 2116, 2, 'mes:unit:formula:add', 'F', '0', '0', 'admin', NOW());
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, perms, menu_type, visible, status, create_by, create_time) VALUES (2119, '换算公式修改', 2116, 3, 'mes:unit:formula:edit', 'F', '0', '0', 'admin', NOW());
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, perms, menu_type, visible, status, create_by, create_time) VALUES (2120, '换算公式删除', 2116, 4, 'mes:unit:formula:remove', 'F', '0', '0', 'admin', NOW());
