package com.yl.paike.teacher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yl.paike.teacher.entity.TeacherAssignment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TeacherAssignmentMapper extends BaseMapper<TeacherAssignment> {

    /**
     * 根据教师ID查询所有教师分配
     * 使用 QueryWrapper 在 Service 层实现
     */
    @Select("SELECT * FROM L_TEACHER_ASSIGNMENTS WHERE teacher_id = #{teacherId}")
    List<TeacherAssignment> findByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 根据课程安排ID查询所有教师分配
     * 使用 QueryWrapper 在 Service 层实现
     */
    @Select("SELECT * FROM L_TEACHER_ASSIGNMENTS WHERE course_schedule_id = #{courseScheduleId}")
    List<TeacherAssignment> findByCourseScheduleId(@Param("courseScheduleId") Long courseScheduleId);

    /**
     * 根据课程安排ID删除教师分配
     */
    @Delete("DELETE FROM L_TEACHER_ASSIGNMENTS WHERE course_schedule_id = #{scheduleId}")
    void deleteByCourseScheduleId(@Param("scheduleId") Long scheduleId);

    /**
     * 根据教师ID和日期范围查询教师分配（关联课程安排）
     */
    @Select("SELECT ta.* FROM L_TEACHER_ASSIGNMENTS ta " +
            "JOIN T_COURSE_SCHEDULE cs ON ta.course_schedule_id = cs.id " +
            "WHERE ta.teacher_id = #{teacherId} " +
            "AND cs.schedule_date BETWEEN #{startDate} AND #{endDate} " +
            "AND cs.status = 1")
    List<TeacherAssignment> findByTeacherIdAndDateRange(@Param("teacherId") Long teacherId,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

    /**
     * 查找冲突的教师分配（同一教师在同一日期和时间段）
     */
    @Select("SELECT ta.* FROM L_TEACHER_ASSIGNMENTS ta " +
            "JOIN T_COURSE_SCHEDULE cs ON ta.course_schedule_id = cs.id " +
            "WHERE ta.teacher_id = #{teacherId} " +
            "AND cs.schedule_date = #{date} " +
            "AND cs.time_slot_id = #{timeSlotId} " +
            "AND cs.status = 1")
    List<TeacherAssignment> findConflictingAssignments(@Param("teacherId") Long teacherId,
                                                       @Param("date") LocalDate date,
                                                       @Param("timeSlotId") Long timeSlotId);

    /**
     * 检查教师是否已分配到指定课程安排
     * 使用 QueryWrapper 在 Service 层实现: lambdaQuery().eq(...).exists()
     */
    @Select("SELECT COUNT(*) > 0 FROM L_TEACHER_ASSIGNMENTS WHERE teacher_id = #{teacherId} AND course_schedule_id = #{courseScheduleId}")
    boolean existsByTeacherIdAndCourseScheduleId(@Param("teacherId") Long teacherId,
                                                  @Param("courseScheduleId") Long courseScheduleId);
}
