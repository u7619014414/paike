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

@TableName("J_CLASSROOMS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Classroom {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("classroom_code")
    private String classroomCode;

    @TableField("classroom_name")
    private String classroomName;

    @TableField("max_capacity")
    private Integer maxCapacity;

    @TableField("location")
    private String location;

    @TableField("facilities")
    private String facilities;

    @TableField("is_active")
    private Boolean isActive = true;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public void generateClassroomCode() {
        if (this.classroomCode == null || this.classroomCode.isEmpty()) {
            this.classroomCode = "CLS" + System.currentTimeMillis();
        }
    }
}
