package com.yl.paike.teacher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yl.paike.teacher.entity.Teacher;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {

    /**
     * 根据教师编号查询教师
     * 使用 QueryWrapper 在 Service 层实现
     */
    @Select("SELECT * FROM L_TEACHERS WHERE teacher_code = #{teacherCode}")
    Teacher findByTeacherCode(@Param("teacherCode") String teacherCode);

    /**
     * 根据手机号查询教师
     * 使用 QueryWrapper 在 Service 层实现
     */
    @Select("SELECT * FROM L_TEACHERS WHERE phone = #{phone}")
    Teacher findByPhone(@Param("phone") String phone);

    /**
     * 查询所有激活的教师
     * 使用 QueryWrapper 在 Service 层实现: lambdaQuery().eq(Teacher::getIsActive, true).list()
     */
    @Select("SELECT * FROM L_TEACHERS WHERE is_active = 1")
    List<Teacher> findByIsActiveTrue();

    /**
     * 根据年龄组和激活状态查询教师
     * age_groups 字段存储逗号分隔的值，如 "1,2,3"
     */
    @Select("SELECT * FROM L_TEACHERS WHERE is_active = 1 AND age_groups LIKE CONCAT('%', #{ageGroup}, '%')")
    List<Teacher> findByAgeGroupAndActive(@Param("ageGroup") String ageGroup);

    /**
     * 根据关键字搜索教师（姓名或手机号）
     */
    @Select("SELECT * FROM L_TEACHERS WHERE teacher_name LIKE CONCAT('%', #{keyword}, '%') OR phone LIKE CONCAT('%', #{keyword}, '%')")
    List<Teacher> searchByKeyword(@Param("keyword") String keyword);
}
