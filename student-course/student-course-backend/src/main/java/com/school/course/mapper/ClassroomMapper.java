package com.school.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.course.entity.Classroom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ClassroomMapper extends BaseMapper<Classroom> {

    @Select("SELECT * FROM J_CLASSROOMS WHERE CLASSROOM_CODE = #{classroomCode}")
    Classroom findByClassroomCode(@Param("classroomCode") String classroomCode);
}
