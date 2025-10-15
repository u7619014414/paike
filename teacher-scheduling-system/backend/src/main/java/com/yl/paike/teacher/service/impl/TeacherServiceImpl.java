package com.yl.paike.teacher.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yl.paike.teacher.dto.*;
import com.yl.paike.teacher.entity.Teacher;
import com.yl.paike.teacher.exception.BusinessException;
import com.yl.paike.teacher.exception.EntityNotFoundException;
import com.yl.paike.teacher.mapper.TeacherAssignmentMapper;
import com.yl.paike.teacher.mapper.TeacherMapper;
import com.yl.paike.teacher.service.TeacherService;
import com.yl.paike.teacher.util.BeanConverter;
import com.yl.paike.teacher.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final TeacherMapper teacherMapper;
    private final TeacherAssignmentMapper assignmentMapper;

    @Override
    @Transactional
    public TeacherDTO createTeacher(TeacherCreateDTO createDTO) {
        // 检查手机号是否已存在
        Teacher existingTeacher = teacherMapper.findByPhone(createDTO.getPhone());
        if (existingTeacher != null) {
            throw new BusinessException("该手机号已被注册");
        }

        Teacher teacher = new Teacher();
        teacher.setTeacherName(createDTO.getTeacherName());
        teacher.setPhone(createDTO.getPhone());
        teacher.setEmail(createDTO.getEmail());
        teacher.setSpecialties(createDTO.getSpecialties());
        teacher.setAgeGroupList(createDTO.getAgeGroups());
        teacher.setIsActive(true);
        teacher.generateTeacherCode(); // 生成教师编号

        teacherMapper.insert(teacher);
        log.info("教师创建成功，教师编号：{}", teacher.getTeacherCode());

        return convertToDTO(teacher);
    }

    @Override
    @Cacheable(value = "teachers", key = "#id")
    public TeacherDTO getTeacherById(Long id) {
        Teacher teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw new EntityNotFoundException("教师", id);
        }
        return convertToDTO(teacher);
    }

    @Override
    public Page<TeacherDTO> getTeacherList(String keyword, List<Integer> ageGroups,
                                           Boolean isActive, int pageNum, int pageSize,
                                           String sortBy, String sortDir) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Teacher::getTeacherName, keyword)
                             .or()
                             .like(Teacher::getPhone, keyword));
        }

        if (ageGroups != null && !ageGroups.isEmpty()) {
            wrapper.and(w -> {
                for (int i = 0; i < ageGroups.size(); i++) {
                    if (i > 0) {
                        w.or();
                    }
                    w.like(Teacher::getAgeGroups, String.valueOf(ageGroups.get(i)));
                }
            });
        }

        if (isActive != null) {
            wrapper.eq(Teacher::getIsActive, isActive);
        }

        // Add sorting
        if ("desc".equalsIgnoreCase(sortDir)) {
            wrapper.orderByDesc(Teacher::getCreatedAt);
        } else {
            wrapper.orderByAsc(Teacher::getCreatedAt);
        }

        Page<Teacher> page = new Page<>(pageNum + 1, pageSize); // MyBatis Plus页码从1开始
        Page<Teacher> teacherPage = teacherMapper.selectPage(page, wrapper);

        Page<TeacherDTO> dtoPage = new Page<>(teacherPage.getCurrent(), teacherPage.getSize(), teacherPage.getTotal());
        dtoPage.setRecords(teacherPage.getRecords().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));

        return dtoPage;
    }

    @Override
    @Transactional
    @CacheEvict(value = "teachers", key = "#id")
    public TeacherDTO updateTeacher(Long id, TeacherUpdateDTO updateDTO) {
        Teacher teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw new EntityNotFoundException("教师", id);
        }

        if (updateDTO.getTeacherName() != null) {
            teacher.setTeacherName(updateDTO.getTeacherName());
        }

        if (updateDTO.getPhone() != null) {
            // 检查新手机号是否被其他教师使用
            Teacher existingTeacher = teacherMapper.findByPhone(updateDTO.getPhone());
            if (existingTeacher != null && !existingTeacher.getId().equals(id)) {
                throw new BusinessException("该手机号已被其他教师使用");
            }
            teacher.setPhone(updateDTO.getPhone());
        }

        if (updateDTO.getEmail() != null) {
            teacher.setEmail(updateDTO.getEmail());
        }

        if (updateDTO.getSpecialties() != null) {
            teacher.setSpecialties(updateDTO.getSpecialties());
        }

        if (updateDTO.getAgeGroups() != null) {
            teacher.setAgeGroupList(updateDTO.getAgeGroups());
        }

        teacherMapper.updateById(teacher);
        log.info("教师信息更新成功，教师ID：{}", id);

        return convertToDTO(teacher);
    }

    @Override
    @Transactional
    @CacheEvict(value = "teachers", key = "#id")
    public void deleteTeacher(Long id) {
        Teacher teacher = teacherMapper.selectById(id);
        if (teacher == null) {
            throw new EntityNotFoundException("教师", id);
        }

        // 检查教师是否有未完成的课程安排
        LocalDate today = LocalDate.now();
        List<com.yl.paike.teacher.entity.TeacherAssignment> futureAssignments =
                assignmentMapper.findByTeacherIdAndDateRange(id, today, today.plusMonths(3));

        if (!futureAssignments.isEmpty()) {
            throw new BusinessException("教师有未完成的课程安排，无法删除");
        }

        teacher.setIsActive(false);
        teacherMapper.updateById(teacher);

        log.info("教师删除成功，教师ID：{}", id);
    }

    @Override
    public List<TeacherDTO> getAvailableTeachers(Integer ageGroup, LocalDate date, Long timeSlotId) {
        // 获取符合年龄组要求的活跃教师
        List<Teacher> eligibleTeachers = teacherMapper.findByAgeGroupAndActive(String.valueOf(ageGroup));

        List<TeacherDTO> availableTeachers = new ArrayList<>();

        for (Teacher teacher : eligibleTeachers) {
            // 检查教师在指定时间是否有冲突
            List<com.yl.paike.teacher.entity.TeacherAssignment> conflicts =
                    assignmentMapper.findConflictingAssignments(teacher.getId(), date, timeSlotId);

            if (conflicts.isEmpty()) {
                availableTeachers.add(convertToDTO(teacher));
            }
        }

        return availableTeachers;
    }

    /**
     * 实体转DTO
     */
    private TeacherDTO convertToDTO(Teacher teacher) {
        TeacherDTO dto = BeanConverter.convert(teacher, TeacherDTO.class);
        dto.setAgeGroups(teacher.getAgeGroupList());
        return dto;
    }
}