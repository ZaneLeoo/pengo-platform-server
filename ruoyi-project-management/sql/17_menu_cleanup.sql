-- 项目管理 V1 菜单收口
-- 说明：仅停用非 V1 菜单，不删除菜单、权限和业务代码，便于后续恢复。

-- 1. 隐藏 AI 模块及其全部下级菜单/权限。
WITH RECURSIVE ai_menu AS (
    SELECT menu_id
    FROM sys_menu
    WHERE menu_id = 2000
    UNION ALL
    SELECT m.menu_id
    FROM sys_menu m
    INNER JOIN ai_menu p ON m.parent_id = p.menu_id
)
UPDATE sys_menu
SET visible = '1',
    status = '1',
    update_by = 'admin',
    update_time = NOW()
WHERE menu_id IN (SELECT menu_id FROM ai_menu);

-- 2. 制造运营仅保留基础数据，隐藏采购管理和库存管理及其全部下级。
WITH RECURSIVE hidden_mes_menu AS (
    SELECT menu_id
    FROM sys_menu
    WHERE menu_id IN (2083, 2084)
    UNION ALL
    SELECT m.menu_id
    FROM sys_menu m
    INNER JOIN hidden_mes_menu p ON m.parent_id = p.menu_id
)
UPDATE sys_menu
SET visible = '1',
    status = '1',
    update_by = 'admin',
    update_time = NOW()
WHERE menu_id IN (SELECT menu_id FROM hidden_mes_menu);

-- 3. 基础数据仅保留物料分类、物料和 BOM。
-- BOM主数据提升一级，避免侧栏出现“BOM管理 -> BOM主数据”的冗余层级。
UPDATE sys_menu
SET parent_id = 2004,
    menu_name = 'BOM',
    order_num = 3,
    update_by = 'admin',
    update_time = NOW()
WHERE menu_id = 2108;

-- 原 BOM 容器、AI 导入、计量单位、供应商、仓库等菜单停用。
UPDATE sys_menu
SET visible = '1',
    status = '1',
    update_by = 'admin',
    update_time = NOW()
WHERE menu_id IN (2017, 2041, 2107, 2121, 2122, 2123, 2085, 2086, 2087);

-- 保证 V1 需要的菜单处于启用、显示状态，并统一显示名称和顺序。
UPDATE sys_menu
SET menu_name = CASE menu_id
        WHEN 2003 THEN '制造运营'
        WHEN 2004 THEN '基础数据'
        WHEN 2005 THEN '物料分类'
        WHEN 2011 THEN '物料'
        WHEN 2108 THEN 'BOM'
    END,
    order_num = CASE menu_id
        WHEN 2005 THEN 1
        WHEN 2011 THEN 2
        WHEN 2108 THEN 3
        ELSE order_num
    END,
    visible = '0',
    status = '0',
    update_by = 'admin',
    update_time = NOW()
WHERE menu_id IN (2003, 2004, 2005, 2011, 2108);

-- 4. 隐藏系统监控、系统工具及其全部下级菜单/权限。
WITH RECURSIVE hidden_system_menu AS (
    SELECT menu_id
    FROM sys_menu
    WHERE menu_id IN (2, 3)
    UNION ALL
    SELECT m.menu_id
    FROM sys_menu m
    INNER JOIN hidden_system_menu p ON m.parent_id = p.menu_id
)
UPDATE sys_menu
SET visible = '1',
    status = '1',
    update_by = 'admin',
    update_time = NOW()
WHERE menu_id IN (SELECT menu_id FROM hidden_system_menu);

-- 5. 顶层动态菜单顺序：项目管理、制造运营、系统管理。
-- 工作台是前端固定首项，不在 sys_menu 中参与排序。
UPDATE sys_menu
SET order_num = CASE menu_id
        WHEN 2124 THEN 1
        WHEN 2003 THEN 2
        WHEN 1 THEN 3
    END,
    update_by = 'admin',
    update_time = NOW()
WHERE menu_id IN (2124, 2003, 1);
