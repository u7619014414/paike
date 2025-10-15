package com.school.course.dto;

import lombok.Data;
import java.time.LocalTime;
import java.util.List;

@Data
public class TimeSlotViewDTO {
    private Long timeSlotId;
    private String slotName;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<CourseInfoDTO> courses;
}
