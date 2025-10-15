package com.yl.paike.teacher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yl.paike.teacher.entity.Classroom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ClassroomMapper extends BaseMapper<Classroom> {

    /**
     * 根据教室编号查询教室
     * 使用 QueryWrapper 在 Service 层实现
     */
    @Select("SELECT * FROM J_CLASSROOMS WHERE classroom_code = #{classroomCode}")
    Classroom findByClassroomCode(@Param("classroomCode") String classroomCode);

    /**
     * 查询所有激活的教室
     * 使用 QueryWrapper 在 Service 层实现: lambdaQuery().eq(Classroom::getIsActive, true).list()
     */
    @Select("SELECT * FROM J_CLASSROOMS WHERE is_active = 1")
    List<Classroom> findByIsActiveTrue();

    /**
     * 根据名称模糊查询教室
     * 使用 QueryWrapper 在 Service 层实现: lambdaQuery().like(Classroom::getClassroomName, name).list()
     */
    @Select("SELECT * FROM J_CLASSROOMS WHERE classroom_name LIKE CONCAT('%', #{name}, '%')")
    List<Classroom> findByClassroomNameContaining(@Param("name") String name);
}
