package com.school.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.course.entity.CourseSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CourseScheduleMapper extends BaseMapper<CourseSchedule> {

    @Select("SELECT * FROM T_COURSE_SCHEDULE WHERE SCHEDULE_DATE BETWEEN #{startDate} AND #{endDate} AND STATUS = 1 ORDER BY SCHEDULE_DATE")
    List<CourseSchedule> findWeekSchedule(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    @Select("SELECT * FROM T_COURSE_SCHEDULE WHERE TIME_SLOT_ID = #{timeSlotId} AND CLASSROOM_ID = #{classroomId} AND SCHEDULE_DATE = #{date} AND STATUS = 1")
    CourseSchedule findConflictSchedule(@Param("timeSlotId") Long timeSlotId,
                                       @Param("classroomId") Long classroomId,
                                       @Param("date") LocalDate date);
}
