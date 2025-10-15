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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@TableName("L_TEACHERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Teacher {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("teacher_code")
    private String teacherCode;

    @TableField("teacher_name")
    private String teacherName;

    @TableField("phone")
    private String phone;

    @TableField("email")
    private String email;

    @TableField("specialties")
    private String specialties;

    @TableField("age_groups")
    private String ageGroups;

    @TableField("is_active")
    private Boolean isActive = true;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public void generateTeacherCode() {
        if (this.teacherCode == null || this.teacherCode.isEmpty()) {
            this.teacherCode = "TEA" + System.currentTimeMillis();
        }
    }

    // Transient field for API convenience - not stored in database
    @TableField(exist = false)
    private List<Integer> ageGroupList;

    public List<Integer> getAgeGroupList() {
        if (this.ageGroupList != null) {
            return this.ageGroupList;
        }
        if (this.ageGroups == null || this.ageGroups.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(this.ageGroups.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public void setAgeGroupList(List<Integer> ageGroupList) {
        this.ageGroupList = ageGroupList;
        if (ageGroupList != null && !ageGroupList.isEmpty()) {
            this.ageGroups = ageGroupList.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        } else {
            this.ageGroups = "";
        }
    }
}
