package com.school.course.service;

import com.school.course.entity.*;
import com.school.course.dto.EnrollmentDTO;
import com.school.course.mapper.*;
import com.school.course.exception.BusinessException;
import com.school.course.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {

    private final StudentEnrollmentMapper enrollmentMapper;
    private final CourseScheduleMapper scheduleMapper;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final ClassroomMapper classroomMapper;
    
    /**
     * 学生选课
     */
    public EnrollmentDTO enrollCourse(Long studentId, Long courseScheduleId) {
        // 验证学生是否存在且已通过审核
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new EntityNotFoundException("学生不存在");
        }

        if (student.getRegistrationStatus() != 2) {
            throw new BusinessException("学生尚未通过审核，无法选课");
        }

        // 验证课程安排是否存在
        CourseSchedule schedule = scheduleMapper.selectById(courseScheduleId);
        if (schedule == null) {
            throw new EntityNotFoundException("课程安排不存在");
        }

        // 加载关联的课程信息
        Course course = courseMapper.selectById(schedule.getCourseId());
        if (course == null) {
            throw new EntityNotFoundException("课程不存在");
        }
        schedule.setCourse(course);

        // 加载关联的教室信息
        Classroom classroom = classroomMapper.selectById(schedule.getClassroomId());
        if (classroom == null) {
            throw new EntityNotFoundException("教室不存在");
        }
        schedule.setClassroom(classroom);

        // 检查年龄组匹配
        if (!course.getAgeGroup().equals(student.getAgeGroup())) {
            throw new BusinessException("学生年龄组不符合课程要求");
        }

        // 检查教室容量（使用教室最大容量，而不是课程最大人数）
        Integer currentCount = enrollmentMapper.countEnrolledStudents(courseScheduleId);
        Integer classroomCapacity = classroom.getMaxCapacity();
        if (currentCount >= classroomCapacity) {
            throw new BusinessException("教室已满，无法选课");
        }

        // 检查学生时间段冲突（同一天的同一个时间段只能选一个课程）
        log.info("检查时间冲突 - 学生ID: {}, 要选课程安排ID: {}, TimeSlotId: {}, ScheduleDate: {}",
                 studentId, courseScheduleId, schedule.getTimeSlotId(), schedule.getScheduleDate());

        StudentEnrollment conflict = enrollmentMapper.findStudentTimeSlotConflict(
            studentId, schedule.getTimeSlotId(), schedule.getScheduleDate());

        if (conflict != null) {
            log.error("时间冲突 - 学生ID: {}, 已选课程安排ID: {}, 要选课程安排ID: {}",
                     studentId, conflict.getCourseScheduleId(), courseScheduleId);
            throw new BusinessException("该时间段已有课程安排，一个学生不能同时在多个教室上课");
        }

        // 检查是否之前取消过当前课程安排，如果有取消的记录，允许重新激活
        StudentEnrollment cancelled = enrollmentMapper
            .findByStudentIdAndCourseScheduleIdAndEnrollmentStatus(studentId, courseScheduleId, 2);

        log.info("检查选课 - 学生ID: {}, 课程ID: {}, 课程名: {}, 课程安排ID: {}, 是否有取消记录: {}",
                 studentId, course.getId(), course.getCourseName(), courseScheduleId,
                 cancelled != null ? "是(scheduleId:" + cancelled.getCourseScheduleId() + ")" : "否");

        // 注意：允许学生选择同一课程的不同时间段/教室，只要时间不冲突即可
        // 时间冲突检查已在前面完成（第74-79行）

        // 检查是否已经选过该课程安排（同一个课程安排不能重复选）
        StudentEnrollment existing = enrollmentMapper
            .findByStudentIdAndCourseScheduleIdAndEnrollmentStatus(studentId, courseScheduleId, 1);

        if (existing != null) {
            throw new BusinessException("已经选择过该课程");
        }

        // 如果之前取消过这个课程安排，重新激活；否则创建新记录
        StudentEnrollment enrollment;
        if (cancelled != null) {
            // 重新激活之前取消的记录
            log.info("重新激活取消的选课记录 - 学生ID: {}, 课程安排ID: {}, 记录ID: {}",
                     studentId, courseScheduleId, cancelled.getId());
            cancelled.setEnrollmentStatus(1);
            cancelled.setEnrollmentDate(LocalDateTime.now());
            enrollmentMapper.updateById(cancelled);
            enrollment = cancelled;
        } else {
            // 创建新的选课记录
            log.info("创建新的选课记录 - 学生ID: {}, 课程安排ID: {}", studentId, courseScheduleId);
            enrollment = new StudentEnrollment();
            enrollment.setStudentId(studentId);
            enrollment.setCourseScheduleId(courseScheduleId);
            enrollment.setEnrollmentStatus(1);
            enrollment.setEnrollmentDate(LocalDateTime.now());
            enrollmentMapper.insert(enrollment);
        }

        log.info("学生选课成功，学生ID：{}，课程安排ID：{}", studentId, courseScheduleId);

        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setStudentId(enrollment.getStudentId());
        dto.setCourseScheduleId(enrollment.getCourseScheduleId());
        dto.setEnrollmentStatus(enrollment.getEnrollmentStatus());
        dto.setEnrollmentDate(enrollment.getEnrollmentDate());

        return dto;
    }
    
    /**
     * 取消选课
     */
    public void cancelEnrollment(Long studentId, Long courseScheduleId) {
        StudentEnrollment enrollment = enrollmentMapper
            .findByStudentIdAndCourseScheduleIdAndEnrollmentStatus(studentId, courseScheduleId, 1);

        if (enrollment == null) {
            throw new EntityNotFoundException("选课记录不存在");
        }

        enrollment.setEnrollmentStatus(2); // 已取消
        enrollmentMapper.updateById(enrollment);

        log.info("取消选课成功，学生ID：{}，课程安排ID：{}", studentId, courseScheduleId);
    }

    /**
     * 获取学生已选课程的scheduleId列表
     */
    public List<Long> getStudentEnrolledScheduleIds(Long studentId) {
        return enrollmentMapper.findEnrolledScheduleIdsByStudent(studentId);
    }
}
