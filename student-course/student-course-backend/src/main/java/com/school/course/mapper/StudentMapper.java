package com.school.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.course.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    @Select("SELECT * FROM S_STUDENTS WHERE STUDENT_CODE = #{studentCode}")
    Student findByStudentCode(@Param("studentCode") String studentCode);

    @Select("SELECT * FROM S_STUDENTS WHERE PARENT_PHONE = #{phone}")
    Student findByParentPhone(@Param("phone") String phone);

    @Select("SELECT * FROM S_STUDENTS WHERE STUDENT_NAME LIKE CONCAT('%', #{name}, '%') OR PARENT_NAME LIKE CONCAT('%', #{name}, '%')")
    List<Student> findByNameContaining(@Param("name") String name);

    @Select("SELECT * FROM S_STUDENTS WHERE IS_ACTIVE = 1 ORDER BY CREATED_AT DESC")
    List<Student> findActiveStudents();
}
