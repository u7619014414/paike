package com.yl.paike.teacher.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SYS_USERS")
public class SysUser {

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("USERNAME")
    private String username;

    @TableField("PASSWORD")
    private String password;

    /**
     * 用户类型：1-管理员, 2-教师, 3-学生
     */
    @TableField("USER_TYPE")
    private Integer userType;

    /**
     * 关联ID：根据用户类型关联到教师表或学生表的ID
     */
    @TableField("RELATED_ID")
    private Long relatedId;

    @TableField("IS_ACTIVE")
    private Boolean isActive;

    @TableField("LAST_LOGIN_TIME")
    private LocalDateTime lastLoginTime;

    @TableField(value = "CREATED_AT", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "UPDATED_AT", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 用户显示名称（非数据库字段）
     */
    @TableField(exist = false)
    private String displayName;
}
