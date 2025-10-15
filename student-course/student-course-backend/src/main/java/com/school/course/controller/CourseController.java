// CourseController.java
package com.school.course.controller;

import com.school.course.dto.ApiResponse;
import com.school.course.dto.CourseScheduleViewDTO;
import com.school.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {

    private final CourseService courseService;

    /**
     * 获取课程表视图
     */
    @GetMapping("/schedule")
    public ApiResponse<List<CourseScheduleViewDTO>> getScheduleView(
            @RequestParam Integer studentAgeGroup,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate) {

        List<CourseScheduleViewDTO> scheduleView = courseService.getWeekScheduleView(studentAgeGroup, startDate);
        return ApiResponse.success(scheduleView);
    }
}