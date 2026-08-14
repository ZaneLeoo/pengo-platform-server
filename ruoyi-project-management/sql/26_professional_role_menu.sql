-- 项目管理 V1：全局专业角色配置菜单
-- 可重复执行：已存在菜单或权限时不重复插入。

SET @pm_menu_id = (
    SELECT menu_id
    FROM sys_menu
    WHERE menu_name = '项目管理'
      AND menu_type = 'M'
    ORDER BY menu_id DESC
    LIMIT 1
);

INSERT INTO sys_menu (
    menu_name, parent_id, order_num, path, component, query, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, remark
)
SELECT '专业角色', @pm_menu_id, 8, 'professional-role', 'projectManagement/professionalRole/index', NULL,
       'ProfessionalRole', 1, 0, 'C', '0', '0', 'projectManagement:professionalRole:list', 'peoples',
       'admin', SYSDATE(), '项目团队可选的专业角色配置'
WHERE @pm_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu
      WHERE parent_id = @pm_menu_id AND path = 'professional-role'
  );

SET @professional_role_menu_id = (
    SELECT menu_id
    FROM sys_menu
    WHERE parent_id = @pm_menu_id
      AND path = 'professional-role'
    ORDER BY menu_id DESC
    LIMIT 1
);

UPDATE sys_menu
SET menu_name = '专业角色',
    order_num = 8,
    component = 'projectManagement/professionalRole/index',
    route_name = 'ProfessionalRole',
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'projectManagement:professionalRole:list',
    icon = 'peoples',
    update_by = 'admin',
    update_time = SYSDATE()
WHERE menu_id = @professional_role_menu_id;

INSERT INTO sys_menu (
    menu_name, parent_id, order_num, path, component, query, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, remark
)
SELECT '专业角色查询', @professional_role_menu_id, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0',
       'projectManagement:professionalRole:query', '#', 'admin', SYSDATE(), ''
WHERE @professional_role_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu
      WHERE parent_id = @professional_role_menu_id
        AND perms = 'projectManagement:professionalRole:query'
  );

INSERT INTO sys_menu (
    menu_name, parent_id, order_num, path, component, query, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, remark
)
SELECT '专业角色新增', @professional_role_menu_id, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0',
       'projectManagement:professionalRole:add', '#', 'admin', SYSDATE(), ''
WHERE @professional_role_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu
      WHERE parent_id = @professional_role_menu_id
        AND perms = 'projectManagement:professionalRole:add'
  );

INSERT INTO sys_menu (
    menu_name, parent_id, order_num, path, component, query, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, remark
)
SELECT '专业角色修改', @professional_role_menu_id, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0',
       'projectManagement:professionalRole:edit', '#', 'admin', SYSDATE(), ''
WHERE @professional_role_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu
      WHERE parent_id = @professional_role_menu_id
        AND perms = 'projectManagement:professionalRole:edit'
  );

INSERT INTO sys_menu (
    menu_name, parent_id, order_num, path, component, query, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, remark
)
SELECT '专业角色删除', @professional_role_menu_id, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0',
       'projectManagement:professionalRole:remove', '#', 'admin', SYSDATE(), ''
WHERE @professional_role_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu
      WHERE parent_id = @professional_role_menu_id
        AND perms = 'projectManagement:professionalRole:remove'
  );
