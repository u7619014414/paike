package com.school.course.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EnrollmentDTO {
    private Long studentId;
    private Long courseScheduleId;
    private Integer enrollmentStatus;
    private LocalDateTime enrollmentDate;
}
