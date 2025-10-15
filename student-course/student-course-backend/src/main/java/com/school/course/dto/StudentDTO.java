package com.school.course.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentDTO {
    private Long id;
    private String studentCode;
    private String studentName;
    private Integer age;
    private Integer ageGroup;
    private String parentName;
    private String parentPhone;
    private String parentEmail;
    private Integer registrationStatus;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
