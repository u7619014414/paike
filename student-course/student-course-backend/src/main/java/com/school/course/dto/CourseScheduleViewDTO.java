package com.school.course.dto;

import lombok.Data;
import java.util.List;

@Data
public class CourseScheduleViewDTO {
    private Integer dayOfWeek;
    private String dayName;
    private List<TimeSlotViewDTO> timeSlots;
}
