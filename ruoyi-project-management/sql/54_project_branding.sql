-- 类型：项目品牌
-- 适用版本：pm 数据库
-- 可重复执行：是
-- 数据风险：仅更新 admin 的显示姓名，不修改登录账号、角色或权限。

UPDATE sys_user
SET nick_name = '系统管理员',
    update_by = 'admin',
    update_time = NOW()
WHERE user_name = 'admin' AND del_flag = '0';
