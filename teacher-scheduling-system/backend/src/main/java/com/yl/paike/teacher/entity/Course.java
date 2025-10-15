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

@TableName("K_COURSES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("course_code")
    private String courseCode;

    @TableField("course_name")
    private String courseName;

    @TableField("age_group")
    private Integer ageGroup;

    @TableField("max_students")
    private Integer maxStudents;

    @TableField("description")
    private String description;

    @TableField("is_active")
    private Boolean isActive = true;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public void generateCourseCode() {
        if (this.courseCode == null || this.courseCode.isEmpty()) {
            this.courseCode = "COU" + System.currentTimeMillis();
        }
    }
}
