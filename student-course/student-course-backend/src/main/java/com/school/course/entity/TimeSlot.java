package com.school.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("T_TIME_SLOTS")
public class TimeSlot {
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("SLOT_NAME")
    private String slotName;

    @TableField("START_TIME")
    private LocalTime startTime;

    @TableField("END_TIME")
    private LocalTime endTime;

    @TableField("DAY_OF_WEEK")
    private Integer dayOfWeek; // 1-7 (周一到周日)

    @TableField("IS_ACTIVE")
    private Boolean isActive = true;

    @TableField(value = "CREATED_AT", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "UPDATED_AT", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}