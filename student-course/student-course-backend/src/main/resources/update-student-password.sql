-- 为学生表添加密码字段
-- 使用方法: mysql -u root -p school_system < update-student-password.sql

USE school_system;

-- 添加密码字段
ALTER TABLE S_STUDENTS
ADD COLUMN password VARCHAR(100) COMMENT '登录密码（加密存储）' AFTER student_code;

-- 为现有学生设置默认密码（密码为: 123456）
-- 注意：这里使用的是明文，实际应用中会在代码中使用BCrypt加密
UPDATE S_STUDENTS SET password = '123456' WHERE password IS NULL;

-- 将密码字段设置为必填
ALTER TABLE S_STUDENTS
MODIFY COLUMN password VARCHAR(100) NOT NULL COMMENT '登录密码（加密存储）';

-- 添加索引以提高查询性能
CREATE INDEX idx_student_code ON S_STUDENTS(student_code);

-- 查看修改结果
DESC S_STUDENTS;

-- 示例：创建测试学生账号（实际密码会在应用中使用BCrypt加密）
-- INSERT INTO S_STUDENTS (student_code, password, student_name, age, age_group, parent_name, parent_phone, parent_email, registration_status, is_active)
-- VALUES ('STU001', '123456', '张三', 8, 3, '张父', '13800138001', 'parent1@example.com', 2, TRUE);
