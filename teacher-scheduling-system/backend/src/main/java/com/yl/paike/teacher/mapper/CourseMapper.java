package com.yl.paike.teacher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yl.paike.teacher.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 根据课程编号查询课程
     * 使用 QueryWrapper 在 Service 层实现
     */
    @Select("SELECT * FROM K_COURSES WHERE course_code = #{courseCode}")
    Course findByCourseCode(@Param("courseCode") String courseCode);

    /**
     * 查询所有激活的课程
     * 使用 QueryWrapper 在 Service 层实现: lambdaQuery().eq(Course::getIsActive, true).list()
     */
    @Select("SELECT * FROM K_COURSES WHERE is_active = 1")
    List<Course> findByIsActiveTrue();

    /**
     * 根据年龄组和激活状态查询课程
     * 使用 QueryWrapper 在 Service 层实现
     */
    @Select("SELECT * FROM K_COURSES WHERE age_group = #{ageGroup} AND is_active = 1")
    List<Course> findByAgeGroupAndIsActiveTrue(@Param("ageGroup") Integer ageGroup);

    /**
     * 根据课程名称模糊查询
     * 使用 QueryWrapper 在 Service 层实现: lambdaQuery().like(Course::getCourseName, name).list()
     */
    @Select("SELECT * FROM K_COURSES WHERE course_name LIKE CONCAT('%', #{name}, '%')")
    List<Course> findByCourseNameContaining(@Param("name") String name);
}
