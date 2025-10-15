package com.yl.paike.teacher.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yl.paike.teacher.dto.CourseCreateDTO;
import com.yl.paike.teacher.dto.CourseDTO;

import java.util.List;

public interface CourseService {

    CourseDTO createCourse(CourseCreateDTO createDTO);

    CourseDTO getCourseById(Long id);

    Page<CourseDTO> getCourseList(Integer ageGroup, String keyword, int pageNum, int pageSize);
    
    CourseDTO updateCourse(Long id, CourseCreateDTO updateDTO);
    
    void deleteCourse(Long id);
    
    List<CourseDTO> getAllActiveCourses();
}
