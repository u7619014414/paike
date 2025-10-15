package com.yl.paike.teacher.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yl.paike.teacher.dto.TeacherCreateDTO;
import com.yl.paike.teacher.dto.TeacherDTO;
import com.yl.paike.teacher.dto.TeacherUpdateDTO;

import java.time.LocalDate;
import java.util.List;

public interface TeacherService {

    TeacherDTO createTeacher(TeacherCreateDTO createDTO);

    TeacherDTO getTeacherById(Long id);

    Page<TeacherDTO> getTeacherList(String keyword, List<Integer> ageGroups, Boolean isActive, int pageNum, int pageSize, String sortBy, String sortDir);
    
    TeacherDTO updateTeacher(Long id, TeacherUpdateDTO updateDTO);
    
    void deleteTeacher(Long id);
    
    List<TeacherDTO> getAvailableTeachers(Integer ageGroup, LocalDate date, Long timeSlotId);
}
