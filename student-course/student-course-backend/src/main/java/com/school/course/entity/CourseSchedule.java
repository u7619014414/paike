// 4. CourseSchedule 实体类
package com.school.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("T_COURSE_SCHEDULE")
public class CourseSchedule {
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("COURSE_ID")
    private Long courseId;

    @TableField("TIME_SLOT_ID")
    private Long timeSlotId;

    @TableField("CLASSROOM_ID")
    private Long classroomId;

    @TableField("SCHEDULE_DATE")
    private LocalDate scheduleDate;

    @TableField("STATUS")
    private Integer status = 1; // 1-正常 2-取消 3-调课

    @TableField(value = "CREATED_AT", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "UPDATED_AT", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联对象（用于返回给前端，不映射到数据库）
    @TableField(exist = false)
    private Course course;

    @TableField(exist = false)
    private TimeSlot timeSlot;

    @TableField(exist = false)
    private Classroom classroom;

}
