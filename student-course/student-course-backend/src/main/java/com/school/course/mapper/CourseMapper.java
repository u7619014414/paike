package com.school.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.course.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    @Select("SELECT * FROM K_COURSES WHERE COURSE_CODE = #{courseCode}")
    Course findByCourseCode(@Param("courseCode") String courseCode);
}
