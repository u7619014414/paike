// TimeSlotController.java
package com.school.course.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.course.dto.ApiResponse;
import com.school.course.entity.TimeSlot;
import com.school.course.mapper.TimeSlotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/time-slots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotMapper timeSlotMapper;

    /**
     * 获取所有时间段
     */
    @GetMapping
    public ApiResponse<List<TimeSlot>> getAllTimeSlots() {
        LambdaQueryWrapper<TimeSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TimeSlot::getIsActive, true)
               .orderByAsc(TimeSlot::getDayOfWeek, TimeSlot::getStartTime);
        List<TimeSlot> timeSlots = timeSlotMapper.selectList(wrapper);
        return ApiResponse.success(timeSlots);
    }
}