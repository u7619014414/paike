package com.school.course.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class StudentRegistrationDTO {
    @NotBlank(message = "学生姓名不能为空")
    @Size(min = 2, max = 20, message = "学生姓名长度应在2-20个字符")
    private String studentName;
    
    @NotNull(message = "年龄不能为空")
    @Min(value = 1, message = "年龄不能小于1岁")
    @Max(value = 12, message = "年龄不能大于12岁")
    private Integer age;
    
    @NotBlank(message = "家长姓名不能为空")
    @Size(min = 2, max = 20, message = "家长姓名长度应在2-20个字符")
    private String parentName;
    
    @NotBlank(message = "家长电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String parentPhone;
    
    @Email(message = "邮箱格式不正确")
    private String parentEmail;
}
