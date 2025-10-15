// 7. StudentEnrollment 实体类
package com.school.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("S_STUDENT_ENROLLMENTS")
public class StudentEnrollment {
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("STUDENT_ID")
    private Long studentId;

    @TableField("COURSE_SCHEDULE_ID")
    private Long courseScheduleId;

    @TableField("ENROLLMENT_STATUS")
    private Integer enrollmentStatus = 1; // 1-已选 2-已取消

    @TableField("ENROLLMENT_DATE")
    private LocalDateTime enrollmentDate;

    @TableField(value = "CREATED_AT", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "UPDATED_AT", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联对象（用于返回给前端，不映射到数据库）
    @TableField(exist = false)
    private Student student;

    @TableField(exist = false)
    private CourseSchedule courseSchedule;
}
