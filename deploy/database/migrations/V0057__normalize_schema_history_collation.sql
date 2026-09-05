-- pm_schema_history 早于 V0056 创建，旧环境可能继承 MySQL 8 的 0900 默认排序规则。
-- 显式转换版本表，保证 pm 库内所有文本列统一使用 utf8mb4_unicode_ci。

ALTER TABLE `pm_schema_history`
    CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
