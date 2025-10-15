package com.yl.paike.teacher.service.impl;

import com.yl.paike.teacher.dto.TimeSlotDTO;
import com.yl.paike.teacher.entity.TimeSlot;
import com.yl.paike.teacher.mapper.TimeSlotMapper;
import com.yl.paike.teacher.service.TimeSlotService;
import com.yl.paike.teacher.util.BeanConverter;
import com.yl.paike.teacher.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotMapper timeSlotMapper;

    @Override
    public List<TimeSlotDTO> getAllActiveTimeSlots() {
        List<TimeSlot> timeSlots = timeSlotMapper.findByIsActiveTrueOrderByDayOfWeekAscStartTimeAsc();
        return timeSlots.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlotDTO> getTimeSlotsByDayOfWeek(Integer dayOfWeek) {
        List<TimeSlot> timeSlots = timeSlotMapper.findByDayOfWeekAndIsActiveTrue(dayOfWeek);
        return timeSlots.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    private TimeSlotDTO convertToDTO(TimeSlot timeSlot) {
        TimeSlotDTO dto = BeanConverter.convert(timeSlot, TimeSlotDTO.class);
        dto.setDayOfWeekName(Constants.WEEK_DAYS[timeSlot.getDayOfWeek() - 1]);
        return dto;
    }
}
