-- 清理旧的审计日志数据（created_at为NULL的记录）
-- 使用前请备份数据！

-- 方案1: 删除所有created_at为NULL的记录
DELETE FROM audit_logs WHERE created_at IS NULL;

-- 或者方案2: 删除所有审计日志（如果数据不重要）
-- TRUNCATE TABLE audit_logs;

-- 验证：查看剩余的记录数
SELECT COUNT(*) as total_count FROM audit_logs;

-- 查看最近10条记录，确认时间是否正常
SELECT
    id,
    actor_id,
    action,
    target_type,
    target_id,
    created_at,
    updated_at
FROM audit_logs
ORDER BY id DESC
LIMIT 10;
