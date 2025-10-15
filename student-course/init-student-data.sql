-- 学生选课系统初始化数据
-- 使用说明：创建完数据库表结构后，执行此脚本初始化测试数据
-- mysql -u school_user -p school_system < init-student-data.sql

USE school_system;

-- 清空现有数据（开发环境使用，生产环境请谨慎）
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE S_STUDENT_ENROLLMENTS;
TRUNCATE TABLE T_COURSE_SCHEDULE;
TRUNCATE TABLE S_STUDENTS;
TRUNCATE TABLE K_COURSES;
TRUNCATE TABLE J_CLASSROOMS;
TRUNCATE TABLE T_TIME_SLOTS;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 插入时间段数据（周一到周日，每天4个时间段）
INSERT INTO T_TIME_SLOTS (slot_name, start_time, end_time, day_of_week, is_active) VALUES
-- 周一
('早班', '09:00:00', '11:00:00', 1, TRUE),
('中班', '11:00:00', '13:00:00', 1, TRUE),
('晚班', '14:00:00', '16:00:00', 1, TRUE),
('夜班', '16:00:00', '18:00:00', 1, TRUE),
-- 周二
('早班', '09:00:00', '11:00:00', 2, TRUE),
('中班', '11:00:00', '13:00:00', 2, TRUE),
('晚班', '14:00:00', '16:00:00', 2, TRUE),
('夜班', '16:00:00', '18:00:00', 2, TRUE),
-- 周三
('早班', '09:00:00', '11:00:00', 3, TRUE),
('中班', '11:00:00', '13:00:00', 3, TRUE),
('晚班', '14:00:00', '16:00:00', 3, TRUE),
('夜班', '16:00:00', '18:00:00', 3, TRUE),
-- 周四
('早班', '09:00:00', '11:00:00', 4, TRUE),
('中班', '11:00:00', '13:00:00', 4, TRUE),
('晚班', '14:00:00', '16:00:00', 4, TRUE),
('夜班', '16:00:00', '18:00:00', 4, TRUE),
-- 周五
('早班', '09:00:00', '11:00:00', 5, TRUE),
('中班', '11:00:00', '13:00:00', 5, TRUE),
('晚班', '14:00:00', '16:00:00', 5, TRUE),
('夜班', '16:00:00', '18:00:00', 5, TRUE),
-- 周六
('早班', '09:00:00', '11:00:00', 6, TRUE),
('中班', '11:00:00', '13:00:00', 6, TRUE),
('晚班', '14:00:00', '16:00:00', 6, TRUE),
('夜班', '16:00:00', '18:00:00', 6, TRUE),
-- 周日
('早班', '09:00:00', '11:00:00', 7, TRUE),
('中班', '11:00:00', '13:00:00', 7, TRUE),
('晚班', '14:00:00', '16:00:00', 7, TRUE),
('夜班', '16:00:00', '18:00:00', 7, TRUE);

-- 2. 插入教室数据
INSERT INTO J_CLASSROOMS (classroom_code, classroom_name, max_capacity, location, facilities, is_active) VALUES
('A101', '阳光教室', 30, '教学楼A栋1楼', '投影仪,空调,白板', TRUE),
('A102', '彩虹教室', 25, '教学楼A栋1楼', '投影仪,空调,白板,钢琴', TRUE),
('A201', '星空教室', 28, '教学楼A栋2楼', '投影仪,空调,白板,电脑', TRUE),
('A202', '海洋教室', 26, '教学楼A栋2楼', '投影仪,空调,白板', TRUE),
('B101', '森林教室', 32, '教学楼B栋1楼', '投影仪,空调,白板,钢琴', TRUE),
('B102', '花园教室', 30, '教学楼B栋1楼', '投影仪,空调,白板', TRUE),
('B201', '月亮教室', 24, '教学楼B栋2楼', '投影仪,空调,白板,电脑', TRUE),
('B202', '太阳教室', 28, '教学楼B栋2楼', '投影仪,空调,白板', TRUE);

-- 3. 插入课程数据（年龄组1-4，每组多个课程）
INSERT INTO K_COURSES (course_code, course_name, age_group, max_students, description, teacher_names, is_active) VALUES
-- 年龄组1 (3-4岁)
('AG1-001', '启蒙绘画', 1, 20, '培养孩子的艺术兴趣和创造力', '张老师,李老师', TRUE),
('AG1-002', '趣味英语', 1, 18, '通过游戏和歌曲学习基础英语', '王老师,刘老师', TRUE),
('AG1-003', '音乐启蒙', 1, 15, '培养音乐感知能力和节奏感', '陈老师', TRUE),
('AG1-004', '快乐运动', 1, 22, '发展基本运动技能', '赵老师,孙老师', TRUE),

-- 年龄组2 (5-6岁)
('AG2-001', '创意美术', 2, 22, '发展艺术创造力和手工技能', '周老师,吴老师', TRUE),
('AG2-002', '英语会话', 2, 20, '提升英语口语和听力能力', '郑老师', TRUE),
('AG2-003', '趣味数学', 2, 18, '通过游戏学习基础数学概念', '钱老师,冯老师', TRUE),
('AG2-004', '舞蹈表演', 2, 20, '培养舞蹈基础和表现力', '陈老师', TRUE),

-- 年龄组3 (7-9岁)
('AG3-001', '绘画进阶', 3, 24, '学习绘画技巧和色彩运用', '张老师,李老师', TRUE),
('AG3-002', '英语阅读', 3, 20, '提升英语阅读理解能力', '王老师', TRUE),
('AG3-003', '数学思维', 3, 22, '培养逻辑思维和问题解决能力', '刘老师,孙老师', TRUE),
('AG3-004', '科学探索', 3, 18, '激发科学兴趣和探索精神', '赵老师', TRUE),

-- 年龄组4 (10-12岁)
('AG4-001', '专业美术', 4, 20, '深入学习绘画技法', '周老师', TRUE),
('AG4-002', '英语写作', 4, 18, '提升英语写作和表达能力', '吴老师,郑老师', TRUE),
('AG4-003', '高级数学', 4, 20, '学习高级数学概念和应用', '钱老师', TRUE),
('AG4-004', '编程入门', 4, 16, '学习编程基础和计算思维', '冯老师,马老师', TRUE);

-- 4. 插入课程安排数据（为本周和未来几周创建课程安排）
-- 获取本周一的日期，并为接下来4周创建课程安排
-- 注意：这里使用变量来动态生成日期

SET @current_monday = DATE_ADD(CURDATE(), INTERVAL - WEEKDAY(CURDATE()) DAY);

-- 年龄组1的课程安排（每周多个时段）
-- 启蒙绘画 - 周一早班、周三晚班
INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG1-001') as course_id,
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 1 AND slot_name = '早班' LIMIT 1) as time_slot_id,
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'A101') as classroom_id,
    DATE_ADD(@current_monday, INTERVAL w WEEK) as schedule_date,
    1 as status
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG1-001'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 3 AND slot_name = '晚班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'A101'),
    DATE_ADD(@current_monday, INTERVAL (2 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

-- 趣味英语 - 周二中班、周四早班
INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG1-002'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 2 AND slot_name = '中班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'A102'),
    DATE_ADD(@current_monday, INTERVAL (1 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG1-002'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 4 AND slot_name = '早班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'A102'),
    DATE_ADD(@current_monday, INTERVAL (3 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

-- 音乐启蒙 - 周五晚班
INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG1-003'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 5 AND slot_name = '晚班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'A102'),
    DATE_ADD(@current_monday, INTERVAL (4 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

-- 快乐运动 - 周六早班
INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG1-004'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 6 AND slot_name = '早班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'B101'),
    DATE_ADD(@current_monday, INTERVAL (5 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

-- 年龄组2的课程安排
-- 创意美术 - 周一中班、周四晚班
INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG2-001'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 1 AND slot_name = '中班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'A201'),
    DATE_ADD(@current_monday, INTERVAL w * 7 DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG2-001'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 4 AND slot_name = '晚班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'A201'),
    DATE_ADD(@current_monday, INTERVAL (3 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

-- 英语会话 - 周二早班、周五中班
INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG2-002'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 2 AND slot_name = '早班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'A202'),
    DATE_ADD(@current_monday, INTERVAL (1 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG2-002'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 5 AND slot_name = '中班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'A202'),
    DATE_ADD(@current_monday, INTERVAL (4 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

-- 趣味数学 - 周三早班
INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG2-003'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 3 AND slot_name = '早班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'B101'),
    DATE_ADD(@current_monday, INTERVAL (2 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

-- 舞蹈表演 - 周六中班
INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG2-004'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 6 AND slot_name = '中班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'B102'),
    DATE_ADD(@current_monday, INTERVAL (5 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

-- 年龄组3的课程安排
-- 绘画进阶 - 周一晚班、周三中班
INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG3-001'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 1 AND slot_name = '晚班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'B101'),
    DATE_ADD(@current_monday, INTERVAL w * 7 DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG3-001'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 3 AND slot_name = '中班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'B101'),
    DATE_ADD(@current_monday, INTERVAL (2 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

-- 英语阅读 - 周二晚班、周四中班
INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG3-002'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 2 AND slot_name = '晚班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'B102'),
    DATE_ADD(@current_monday, INTERVAL (1 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG3-002'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 4 AND slot_name = '中班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'B102'),
    DATE_ADD(@current_monday, INTERVAL (3 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

-- 数学思维 - 周五早班
INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG3-003'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 5 AND slot_name = '早班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'B201'),
    DATE_ADD(@current_monday, INTERVAL (4 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

-- 科学探索 - 周六晚班
INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG3-004'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 6 AND slot_name = '晚班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'B201'),
    DATE_ADD(@current_monday, INTERVAL (5 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

-- 年龄组4的课程安排
-- 专业美术 - 周一夜班、周三夜班
INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG4-001'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 1 AND slot_name = '夜班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'B102'),
    DATE_ADD(@current_monday, INTERVAL w * 7 DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG4-001'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 3 AND slot_name = '夜班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'B102'),
    DATE_ADD(@current_monday, INTERVAL (2 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

-- 英语写作 - 周二夜班、周四夜班
INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG4-002'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 2 AND slot_name = '夜班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'B201'),
    DATE_ADD(@current_monday, INTERVAL (1 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG4-002'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 4 AND slot_name = '夜班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'B201'),
    DATE_ADD(@current_monday, INTERVAL (3 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

-- 高级数学 - 周五夜班
INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG4-003'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 5 AND slot_name = '夜班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'B202'),
    DATE_ADD(@current_monday, INTERVAL (4 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

-- 编程入门 - 周六夜班
INSERT INTO T_COURSE_SCHEDULE (course_id, time_slot_id, classroom_id, schedule_date, status)
SELECT
    (SELECT id FROM K_COURSES WHERE course_code = 'AG4-004'),
    (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 6 AND slot_name = '夜班' LIMIT 1),
    (SELECT id FROM J_CLASSROOMS WHERE classroom_code = 'B202'),
    DATE_ADD(@current_monday, INTERVAL (5 + w * 7) DAY),
    1
FROM (SELECT 0 as w UNION SELECT 1 UNION SELECT 2 UNION SELECT 3) weeks;

-- 5. 插入测试学生数据（每个年龄组3-5个学生）
-- 密码统一设置为 "123456"（实际应该加密，这里为了测试方便使用明文）
INSERT INTO S_STUDENTS (student_code, password, student_name, age, age_group, parent_name, parent_phone, parent_email, registration_status, is_active) VALUES
-- 年龄组1 (3-4岁)
('STU20250001', '123456', '小明', 3, 1, '张爸爸', '13800138001', 'zhang@example.com', 2, TRUE),
('STU20250002', '123456', '小红', 4, 1, '李妈妈', '13800138002', 'li@example.com', 2, TRUE),
('STU20250003', '123456', '小刚', 3, 1, '王爸爸', '13800138003', 'wang@example.com', 2, TRUE),
('STU20250004', '123456', '小丽', 4, 1, '赵妈妈', '13800138004', 'zhao@example.com', 2, TRUE),

-- 年龄组2 (5-6岁)
('STU20250005', '123456', '小华', 5, 2, '陈爸爸', '13800138005', 'chen@example.com', 2, TRUE),
('STU20250006', '123456', '小芳', 6, 2, '周妈妈', '13800138006', 'zhou@example.com', 2, TRUE),
('STU20250007', '123456', '小强', 5, 2, '吴爸爸', '13800138007', 'wu@example.com', 2, TRUE),
('STU20250008', '123456', '小梅', 6, 2, '郑妈妈', '13800138008', 'zheng@example.com', 2, TRUE),
('STU20250009', '123456', '小东', 5, 2, '冯爸爸', '13800138009', 'feng@example.com', 2, TRUE),

-- 年龄组3 (7-9岁)
('STU20250010', '123456', '小伟', 7, 3, '孙爸爸', '13800138010', 'sun@example.com', 2, TRUE),
('STU20250011', '123456', '小雪', 8, 3, '钱妈妈', '13800138011', 'qian@example.com', 2, TRUE),
('STU20250012', '123456', '小涛', 9, 3, '马爸爸', '13800138012', 'ma@example.com', 2, TRUE),
('STU20250013', '123456', '小兰', 7, 3, '朱妈妈', '13800138013', 'zhu@example.com', 2, TRUE),
('STU20250014', '123456', '小军', 8, 3, '胡爸爸', '13800138014', 'hu@example.com', 2, TRUE),

-- 年龄组4 (10-12岁)
('STU20250015', '123456', '小宇', 10, 4, '林爸爸', '13800138015', 'lin@example.com', 2, TRUE),
('STU20250016', '123456', '小霞', 11, 4, '何妈妈', '13800138016', 'he@example.com', 2, TRUE),
('STU20250017', '123456', '小峰', 12, 4, '高爸爸', '13800138017', 'gao@example.com', 2, TRUE),
('STU20250018', '123456', '小婷', 10, 4, '罗妈妈', '13800138018', 'luo@example.com', 2, TRUE);

-- 6. 添加一些测试选课记录（部分学生已经选了一些课程）
-- 小明（年龄组1）已选启蒙绘画的周一早班课程
INSERT INTO S_STUDENT_ENROLLMENTS (student_id, course_schedule_id, enrollment_status)
SELECT
    (SELECT id FROM S_STUDENTS WHERE student_code = 'STU20250001'),
    cs.id,
    1
FROM T_COURSE_SCHEDULE cs
WHERE cs.course_id = (SELECT id FROM K_COURSES WHERE course_code = 'AG1-001')
AND cs.time_slot_id = (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 1 AND slot_name = '早班' LIMIT 1)
AND cs.schedule_date = @current_monday
LIMIT 1;

-- 小红（年龄组1）已选趣味英语
INSERT INTO S_STUDENT_ENROLLMENTS (student_id, course_schedule_id, enrollment_status)
SELECT
    (SELECT id FROM S_STUDENTS WHERE student_code = 'STU20250002'),
    cs.id,
    1
FROM T_COURSE_SCHEDULE cs
WHERE cs.course_id = (SELECT id FROM K_COURSES WHERE course_code = 'AG1-002')
AND cs.time_slot_id = (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 2 AND slot_name = '中班' LIMIT 1)
AND cs.schedule_date = DATE_ADD(@current_monday, INTERVAL 1 DAY)
LIMIT 1;

-- 小华（年龄组2）已选创意美术
INSERT INTO S_STUDENT_ENROLLMENTS (student_id, course_schedule_id, enrollment_status)
SELECT
    (SELECT id FROM S_STUDENTS WHERE student_code = 'STU20250005'),
    cs.id,
    1
FROM T_COURSE_SCHEDULE cs
WHERE cs.course_id = (SELECT id FROM K_COURSES WHERE course_code = 'AG2-001')
AND cs.time_slot_id = (SELECT id FROM T_TIME_SLOTS WHERE day_of_week = 1 AND slot_name = '中班' LIMIT 1)
AND cs.schedule_date = @current_monday
LIMIT 1;

-- 查询统计信息
SELECT '=== 数据初始化完成 ===' as status;
SELECT
    '时间段' as item,
    COUNT(*) as count
FROM T_TIME_SLOTS
UNION ALL
SELECT '教室', COUNT(*) FROM J_CLASSROOMS
UNION ALL
SELECT '课程', COUNT(*) FROM K_COURSES
UNION ALL
SELECT '课程安排', COUNT(*) FROM T_COURSE_SCHEDULE
UNION ALL
SELECT '学生', COUNT(*) FROM S_STUDENTS
UNION ALL
SELECT '选课记录', COUNT(*) FROM S_STUDENT_ENROLLMENTS;

SELECT '=== 本周课程安排统计（按年龄组） ===' as status;
SELECT
    k.age_group as '年龄组',
    COUNT(DISTINCT cs.id) as '课程安排数'
FROM T_COURSE_SCHEDULE cs
JOIN K_COURSES k ON cs.course_id = k.id
WHERE cs.schedule_date BETWEEN @current_monday AND DATE_ADD(@current_monday, INTERVAL 6 DAY)
GROUP BY k.age_group
ORDER BY k.age_group;
