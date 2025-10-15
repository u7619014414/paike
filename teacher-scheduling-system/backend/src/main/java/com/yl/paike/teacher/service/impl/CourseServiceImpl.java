package com.yl.paike.teacher.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yl.paike.teacher.dto.CourseCreateDTO;
import com.yl.paike.teacher.dto.CourseDTO;
import com.yl.paike.teacher.entity.Course;
import com.yl.paike.teacher.exception.BusinessException;
import com.yl.paike.teacher.exception.EntityNotFoundException;
import com.yl.paike.teacher.mapper.CourseMapper;
import com.yl.paike.teacher.mapper.CourseScheduleMapper;
import com.yl.paike.teacher.service.CourseService;
import com.yl.paike.teacher.util.BeanConverter;
import com.yl.paike.teacher.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final CourseScheduleMapper scheduleMapper;

    @Override
    @Transactional
    public CourseDTO createCourse(CourseCreateDTO createDTO) {
        Course course = new Course();
        course.setCourseName(createDTO.getCourseName());
        course.setAgeGroup(createDTO.getAgeGroup());
        course.setMaxStudents(createDTO.getMaxStudents());
        course.setDescription(createDTO.getDescription());
        course.setIsActive(true);
        course.generateCourseCode(); // 生成课程编号

        courseMapper.insert(course);
        log.info("课程创建成功，课程编号：{}", course.getCourseCode());

        return convertToDTO(course);
    }

    @Override
    public CourseDTO getCourseById(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new EntityNotFoundException("课程", id);
        }
        return convertToDTO(course);
    }

    @Override
    public Page<CourseDTO> getCourseList(Integer ageGroup, String keyword, int pageNum, int pageSize) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();

        if (ageGroup != null) {
            wrapper.eq(Course::getAgeGroup, ageGroup);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(Course::getCourseName, keyword);
        }

        Page<Course> page = new Page<>(pageNum + 1, pageSize); // MyBatis Plus页码从1开始
        Page<Course> coursePage = courseMapper.selectPage(page, wrapper);

        Page<CourseDTO> dtoPage = new Page<>(coursePage.getCurrent(), coursePage.getSize(), coursePage.getTotal());
        dtoPage.setRecords(coursePage.getRecords().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));

        return dtoPage;
    }

    @Override
    @Transactional
    public CourseDTO updateCourse(Long id, CourseCreateDTO updateDTO) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new EntityNotFoundException("课程", id);
        }

        course.setCourseName(updateDTO.getCourseName());
        course.setAgeGroup(updateDTO.getAgeGroup());
        course.setMaxStudents(updateDTO.getMaxStudents());
        course.setDescription(updateDTO.getDescription());

        courseMapper.updateById(course);
        log.info("课程更新成功，课程ID：{}", id);

        return convertToDTO(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new EntityNotFoundException("课程", id);
        }

        long scheduleCount = scheduleMapper.findByCourseIdAndStatus(id, 1).size();
        if (scheduleCount > 0) {
            throw new BusinessException("课程有未完成的安排，无法删除");
        }

        course.setIsActive(false);
        courseMapper.updateById(course);

        log.info("课程删除成功，课程ID：{}", id);
    }

    @Override
    public List<CourseDTO> getAllActiveCourses() {
        List<Course> courses = courseMapper.findByIsActiveTrue();
        return courses.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    private CourseDTO convertToDTO(Course course) {
        CourseDTO dto = BeanConverter.convert(course, CourseDTO.class);
        dto.setAgeGroupName(Constants.AGE_GROUP_NAMES[course.getAgeGroup()]);
        return dto;
    }
}
