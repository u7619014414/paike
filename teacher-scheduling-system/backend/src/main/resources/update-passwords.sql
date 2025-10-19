-- 更新现有用户的密码为正确的哈希值
-- 密码加密方式: SHA-256 + Base64

-- 更新admin用户密码 (admin123)
UPDATE SYS_USERS
SET PASSWORD = 'JAvlGPq9JyTdtvBO6x2llnRI1+gxwIyPqCKAn3THIKk='
WHERE USERNAME = 'admin';

-- 更新teacher1用户密码 (teacher123)
UPDATE SYS_USERS
SET PASSWORD = 'zeOD7ujuekQArfehX3FvF5ouuXZGs34InrjW0E5mNBY='
WHERE USERNAME = 'teacher1';

-- 更新teacher2用户密码 (teacher123)
UPDATE SYS_USERS
SET PASSWORD = 'zeOD7ujuekQArfehX3FvF5ouuXZGs34InrjW0E5mNBY='
WHERE USERNAME = 'teacher2';

-- 更新student1用户密码 (user123)
UPDATE SYS_USERS
SET PASSWORD = '5gbjiw2MGbJM8O44CBgxYup81j/3kS27IrXoAyhrREY='
WHERE USERNAME = 'student1';

-- 查询更新后的用户
SELECT USERNAME, PASSWORD, USER_TYPE FROM SYS_USERS WHERE USERNAME IN ('admin', 'teacher1', 'teacher2', 'student1');
