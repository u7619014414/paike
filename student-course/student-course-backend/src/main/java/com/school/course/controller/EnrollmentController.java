// EnrollmentController.java
package com.school.course.controller;

import com.school.course.dto.ApiResponse;
import com.school.course.dto.EnrollmentDTO;
import com.school.course.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Slf4j
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /**
     * 学生选课
     */
    @PostMapping
    public ApiResponse<EnrollmentDTO> enrollCourse(@RequestParam Long studentId,
                                                   @RequestParam Long courseScheduleId) {
        EnrollmentDTO enrollment = enrollmentService.enrollCourse(studentId, courseScheduleId);
        return ApiResponse.success(enrollment, "选课成功");
    }

    /**
     * 取消选课
     */
    @DeleteMapping
    public ApiResponse<Void> cancelEnrollment(@RequestParam Long studentId,
                                              @RequestParam Long courseScheduleId) {
        enrollmentService.cancelEnrollment(studentId, courseScheduleId);
        return ApiResponse.success(null, "取消选课成功");
    }
}
