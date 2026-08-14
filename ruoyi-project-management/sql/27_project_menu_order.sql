-- 项目管理菜单收口：统一 V1 菜单顺序。
-- 项目工作台作为入口保留；任务管理暂时隐藏，代码和接口不删除。

SET @pm_menu_id = (
    SELECT menu_id
    FROM sys_menu
    WHERE menu_name = '项目管理'
      AND menu_type = 'M'
    ORDER BY menu_id DESC
    LIMIT 1
);

UPDATE sys_menu
SET order_num = CASE path
        WHEN 'workbench' THEN 0
        WHEN 'category' THEN 1
        WHEN 'project' THEN 2
        WHEN 'deliverable' THEN 3
        WHEN 'deliverable-type' THEN 4
        WHEN 'issue' THEN 5
        WHEN 'person' THEN 6
        WHEN 'professional-role' THEN 7
        WHEN 'task' THEN 99
        ELSE order_num
    END,
    update_by = 'admin',
    update_time = SYSDATE()
WHERE parent_id = @pm_menu_id
  AND path IN ('workbench', 'category', 'project', 'deliverable', 'deliverable-type',
               'issue', 'person', 'professional-role', 'task');

UPDATE sys_menu
SET visible = '1',
    status = '1',
    update_by = 'admin',
    update_time = SYSDATE()
WHERE parent_id = @pm_menu_id
  AND path = 'task';
