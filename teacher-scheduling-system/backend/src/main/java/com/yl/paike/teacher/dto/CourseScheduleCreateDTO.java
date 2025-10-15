package com.yl.paike.teacher.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseScheduleCreateDTO {
    
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    @NotNull(message = "时间段ID不能为空")
    private Long timeSlotId;
    
    @NotNull(message = "教室ID不能为空")
    private Long classroomId;
    
    @NotNull(message = "上课日期不能为空")
    private LocalDate scheduleDate;
}
