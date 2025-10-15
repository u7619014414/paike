package com.yl.paike.teacher.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import com.yl.paike.teacher.dto.*;
import com.yl.paike.teacher.entity.*;
import com.yl.paike.teacher.exception.BusinessException;
import com.yl.paike.teacher.exception.EntityNotFoundException;
import com.yl.paike.teacher.mapper.*;
import com.yl.paike.teacher.service.ConflictDetectionService;
import com.yl.paike.teacher.service.ScheduleService;
import com.yl.paike.teacher.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor

public class ScheduleServiceImpl implements ScheduleService {

    private final CourseScheduleMapper scheduleMapper;
    private final TeacherMapper teacherMapper;
    private final TeacherAssignmentMapper assignmentMapper;
    private final CourseMapper courseMapper;
    private final ClassroomMapper classroomMapper;
    private final TimeSlotMapper timeSlotMapper;
    private final ConflictDetectionService conflictDetectionService;
    
    @Override
    @Transactional
    public CourseScheduleDTO createSchedule(CourseScheduleCreateDTO createDTO) {
        Course course = courseMapper.selectById(createDTO.getCourseId());
        if (course == null) {
            throw new EntityNotFoundException("课程", createDTO.getCourseId());
        }

        Classroom classroom = classroomMapper.selectById(createDTO.getClassroomId());
        if (classroom == null) {
            throw new EntityNotFoundException("教室", createDTO.getClassroomId());
        }

        TimeSlot timeSlot = timeSlotMapper.selectById(createDTO.getTimeSlotId());
        if (timeSlot == null) {
            throw new EntityNotFoundException("时间段", createDTO.getTimeSlotId());
        }

        ConflictCheckResult classroomConflict = conflictDetectionService.checkClassroomConflicts(
            createDTO.getClassroomId(), createDTO.getScheduleDate(), createDTO.getTimeSlotId());

        if (classroomConflict.getHasConflicts()) {
            throw new BusinessException("教室时间冲突：" + classroomConflict.getConflictDescription());
        }

        CourseSchedule schedule = new CourseSchedule();
        schedule.setCourseId(createDTO.getCourseId());
        schedule.setTimeSlotId(createDTO.getTimeSlotId());
        schedule.setClassroomId(createDTO.getClassroomId());
        schedule.setScheduleDate(createDTO.getScheduleDate());
        schedule.setStatus(Constants.SCHEDULE_STATUS_NORMAL);

        scheduleMapper.insert(schedule);
        log.info("课程安排创建成功，安排ID：{}", schedule.getId());

        return convertToDTO(schedule);
    }
    
    @Override
    @Transactional
    public CourseScheduleDTO updateSchedule(Long scheduleId, CourseScheduleCreateDTO updateDTO) {
        CourseSchedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new EntityNotFoundException("课程安排", scheduleId);
        }

        // 验证课程、教室和时间段是否存在
        Course course = courseMapper.selectById(updateDTO.getCourseId());
        if (course == null) {
            throw new EntityNotFoundException("课程", updateDTO.getCourseId());
        }

        Classroom classroom = classroomMapper.selectById(updateDTO.getClassroomId());
        if (classroom == null) {
            throw new EntityNotFoundException("教室", updateDTO.getClassroomId());
        }

        TimeSlot timeSlot = timeSlotMapper.selectById(updateDTO.getTimeSlotId());
        if (timeSlot == null) {
            throw new EntityNotFoundException("时间段", updateDTO.getTimeSlotId());
        }

        // 检查教室冲突（排除当前记录）
        CourseSchedule conflictSchedule = scheduleMapper.findConflictSchedule(updateDTO.getTimeSlotId(),
                updateDTO.getClassroomId(), updateDTO.getScheduleDate());
        if (conflictSchedule != null && !conflictSchedule.getId().equals(scheduleId)) {
            throw new BusinessException("该时间段教室已被占用");
        }

        // 更新信息
        schedule.setCourseId(updateDTO.getCourseId());
        schedule.setTimeSlotId(updateDTO.getTimeSlotId());
        schedule.setClassroomId(updateDTO.getClassroomId());
        schedule.setScheduleDate(updateDTO.getScheduleDate());

        scheduleMapper.updateById(schedule);
        log.info("课程安排更新成功，安排ID：{}", scheduleId);

        return convertToDTO(schedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        CourseSchedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new EntityNotFoundException("课程安排", scheduleId);
        }

        // 删除教师分配
        assignmentMapper.deleteByCourseScheduleId(scheduleId);

        // 删除课程安排
        scheduleMapper.deleteById(scheduleId);

        log.info("课程安排删除成功，安排ID：{}", scheduleId);
    }

    @Override
    @Transactional
    public ScheduleAssignmentDTO assignTeachers(ScheduleAssignmentRequestDTO requestDTO) {
        CourseSchedule schedule = scheduleMapper.selectById(requestDTO.getCourseScheduleId());
        if (schedule == null) {
            throw new EntityNotFoundException("课程安排", requestDTO.getCourseScheduleId());
        }

        if (requestDTO.getTeacherIds().size() < 1 || requestDTO.getTeacherIds().size() > 3) {
            throw new BusinessException("每堂课需要1-3名教师");
        }

        if (!requestDTO.getTeacherIds().contains(requestDTO.getMainTeacherId())) {
            throw new BusinessException("主讲教师必须在教师列表中");
        }

        List<Teacher> teachers = new ArrayList<>();
        for (Long teacherId : requestDTO.getTeacherIds()) {
            Teacher teacher = teacherMapper.selectById(teacherId);
            if (teacher == null) {
                throw new EntityNotFoundException("教师", teacherId);
            }

            if (!teacher.getIsActive()) {
                throw new BusinessException("教师已禁用：" + teacher.getTeacherName());
            }

            // Load course to check age group
            Course course = courseMapper.selectById(schedule.getCourseId());
            if (!teacher.getAgeGroupList().contains(course.getAgeGroup())) {
                throw new BusinessException("教师年龄组不匹配：" + teacher.getTeacherName());
            }

            teachers.add(teacher);
        }

        ConflictCheckResult conflictResult = conflictDetectionService.checkTeacherConflicts(
            requestDTO.getTeacherIds(),
            schedule.getScheduleDate(),
            schedule.getTimeSlotId());

        if (conflictResult.getHasConflicts()) {
            throw new BusinessException("教师时间冲突：" + conflictResult.getConflictDescription());
        }

        assignmentMapper.deleteByCourseScheduleId(requestDTO.getCourseScheduleId());

        List<TeacherAssignment> assignments = new ArrayList<>();
        for (Long teacherId : requestDTO.getTeacherIds()) {
            TeacherAssignment assignment = new TeacherAssignment();
            assignment.setTeacherId(teacherId);
            assignment.setCourseScheduleId(requestDTO.getCourseScheduleId());
            assignment.setIsMainTeacher(teacherId.equals(requestDTO.getMainTeacherId()));
            assignmentMapper.insert(assignment);
            assignments.add(assignment);
        }

        log.info("教师分配成功，课程安排ID：{}，教师数量：{}",
                requestDTO.getCourseScheduleId(), requestDTO.getTeacherIds().size());

        return buildScheduleAssignmentDTO(assignments, schedule);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<CourseScheduleDTO> getScheduleList(LocalDate startDate, LocalDate endDate,
                                                   Integer status, Page page) {
        // 使用 MyBatis Plus 分页查询
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CourseSchedule> wrapper =
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (startDate != null) {
            wrapper.ge(CourseSchedule::getScheduleDate, startDate);
        }

        if (endDate != null) {
            wrapper.le(CourseSchedule::getScheduleDate, endDate);
        }

        if (status != null) {
            wrapper.eq(CourseSchedule::getStatus, status);
        }

        wrapper.orderByAsc(CourseSchedule::getScheduleDate);

        Page<CourseSchedule> schedulePage = scheduleMapper.selectPage(page, wrapper);

        // 转换为DTO
        Page<CourseScheduleDTO> dtoPage = new Page<>(schedulePage.getCurrent(), schedulePage.getSize(), schedulePage.getTotal());
        dtoPage.setRecords(schedulePage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));

        return dtoPage;
    }

   /* @Override
    @Transactional(readOnly = true)
    public Page<CourseScheduleDTO> getScheduleList(LocalDate startDate, LocalDate endDate,
                                                   Integer status, Page Page) {
        Specification<CourseSchedule> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 注意：不要在这里使用 fetch，因为会和分页冲突

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("scheduleDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("scheduleDate"), endDate));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<CourseSchedule> schedulePage = scheduleRepository.findAll(spec, Page);

        // 在转换时单独查询关联对象
        return schedulePage.map(schedule -> {
            CourseScheduleDTO dto = new CourseScheduleDTO();
            dto.setId(schedule.getId());
            dto.setCourseId(schedule.getCourseId());
            dto.setTimeSlotId(schedule.getTimeSlotId());
            dto.setClassroomId(schedule.getClassroomId());
            dto.setScheduleDate(schedule.getScheduleDate());
            dto.setStatus(schedule.getStatus());

            // 手动查询关联对象
            courseRepository.findById(schedule.getCourseId()).ifPresent(course -> {
                dto.setCourseName(course.getCourseName());
                dto.setAgeGroup(course.getAgeGroup());
                dto.setMaxStudents(course.getMaxStudents());
            });

            timeSlotRepository.findById(schedule.getTimeSlotId()).ifPresent(timeSlot -> {
                dto.setTimeSlotName(timeSlot.getSlotName());
                dto.setStartTime(timeSlot.getStartTime());
                dto.setEndTime(timeSlot.getEndTime());
            });

            classroomRepository.findById(schedule.getClassroomId()).ifPresent(classroom -> {
                dto.setClassroomName(classroom.getClassroomName());
            });

            // 获取教师名称列表
            List<TeacherAssignment> assignments = assignmentRepository.findByCourseScheduleId(schedule.getId());
            List<String> teacherNames = assignments.stream()
                    .map(a -> {
                        return teacherRepository.findById(a.getTeacherId())
                                .map(Teacher::getTeacherName)
                                .orElse("未知教师");
                    })
                    .collect(Collectors.toList());
            dto.setTeacherNames(teacherNames);

            // TODO: 获取当前学生数
            dto.setCurrentStudents(0);

            return dto;
        });
    }
*/
    @Override
    @Transactional
    public void cancelSchedule(Long scheduleId, String reason) {
        CourseSchedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new EntityNotFoundException("课程安排", scheduleId);
        }

        schedule.setStatus(Constants.SCHEDULE_STATUS_CANCELLED);
        scheduleMapper.updateById(schedule);

        assignmentMapper.deleteByCourseScheduleId(scheduleId);

        log.info("课程取消成功，课程安排ID：{}，取消原因：{}", scheduleId, reason);
    }

    @Override
    public List<CourseScheduleDTO> getTeacherSchedules(Long teacherId, LocalDate startDate, LocalDate endDate) {
        List<TeacherAssignment> assignments = assignmentMapper.findByTeacherIdAndDateRange(
            teacherId, startDate, endDate);

        List<CourseScheduleDTO> schedules = new ArrayList<>();
        for (TeacherAssignment assignment : assignments) {
            CourseSchedule schedule = scheduleMapper.selectById(assignment.getCourseScheduleId());
            if (schedule != null) {
                schedules.add(convertToDTO(schedule));
            }
        }

        return schedules;
    }
    
    private CourseScheduleDTO convertToDTO(CourseSchedule schedule) {
        CourseScheduleDTO dto = new CourseScheduleDTO();
        dto.setId(schedule.getId());
        dto.setCourseId(schedule.getCourseId());
        dto.setTimeSlotId(schedule.getTimeSlotId());
        dto.setClassroomId(schedule.getClassroomId());
        dto.setScheduleDate(schedule.getScheduleDate());
        dto.setStatus(schedule.getStatus());

        // 手动加载关联对象
        Course course = courseMapper.selectById(schedule.getCourseId());
        if (course != null) {
            dto.setCourseName(course.getCourseName());
            dto.setAgeGroup(course.getAgeGroup());
            dto.setMaxStudents(course.getMaxStudents());
        }

        TimeSlot timeSlot = timeSlotMapper.selectById(schedule.getTimeSlotId());
        if (timeSlot != null) {
            dto.setTimeSlotName(timeSlot.getSlotName());
            dto.setStartTime(timeSlot.getStartTime());
            dto.setEndTime(timeSlot.getEndTime());
        }

        Classroom classroom = classroomMapper.selectById(schedule.getClassroomId());
        if (classroom != null) {
            dto.setClassroomName(classroom.getClassroomName());
        }

        List<TeacherAssignment> assignments = assignmentMapper.findByCourseScheduleId(schedule.getId());
        List<String> teacherNames = assignments.stream()
            .map(a -> {
                Teacher teacher = teacherMapper.selectById(a.getTeacherId());
                return teacher != null ? teacher.getTeacherName() : "未知教师";
            })
            .collect(Collectors.toList());
        dto.setTeacherNames(teacherNames);

        dto.setCurrentStudents(0);

        return dto;
    }
    
    private ScheduleAssignmentDTO buildScheduleAssignmentDTO(List<TeacherAssignment> assignments, CourseSchedule schedule) {
        ScheduleAssignmentDTO dto = new ScheduleAssignmentDTO();
        dto.setCourseScheduleId(schedule.getId());
        dto.setScheduleDate(schedule.getScheduleDate());

        // 手动加载关联对象
        Course course = courseMapper.selectById(schedule.getCourseId());
        if (course != null) {
            dto.setCourseName(course.getCourseName());
        }

        Classroom classroom = classroomMapper.selectById(schedule.getClassroomId());
        if (classroom != null) {
            dto.setClassroomName(classroom.getClassroomName());
        }

        TimeSlot timeSlot = timeSlotMapper.selectById(schedule.getTimeSlotId());
        if (timeSlot != null) {
            dto.setTimeSlotName(timeSlot.getSlotName());
        }

        List<TeacherAssignmentDetailDTO> teacherDetails = assignments.stream()
            .map(assignment -> {
                TeacherAssignmentDetailDTO detail = new TeacherAssignmentDetailDTO();
                detail.setAssignmentId(assignment.getId());
                detail.setTeacherId(assignment.getTeacherId());

                Teacher teacher = teacherMapper.selectById(assignment.getTeacherId());
                if (teacher != null) {
                    detail.setTeacherName(teacher.getTeacherName());
                }
                detail.setIsMainTeacher(assignment.getIsMainTeacher());
                return detail;
            })
            .collect(Collectors.toList());

        dto.setTeacherDetails(teacherDetails);

        return dto;
    }
}

