-- 学生选课系统数据库结构
-- 创建数据库
CREATE DATABASE IF NOT EXISTS school_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE school_system;

-- 时间段表
CREATE TABLE T_TIME_SLOTS (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    slot_name VARCHAR(50) NOT NULL COMMENT '时间段名称',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    day_of_week TINYINT NOT NULL COMMENT '星期几(1-7)',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '时间段表';

-- 教室表
CREATE TABLE J_CLASSROOMS (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    classroom_code VARCHAR(20) UNIQUE NOT NULL COMMENT '教室编号',
    classroom_name VARCHAR(100) NOT NULL COMMENT '教室名称',
    max_capacity INT NOT NULL COMMENT '最大容量',
    location VARCHAR(200) COMMENT '位置',
    facilities TEXT COMMENT '设施',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '教室表';

-- 课程表
CREATE TABLE K_COURSES (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(20) UNIQUE NOT NULL COMMENT '课程编号',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    age_group TINYINT NOT NULL COMMENT '年龄组(1-4)',
    max_students INT NOT NULL COMMENT '最大学生数',
    description TEXT COMMENT '课程描述',
    teacher_names VARCHAR(500) COMMENT '教师姓名',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '课程表';

-- 课程安排表
CREATE TABLE T_COURSE_SCHEDULE (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL COMMENT '课程ID',
    time_slot_id BIGINT NOT NULL COMMENT '时间段ID',
    classroom_id BIGINT NOT NULL COMMENT '教室ID',
    schedule_date DATE NOT NULL COMMENT '安排日期',
    status TINYINT DEFAULT 1 COMMENT '状态(1-正常,2-取消)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES K_COURSES(id),
    FOREIGN KEY (time_slot_id) REFERENCES T_TIME_SLOTS(id),
    FOREIGN KEY (classroom_id) REFERENCES J_CLASSROOMS(id)
) COMMENT '课程安排表';

-- 学生表
CREATE TABLE S_STUDENTS (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_code VARCHAR(20) UNIQUE NOT NULL COMMENT '学生编号',
    password VARCHAR(255) DEFAULT '123456' COMMENT '登录密码',
    student_name VARCHAR(50) NOT NULL COMMENT '学生姓名',
    age TINYINT NOT NULL COMMENT '年龄',
    age_group TINYINT NOT NULL COMMENT '年龄组(1-4)',
    parent_name VARCHAR(50) NOT NULL COMMENT '家长姓名',
    parent_phone VARCHAR(20) NOT NULL COMMENT '家长电话',
    parent_email VARCHAR(100) COMMENT '家长邮箱',
    registration_status TINYINT DEFAULT 1 COMMENT '注册状态(1-待审核,2-已通过,3-已拒绝)',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '学生表';

-- 学生选课表
CREATE TABLE S_STUDENT_ENROLLMENTS (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_schedule_id BIGINT NOT NULL COMMENT '课程安排ID',
    enrollment_status TINYINT DEFAULT 1 COMMENT '选课状态(1-已选,2-已取消)',
    enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES S_STUDENTS(id),
    FOREIGN KEY (course_schedule_id) REFERENCES T_COURSE_SCHEDULE(id),
    UNIQUE KEY uk_student_course (student_id, course_schedule_id)
) COMMENT '学生选课表';

-- 创建索引
CREATE INDEX idx_time_slots_day ON T_TIME_SLOTS(day_of_week);
CREATE INDEX idx_course_schedule_date ON T_COURSE_SCHEDULE(schedule_date);
CREATE INDEX idx_students_age_group ON S_STUDENTS(age_group);
CREATE INDEX idx_students_status ON S_STUDENTS(registration_status);
CREATE INDEX idx_enrollments_student ON S_STUDENT_ENROLLMENTS(student_id);
CREATE INDEX idx_enrollments_course ON S_STUDENT_ENROLLMENTS(course_schedule_id);
