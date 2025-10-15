package com.yl.paike.teacher.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@TableName("L_TEACHER_ASSIGNMENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAssignment {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("teacher_id")
    private Long teacherId;

    @TableField("course_schedule_id")
    private Long courseScheduleId;

    @TableField("is_main_teacher")
    private Boolean isMainTeacher = false;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private Teacher teacher;

    @TableField(exist = false)
    private CourseSchedule courseSchedule;
}
