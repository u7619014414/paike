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

@TableName("SYS_CONFLICT_WARNINGS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConflictWarning {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("conflict_type")
    private Integer conflictType;

    @TableField("conflict_description")
    private String conflictDescription;

    @TableField("related_ids")
    private String relatedIds;

    @TableField("conflict_date")
    private LocalDate conflictDate;

    @TableField("time_slot_id")
    private Long timeSlotId;

    @TableField("status")
    private Integer status = 1;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
