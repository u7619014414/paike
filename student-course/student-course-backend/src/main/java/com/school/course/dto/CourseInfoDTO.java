package com.school.course.dto;

import lombok.Data;
import java.util.List;

@Data
public class CourseInfoDTO {
    private Long courseId;
    private Long scheduleId;
    private String courseName;
    private String classroomName;
    private List<String> teacherNames;
    private Integer maxStudents;
    private Integer currentStudents;
    private String status; // FULL(红色), AVAILABLE(黄色), EMPTY(白色), DISABLED(灰色)
    private Integer ageGroup;
    private Boolean canEnroll;
}
