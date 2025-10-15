// 1. Student 实体类
package com.school.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("S_STUDENTS")
public class Student {
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("STUDENT_CODE")
    private String studentCode;

    @TableField("PASSWORD")
    private String password;

    @TableField("STUDENT_NAME")
    private String studentName;

    @TableField("AGE")
    private Integer age; // 对应数据库的 tinyint

    @TableField("AGE_GROUP")
    private Integer ageGroup; // 对应数据库的 tinyint

    @TableField("PARENT_NAME")
    private String parentName;

    @TableField("PARENT_PHONE")
    private String parentPhone;

    @TableField("PARENT_EMAIL")
    private String parentEmail;

    @TableField("REGISTRATION_STATUS")
    private Integer registrationStatus = 2; // 1-待审核 2-已通过 3-已拒绝

    @TableField("IS_ACTIVE")
    private Boolean isActive = true;

    @TableField(value = "CREATED_AT", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "UPDATED_AT", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 生成学生编码的逻辑移到 Service 层或使用 MyBatis Plus 的填充功能
    public void generateStudentCode() {
        if (this.studentCode == null) {
            this.studentCode = "STU" + System.currentTimeMillis();
        }
        // 根据年龄自动设置年龄组
        if (this.age != null) {
            if (this.age >= 1 && this.age <= 3) {
                this.ageGroup = 1; // 学前班
            } else if (this.age > 3 && this.age <= 6) {
                this.ageGroup = 2; // 小班
            } else if (this.age > 6 && this.age <= 9) {
                this.ageGroup = 3; // 中班
            } else if (this.age > 9 && this.age <= 12) {
                this.ageGroup = 4; // 大班
            }
        }
    }
}
