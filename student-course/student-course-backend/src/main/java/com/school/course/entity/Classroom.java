// 2. Classroom 实体类
package com.school.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("J_CLASSROOMS")
public class Classroom {
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("CLASSROOM_CODE")
    private String classroomCode;

    @TableField("CLASSROOM_NAME")
    private String classroomName;

    @TableField("MAX_CAPACITY")
    private Integer maxCapacity;

    @TableField("LOCATION")
    private String location;

    @TableField("FACILITIES")
    private String facilities;

    @TableField("IS_ACTIVE")
    private Boolean isActive = true;

    @TableField(value = "CREATED_AT", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "UPDATED_AT", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public void generateClassroomCode() {
        if (this.classroomCode == null) {
            this.classroomCode = "CLS" + System.currentTimeMillis();
        }
    }
}
