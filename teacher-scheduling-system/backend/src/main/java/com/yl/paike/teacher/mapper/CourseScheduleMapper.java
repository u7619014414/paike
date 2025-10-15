package com.yl.paike.teacher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yl.paike.teacher.entity.CourseSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CourseScheduleMapper extends BaseMapper<CourseSchedule> {

    /**
     * 根据日期和状态查询课程安排
     * 使用 QueryWrapper 在 Service 层实现
     */
    @Select("SELECT * FROM T_COURSE_SCHEDULE WHERE schedule_date = #{date} AND status = #{status}")
    List<CourseSchedule> findByScheduleDateAndStatus(@Param("date") LocalDate date, @Param("status") Integer status);

    /**
     * 根据日期范围查询激活的课程安排
     */
    @Select("SELECT * FROM T_COURSE_SCHEDULE WHERE schedule_date BETWEEN #{startDate} AND #{endDate} AND status = 1 ORDER BY schedule_date, time_slot_id")
    List<CourseSchedule> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查找冲突的课程安排（同一时间段、同一教室）
     */
    @Select("SELECT * FROM T_COURSE_SCHEDULE WHERE time_slot_id = #{timeSlotId} AND classroom_id = #{classroomId} AND schedule_date = #{date} AND status = 1 LIMIT 1")
    CourseSchedule findConflictSchedule(@Param("timeSlotId") Long timeSlotId, @Param("classroomId") Long classroomId, @Param("date") LocalDate date);

    /**
     * 根据课程ID和状态查询课程安排
     * 使用 QueryWrapper 在 Service 层实现
     */
    @Select("SELECT * FROM T_COURSE_SCHEDULE WHERE course_id = #{courseId} AND status = #{status}")
    List<CourseSchedule> findByCourseIdAndStatus(@Param("courseId") Long courseId, @Param("status") Integer status);

    /**
     * 根据ID查询课程安排（包含关联对象）
     * 注意：MyBatis Plus 中关联查询需要在 Service 层手动加载或使用 ResultMap
     */
    @Select("SELECT cs.*, " +
            "c.id as course_id, c.course_code, c.course_name, c.age_group as course_age_group, c.description as course_description, c.max_capacity, c.is_active as course_is_active, c.created_at as course_created_at, c.updated_at as course_updated_at, " +
            "ts.id as time_slot_id, ts.day_of_week, ts.start_time, ts.end_time, ts.is_active as time_slot_is_active, ts.created_at as time_slot_created_at, ts.updated_at as time_slot_updated_at, " +
            "cr.id as classroom_id, cr.classroom_code, cr.classroom_name, cr.capacity, cr.is_active as classroom_is_active, cr.created_at as classroom_created_at, cr.updated_at as classroom_updated_at " +
            "FROM T_COURSE_SCHEDULE cs " +
            "LEFT JOIN K_COURSES c ON cs.course_id = c.id " +
            "LEFT JOIN T_TIME_SLOTS ts ON cs.time_slot_id = ts.id " +
            "LEFT JOIN J_CLASSROOMS cr ON cs.classroom_id = cr.id " +
            "WHERE cs.id = #{id}")
    CourseSchedule findByIdWithRelations(@Param("id") Long id);

    /**
     * 查询所有课程安排（带条件过滤和关联对象）
     */
    @Select("<script>" +
            "SELECT cs.*, " +
            "c.id as course_id, c.course_code, c.course_name, c.age_group as course_age_group, c.description as course_description, c.max_capacity, c.is_active as course_is_active, c.created_at as course_created_at, c.updated_at as course_updated_at, " +
            "ts.id as time_slot_id, ts.day_of_week, ts.start_time, ts.end_time, ts.is_active as time_slot_is_active, ts.created_at as time_slot_created_at, ts.updated_at as time_slot_updated_at, " +
            "cr.id as classroom_id, cr.classroom_code, cr.classroom_name, cr.capacity, cr.is_active as classroom_is_active, cr.created_at as classroom_created_at, cr.updated_at as classroom_updated_at " +
            "FROM T_COURSE_SCHEDULE cs " +
            "LEFT JOIN K_COURSES c ON cs.course_id = c.id " +
            "LEFT JOIN T_TIME_SLOTS ts ON cs.time_slot_id = ts.id " +
            "LEFT JOIN J_CLASSROOMS cr ON cs.classroom_id = cr.id " +
            "WHERE 1=1 " +
            "<if test='startDate != null'> AND cs.schedule_date &gt;= #{startDate} </if> " +
            "<if test='endDate != null'> AND cs.schedule_date &lt;= #{endDate} </if> " +
            "<if test='status != null'> AND cs.status = #{status} </if> " +
            "ORDER BY cs.schedule_date DESC, cs.time_slot_id" +
            "</script>")
    List<CourseSchedule> findAllWithDetails(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate,
                                            @Param("status") Integer status);
}
