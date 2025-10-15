package com.yl.paike.teacher.service;

import com.yl.paike.teacher.dto.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {

    CourseScheduleDTO createSchedule(CourseScheduleCreateDTO createDTO);

    CourseScheduleDTO updateSchedule(Long scheduleId, CourseScheduleCreateDTO updateDTO);

    void deleteSchedule(Long scheduleId);

    ScheduleAssignmentDTO assignTeachers(ScheduleAssignmentRequestDTO requestDTO);

    Page<CourseScheduleDTO> getScheduleList(LocalDate startDate, LocalDate endDate, Integer status, Page page);

    void cancelSchedule(Long scheduleId, String reason);

    List<CourseScheduleDTO> getTeacherSchedules(Long teacherId, LocalDate startDate, LocalDate endDate);
}

