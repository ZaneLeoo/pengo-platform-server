-- 类型：数据库字符集修复
-- 适用版本：基线 55 + 56 → 57
-- 前置条件：pm_schema_history 已存在
-- 可重复执行：是
-- 数据风险：更新表定义，不删除数据
-- 回退方式：无需回退；统一使用项目标准 utf8mb4_unicode_ci

ALTER TABLE `pm_schema_history`
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
