-- 教师表
CREATE TABLE IF NOT EXISTS L_TEACHERS (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '教师ID',
    teacher_code VARCHAR(50) NOT NULL UNIQUE COMMENT '教师编号',
    teacher_name VARCHAR(100) NOT NULL COMMENT '教师姓名',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '电子邮箱',
    specialties TEXT COMMENT '专长领域',
    age_groups VARCHAR(50) COMMENT '适教年龄段(1,2,3,4)',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否在职',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_teacher_code (teacher_code),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师信息表';

-- 教师分配表
CREATE TABLE IF NOT EXISTS L_TEACHER_ASSIGNMENTS (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分配ID',
    course_schedule_id BIGINT NOT NULL COMMENT '课程安排ID',
    teacher_id BIGINT NOT NULL COMMENT '教师ID',
    is_main_teacher BOOLEAN DEFAULT FALSE COMMENT '是否为主讲教师',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_schedule_id (course_schedule_id),
    INDEX idx_teacher_id (teacher_id),
    FOREIGN KEY (course_schedule_id) REFERENCES T_COURSE_SCHEDULE(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES L_TEACHERS(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师分配表';

-- 插入教师初始数据
INSERT INTO L_TEACHERS (teacher_code, teacher_name, phone, email, specialties, age_groups, is_active) VALUES
('T001', '张老师', '13800138001', 'zhang@example.com', '数学、逻辑思维', '1,2,3', TRUE),
('T002', '李老师', '13800138002', 'li@example.com', '英语、语言表达', '2,3,4', TRUE),
('T003', '王老师', '13800138003', 'wang@example.com', '美术、创意设计', '1,2,3,4', TRUE),
('T004', '刘老师', '13800138004', 'liu@example.com', '音乐、舞蹈', '2,3,4', TRUE),
('T005', '陈老师', '13800138005', 'chen@example.com', '体育、户外活动', '1,2,3,4', TRUE),
('T006', '赵老师', '13800138006', 'zhao@example.com', '科学、实验探索', '3,4', TRUE),
('T007', '周老师', '13800138007', 'zhou@example.com', '阅读、写作', '2,3,4', TRUE),
('T008', '吴老师', '13800138008', 'wu@example.com', '手工、创意制作', '1,2,3', TRUE);

-- 为已有的课程安排添加教师分配示例数据
-- 假设课程安排ID 1-10已存在，为其分配教师
INSERT INTO L_TEACHER_ASSIGNMENTS (course_schedule_id, teacher_id, is_main_teacher) VALUES
-- 课程安排 1: 趣味数学 (早班) - 主讲: 张老师, 助教: 吴老师
(1, 1, TRUE),
(1, 8, FALSE),
-- 课程安排 2: 英语启蒙 (中班) - 主讲: 李老师
(2, 2, TRUE),
-- 课程安排 3: 美术创作 (晚班) - 主讲: 王老师, 助教: 吴老师
(3, 3, TRUE),
(3, 8, FALSE),
-- 课程安排 5: 音乐欣赏 (早班) - 主讲: 刘老师
(5, 4, TRUE),
-- 课程安排 7: 趣味数学 (晚班) - 主讲: 张老师
(7, 1, TRUE),
-- 课程安排 8: 英语启蒙 (夜班) - 主讲: 李老师
(8, 2, TRUE),
-- 课程安排 9: 体能训练 (早班) - 主讲: 陈老师
(9, 5, TRUE),
-- 课程安排 10: 阅读理解 (中班) - 主讲: 周老师, 助教: 李老师
(10, 7, TRUE),
(10, 2, FALSE),
-- 课程安排 11: 美术创作 (晚班) - 主讲: 王老师
(11, 3, TRUE),
-- 课程安排 12: 音乐欣赏 (夜班) - 主讲: 刘老师
(12, 4, TRUE);
