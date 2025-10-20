-- 为 S_STUDENTS 表添加 password 字段
-- 执行方式: mysql -u school_user -p school_system < add-password-field.sql

USE school_system;

-- 添加 password 字段（如果不存在）
ALTER TABLE S_STUDENTS ADD COLUMN IF NOT EXISTS password VARCHAR(255) DEFAULT '123456' COMMENT '登录密码' AFTER student_code;

-- 为已有数据设置默认密码
UPDATE S_STUDENTS SET password = '123456' WHERE password IS NULL OR password = '';

SELECT '=== Password字段添加完成 ===' as status;
SELECT
    'student_code' as '学号',
    'password' as '密码'
FROM S_STUDENTS
LIMIT 5;
