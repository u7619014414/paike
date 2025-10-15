package com.yl.paike.teacher.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("T_COURSE_SCHEDULE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseSchedule {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("course_id")
    private Long courseId;

    @TableField("time_slot_id")
    private Long timeSlotId;

    @TableField("classroom_id")
    private Long classroomId;

    @TableField("schedule_date")
    private LocalDate scheduleDate;

    @TableField("status")
    private Integer status = 1;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private Course course;

    @TableField(exist = false)
    private TimeSlot timeSlot;

    @TableField(exist = false)
    private Classroom classroom;
}
