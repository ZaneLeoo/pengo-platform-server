-- 工作台“我的任务”入口依赖任务管理动态路由，恢复该菜单的显示和启用状态。
SET @pm_menu_id = (
    SELECT menu_id
    FROM sys_menu
    WHERE menu_name = '项目管理'
      AND menu_type = 'M'
    ORDER BY menu_id DESC
    LIMIT 1
);

UPDATE sys_menu
SET visible = '0',
    status = '0',
    update_by = 'admin',
    update_time = SYSDATE()
WHERE parent_id = @pm_menu_id
  AND path = 'task';
