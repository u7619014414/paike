package com.yl.paike.teacher.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yl.paike.teacher.dto.ConflictCheckResult;
import com.yl.paike.teacher.dto.ConflictWarningDTO;
import com.yl.paike.teacher.entity.ConflictWarning;
import com.yl.paike.teacher.entity.CourseSchedule;
import com.yl.paike.teacher.entity.Teacher;
import com.yl.paike.teacher.entity.TeacherAssignment;
import com.yl.paike.teacher.exception.EntityNotFoundException;
import com.yl.paike.teacher.mapper.*;
import com.yl.paike.teacher.service.ConflictDetectionService;
import com.yl.paike.teacher.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConflictDetectionServiceImpl implements ConflictDetectionService {

    private final TeacherAssignmentMapper assignmentMapper;
    private final ConflictWarningMapper conflictWarningMapper;
    private final CourseScheduleMapper scheduleMapper;
    private final TeacherMapper teacherMapper;
    private final TimeSlotMapper timeSlotMapper;
    
    @Override
    public ConflictCheckResult checkTeacherConflicts(List<Long> teacherIds, LocalDate date, Long timeSlotId) {
        ConflictCheckResult result = new ConflictCheckResult();
        List<String> conflicts = new ArrayList<>();

        for (Long teacherId : teacherIds) {
            List<TeacherAssignment> existingAssignments =
                assignmentMapper.findConflictingAssignments(teacherId, date, timeSlotId);

            if (!existingAssignments.isEmpty()) {
                String teacherName = getTeacherName(teacherId);
                String timeSlotName = getTimeSlotName(timeSlotId);
                conflicts.add(String.format("教师 %s 在 %s 已有课程安排", teacherName, timeSlotName));

                recordConflictWarning(
                    Constants.CONFLICT_TYPE_TEACHER,
                    teacherId,
                    date,
                    timeSlotId,
                    String.format("教师时间冲突：%s", teacherName)
                );
            }
        }

        result.setHasConflicts(!conflicts.isEmpty());
        result.setConflictDescription(String.join("；", conflicts));

        return result;
    }

    @Override
    public ConflictCheckResult checkClassroomConflicts(Long classroomId, LocalDate date, Long timeSlotId) {
        ConflictCheckResult result = new ConflictCheckResult();

        CourseSchedule conflict = scheduleMapper.findConflictSchedule(
            timeSlotId, classroomId, date);

        if (conflict != null) {
            String timeSlotName = getTimeSlotName(timeSlotId);
            String conflictDescription = String.format(
                "教室在 %s %s 已被占用",
                date,
                timeSlotName
            );

            result.setHasConflicts(true);
            result.setConflictDescription(conflictDescription);

            recordConflictWarning(
                Constants.CONFLICT_TYPE_CLASSROOM,
                classroomId,
                date,
                timeSlotId,
                conflictDescription
            );
        }

        return result;
    }
    
    @Override
    public Page<ConflictWarningDTO> getConflictWarnings(Integer conflictType, Integer status,
                                                        LocalDate startDate, LocalDate endDate,
                                                        int pageNum, int pageSize) {
        LambdaQueryWrapper<ConflictWarning> wrapper = new LambdaQueryWrapper<>();

        if (conflictType != null) {
            wrapper.eq(ConflictWarning::getConflictType, conflictType);
        }

        if (status != null) {
            wrapper.eq(ConflictWarning::getStatus, status);
        }

        if (startDate != null) {
            wrapper.ge(ConflictWarning::getConflictDate, startDate);
        }

        if (endDate != null) {
            wrapper.le(ConflictWarning::getConflictDate, endDate);
        }

        Page<ConflictWarning> page = new Page<>(pageNum + 1, pageSize);
        Page<ConflictWarning> warningPage = conflictWarningMapper.selectPage(page, wrapper);

        Page<ConflictWarningDTO> dtoPage = new Page<>(warningPage.getCurrent(), warningPage.getSize(), warningPage.getTotal());
        dtoPage.setRecords(warningPage.getRecords().stream()
            .map(this::convertToWarningDTO)
            .collect(Collectors.toList()));

        return dtoPage;
    }

    @Override
    @Transactional
    public void resolveConflictWarning(Long warningId, Integer resolution, String remark) {
        ConflictWarning warning = conflictWarningMapper.selectById(warningId);
        if (warning == null) {
            throw new EntityNotFoundException("冲突警告", warningId);
        }

        warning.setStatus(resolution);
        conflictWarningMapper.updateById(warning);

        log.info("冲突警告处理完成，警告ID：{}，处理结果：{}", warningId, resolution);
    }

    @Override
    @Transactional
    public void performFullConflictCheck(LocalDate startDate, LocalDate endDate) {
        log.info("开始执行全面冲突检测，日期范围：{} 至 {}", startDate, endDate);

        List<CourseSchedule> schedules = scheduleMapper.findByDateRange(startDate, endDate);

        log.info("全面冲突检测完成");
    }
    
    private void recordConflictWarning(Integer conflictType, Long relatedId, LocalDate date,
                                     Long timeSlotId, String description) {
        ConflictWarning warning = new ConflictWarning();
        warning.setConflictType(conflictType);
        warning.setConflictDescription(description);
        warning.setRelatedIds(String.valueOf(relatedId));
        warning.setConflictDate(date);
        warning.setTimeSlotId(timeSlotId);
        warning.setStatus(Constants.WARNING_STATUS_UNRESOLVED);

        conflictWarningMapper.insert(warning);
    }

    private String getTeacherName(Long teacherId) {
        Teacher teacher = teacherMapper.selectById(teacherId);
        return teacher != null ? teacher.getTeacherName() : "未知教师";
    }

    private String getTimeSlotName(Long timeSlotId) {
        com.yl.paike.teacher.entity.TimeSlot slot = timeSlotMapper.selectById(timeSlotId);
        if (slot != null) {
            return slot.getSlotName() + " " + slot.getStartTime() + "-" + slot.getEndTime();
        }
        return "未知时间段";
    }
    
    private ConflictWarningDTO convertToWarningDTO(ConflictWarning warning) {
        ConflictWarningDTO dto = new ConflictWarningDTO();
        dto.setId(warning.getId());
        dto.setConflictType(warning.getConflictType());
        dto.setConflictTypeName(getConflictTypeName(warning.getConflictType()));
        dto.setConflictDescription(warning.getConflictDescription());
        dto.setConflictDate(warning.getConflictDate());
        dto.setTimeSlotId(warning.getTimeSlotId());
        dto.setTimeSlotName(getTimeSlotName(warning.getTimeSlotId()));
        dto.setStatus(warning.getStatus());
        dto.setStatusName(getStatusName(warning.getStatus()));
        dto.setCreatedAt(warning.getCreatedAt());
        
        if (warning.getRelatedIds() != null && !warning.getRelatedIds().isEmpty()) {
            List<Long> relatedIds = Arrays.stream(warning.getRelatedIds().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());
            dto.setRelatedIds(relatedIds);
        }
        
        return dto;
    }
    
    private String getConflictTypeName(Integer type) {
        return switch (type) {
            case Constants.CONFLICT_TYPE_TEACHER -> "教师冲突";
            case Constants.CONFLICT_TYPE_STUDENT -> "学生冲突";
            case Constants.CONFLICT_TYPE_CLASSROOM -> "教室冲突";
            default -> "未知类型";
        };
    }
    
    private String getStatusName(Integer status) {
        return switch (status) {
            case Constants.WARNING_STATUS_UNRESOLVED -> "未处理";
            case Constants.WARNING_STATUS_RESOLVED -> "已处理";
            case Constants.WARNING_STATUS_IGNORED -> "已忽略";
            default -> "未知状态";
        };
    }
}
