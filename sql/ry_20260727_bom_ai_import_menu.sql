-- BOM 菜单整理：BOM 管理目录下挂 BOM 主数据和 AI 图纸导入
-- 兼容旧版 BOM 菜单（mes:* 权限、BOM 管理为 C 类型）

SET @mes_base_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE menu_type = 'M'
      AND path = 'base'
      AND menu_name = '基础数据'
    ORDER BY menu_id
    LIMIT 1
);

-- 确保 BOM 管理是基础数据下的目录。
INSERT INTO sys_menu (
    menu_name, parent_id, order_num, path, component, query, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, remark
)
SELECT
    'BOM管理', @mes_base_id, 3, 'bom', '', '', '',
    '1', '0', 'M', '0', '0', '', 'tree',
    'admin', SYSDATE(), 'BOM管理目录'
WHERE @mes_base_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM sys_menu
      WHERE parent_id = @mes_base_id
        AND menu_type = 'M'
        AND menu_name = 'BOM管理'
  );

SET @bom_dir_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE parent_id = @mes_base_id
      AND menu_type = 'M'
      AND menu_name = 'BOM管理'
    ORDER BY menu_id
    LIMIT 1
);

UPDATE sys_menu
SET menu_name = 'BOM管理',
    parent_id = @mes_base_id,
    order_num = 3,
    path = 'bom',
    component = '',
    query = '',
    route_name = '',
    is_frame = '1',
    is_cache = '0',
    menu_type = 'M',
    visible = '0',
    status = '0',
    perms = '',
    icon = 'tree',
    remark = 'BOM管理目录'
WHERE menu_id = @bom_dir_id;

-- 优先复用现有 BOM 主数据菜单，兼容旧版 mes:bomMaster:list 权限。
SET @bom_master_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE menu_type = 'C'
      AND (
          route_name = 'BomMaster'
          OR perms IN ('base:bomMaster:list', 'mes:bomMaster:list')
      )
    ORDER BY
        CASE
            WHEN route_name = 'BomMaster' THEN 1
            WHEN perms = 'base:bomMaster:list' THEN 2
            ELSE 3
        END,
        menu_id
    LIMIT 1
);

INSERT INTO sys_menu (
    menu_name, parent_id, order_num, path, component, query, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, remark
)
SELECT
    'BOM主数据', @bom_dir_id, 1, 'master', 'mes/base/bom/index', '', 'BomMaster',
    '1', '0', 'C', '0', '0', 'base:bomMaster:list', 'tree',
    'admin', SYSDATE(), 'BOM主数据菜单'
WHERE @bom_dir_id IS NOT NULL
  AND @bom_master_id IS NULL;

SET @bom_master_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE menu_type = 'C'
      AND (
          route_name = 'BomMaster'
          OR perms = 'base:bomMaster:list'
      )
    ORDER BY
        CASE WHEN route_name = 'BomMaster' THEN 1 ELSE 2 END,
        menu_id
    LIMIT 1
);

UPDATE sys_menu
SET menu_name = 'BOM主数据',
    parent_id = @bom_dir_id,
    order_num = 1,
    path = 'master',
    component = 'mes/base/bom/index',
    query = '',
    route_name = 'BomMaster',
    is_frame = '1',
    is_cache = '0',
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'base:bomMaster:list',
    icon = 'tree',
    remark = 'BOM主数据菜单'
WHERE menu_id = @bom_master_id;

-- 统一旧 BOM 按钮权限，并确保按钮挂在 BOM 主数据页面下。
UPDATE sys_menu
SET perms = REPLACE(perms, 'mes:bom', 'base:bom')
WHERE perms LIKE 'mes:bom%';

UPDATE sys_menu
SET parent_id = @bom_master_id
WHERE @bom_master_id IS NOT NULL
  AND menu_type = 'F'
  AND (
      perms LIKE 'base:bomMaster:%'
      OR perms LIKE 'base:bomVersion:%'
      OR perms LIKE 'base:bomItem:%'
  );

-- AI 导入作为 BOM 管理目录下与主数据同级的菜单。
SET @bom_ai_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE menu_type = 'C'
      AND (
          route_name = 'BomAiImport'
          OR perms = 'base:bomAiImport:import'
      )
    ORDER BY
        CASE WHEN route_name = 'BomAiImport' THEN 1 ELSE 2 END,
        menu_id
    LIMIT 1
);

INSERT INTO sys_menu (
    menu_name, parent_id, order_num, path, component, query, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, remark
)
SELECT
    'AI图纸导入', @bom_dir_id, 2, 'ai-import', 'mes/base/bomAiImport/index', '', 'BomAiImport',
    '1', '0', 'C', '0', '0', 'base:bomAiImport:import', 'file-search',
    'admin', SYSDATE(), 'AI BOM 图纸导入'
WHERE @bom_dir_id IS NOT NULL
  AND @bom_ai_id IS NULL;

SET @bom_ai_id := (
    SELECT menu_id
    FROM sys_menu
    WHERE menu_type = 'C'
      AND (
          route_name = 'BomAiImport'
          OR perms = 'base:bomAiImport:import'
      )
    ORDER BY
        CASE WHEN route_name = 'BomAiImport' THEN 1 ELSE 2 END,
        menu_id
    LIMIT 1
);

UPDATE sys_menu
SET menu_name = 'AI图纸导入',
    parent_id = @bom_dir_id,
    order_num = 2,
    path = 'ai-import',
    component = 'mes/base/bomAiImport/index',
    query = '',
    route_name = 'BomAiImport',
    is_frame = '1',
    is_cache = '0',
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'base:bomAiImport:import',
    icon = 'file-search',
    remark = 'AI BOM 图纸导入'
WHERE menu_id = @bom_ai_id;

-- 超级管理员默认拥有 BOM 目录、页面、详情路由和页面权限。
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE (
       m.menu_id IN (@bom_dir_id, @bom_master_id, @bom_ai_id)
       OR m.route_name = 'BomDetail'
       OR m.perms LIKE 'base:bomMaster:%'
       OR m.perms LIKE 'base:bomVersion:%'
       OR m.perms LIKE 'base:bomItem:%'
       OR m.perms = 'base:bomAiImport:import'
   )
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_menu r
      WHERE r.role_id = 1
        AND r.menu_id = m.menu_id
  );

-- 已拥有 BOM 按钮权限的角色需要同时拥有路由祖先；AI 菜单仍需单独授权。
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT existing.role_id, required.menu_id
FROM sys_role_menu existing
JOIN sys_menu button ON button.menu_id = existing.menu_id
JOIN sys_menu required
  ON required.menu_id IN (@bom_dir_id, @bom_master_id)
  OR required.route_name = 'BomDetail'
WHERE button.menu_type = 'F'
  AND (
      button.perms LIKE 'base:bomMaster:%'
      OR button.perms LIKE 'base:bomVersion:%'
      OR button.perms LIKE 'base:bomItem:%'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_menu r
      WHERE r.role_id = existing.role_id
        AND r.menu_id = required.menu_id
  );
