package com.school.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.course.entity.StudentEnrollment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StudentEnrollmentMapper extends BaseMapper<StudentEnrollment> {

    @Select("SELECT COUNT(*) FROM S_STUDENT_ENROLLMENTS WHERE COURSE_SCHEDULE_ID = #{scheduleId} AND ENROLLMENT_STATUS = 1")
    Integer countEnrolledStudents(@Param("scheduleId") Long scheduleId);

    @Select("SELECT e.* FROM S_STUDENT_ENROLLMENTS e " +
            "INNER JOIN T_COURSE_SCHEDULE cs ON e.COURSE_SCHEDULE_ID = cs.ID " +
            "WHERE e.STUDENT_ID = #{studentId} " +
            "AND cs.TIME_SLOT_ID = #{timeSlotId} " +
            "AND e.ENROLLMENT_STATUS = 1 " +
            "LIMIT 1")
    StudentEnrollment findStudentTimeSlotConflict(@Param("studentId") Long studentId,
                                                  @Param("timeSlotId") Long timeSlotId);

    @Select("SELECT * FROM S_STUDENT_ENROLLMENTS WHERE STUDENT_ID = #{studentId} AND COURSE_SCHEDULE_ID = #{courseScheduleId} AND ENROLLMENT_STATUS = #{enrollmentStatus}")
    StudentEnrollment findByStudentIdAndCourseScheduleIdAndEnrollmentStatus(
        @Param("studentId") Long studentId,
        @Param("courseScheduleId") Long courseScheduleId,
        @Param("enrollmentStatus") Integer enrollmentStatus);

    @Select("SELECT COURSE_SCHEDULE_ID FROM S_STUDENT_ENROLLMENTS WHERE STUDENT_ID = #{studentId} AND ENROLLMENT_STATUS = 1")
    List<Long> findEnrolledScheduleIdsByStudent(@Param("studentId") Long studentId);
}
