-- 项目管理 V1：交付物类型与格式配置菜单
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
SELECT '交付物类型', @pm_menu_id, 7, 'deliverable-type', 'projectManagement/deliverableType/index', NULL,
       'ProjectDeliverableType', 1, 0, 'C', '0', '0', 'projectManagement:deliverableType:list', 'dict',
       'admin', SYSDATE(), '交付物类型与格式配置'
WHERE @pm_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu
      WHERE parent_id = @pm_menu_id AND path = 'deliverable-type'
  );

SET @deliverable_type_menu_id = (
    SELECT menu_id
    FROM sys_menu
    WHERE parent_id = @pm_menu_id
      AND path = 'deliverable-type'
    ORDER BY menu_id DESC
    LIMIT 1
);

UPDATE sys_menu
SET menu_name = '交付物类型',
    order_num = 7,
    component = 'projectManagement/deliverableType/index',
    route_name = 'ProjectDeliverableType',
    menu_type = 'C',
    visible = '0',
    status = '0',
    perms = 'projectManagement:deliverableType:list',
    icon = 'dict',
    update_by = 'admin',
    update_time = SYSDATE()
WHERE menu_id = @deliverable_type_menu_id;

INSERT INTO sys_menu (
    menu_name, parent_id, order_num, path, component, query, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, remark
)
SELECT '类型查询', @deliverable_type_menu_id, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0',
       'projectManagement:deliverableType:query', '#', 'admin', SYSDATE(), ''
WHERE @deliverable_type_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu
      WHERE parent_id = @deliverable_type_menu_id
        AND perms = 'projectManagement:deliverableType:query'
  );

INSERT INTO sys_menu (
    menu_name, parent_id, order_num, path, component, query, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, remark
)
SELECT '类型新增', @deliverable_type_menu_id, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0',
       'projectManagement:deliverableType:add', '#', 'admin', SYSDATE(), ''
WHERE @deliverable_type_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu
      WHERE parent_id = @deliverable_type_menu_id
        AND perms = 'projectManagement:deliverableType:add'
  );

INSERT INTO sys_menu (
    menu_name, parent_id, order_num, path, component, query, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, remark
)
SELECT '类型修改', @deliverable_type_menu_id, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0',
       'projectManagement:deliverableType:edit', '#', 'admin', SYSDATE(), ''
WHERE @deliverable_type_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu
      WHERE parent_id = @deliverable_type_menu_id
        AND perms = 'projectManagement:deliverableType:edit'
  );

INSERT INTO sys_menu (
    menu_name, parent_id, order_num, path, component, query, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon,
    create_by, create_time, remark
)
SELECT '类型删除', @deliverable_type_menu_id, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0',
       'projectManagement:deliverableType:remove', '#', 'admin', SYSDATE(), ''
WHERE @deliverable_type_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM sys_menu
      WHERE parent_id = @deliverable_type_menu_id
        AND perms = 'projectManagement:deliverableType:remove'
  );
