// 3. Course 实体类
package com.school.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("K_COURSES")
public class Course {
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("COURSE_CODE")
    private String courseCode;

    @TableField("COURSE_NAME")
    private String courseName;

    @TableField("AGE_GROUP")
    private Integer ageGroup; // 对应数据库的 tinyint

    @TableField("MAX_STUDENTS")
    private Integer maxStudents;

    @TableField("DESCRIPTION")
    private String description;

    @TableField("TEACHER_NAMES")
    private String teacherNames; // 教师姓名（多个用逗号分隔）

    @TableField("IS_ACTIVE")
    private Boolean isActive = true;

    @TableField(value = "CREATED_AT", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "UPDATED_AT", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public void generateCourseCode() {
        if (this.courseCode == null) {
            this.courseCode = "COU" + System.currentTimeMillis();
        }
    }
}
