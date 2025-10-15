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

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {

    private final StudentEnrollmentMapper enrollmentMapper;
    private final CourseScheduleMapper scheduleMapper;
    private final StudentMapper studentMapper;
    
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

        // 检查年龄组匹配
        if (!schedule.getCourse().getAgeGroup().equals(student.getAgeGroup())) {
            throw new BusinessException("学生年龄组不符合课程要求");
        }

        // 检查课程容量
        Integer currentCount = enrollmentMapper.countEnrolledStudents(courseScheduleId);
        if (currentCount >= schedule.getCourse().getMaxStudents()) {
            throw new BusinessException("课程已满，无法选课");
        }

        // 检查学生时间冲突
        StudentEnrollment conflict = enrollmentMapper.findStudentTimeConflict(
            studentId, schedule.getScheduleDate(), schedule.getTimeSlotId());

        if (conflict != null) {
            throw new BusinessException("学生在该时间段已有课程安排");
        }

        // 检查是否已经选过该课程
        StudentEnrollment existing = enrollmentMapper
            .findByStudentIdAndCourseScheduleIdAndEnrollmentStatus(studentId, courseScheduleId, 1);

        if (existing != null) {
            throw new BusinessException("已经选择过该课程");
        }

        // 创建选课记录
        StudentEnrollment enrollment = new StudentEnrollment();
        enrollment.setStudentId(studentId);
        enrollment.setCourseScheduleId(courseScheduleId);
        enrollment.setEnrollmentStatus(1);
        enrollment.setEnrollmentDate(LocalDateTime.now());

        enrollmentMapper.insert(enrollment);

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
}
