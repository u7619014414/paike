package com.school.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.course.entity.Student;
import com.school.course.dto.StudentDTO;
import com.school.course.dto.StudentRegistrationDTO;
import com.school.course.mapper.StudentMapper;
import com.school.course.exception.BusinessException;
import com.school.course.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentMapper studentMapper;
    
    /**
     * 学生注册
     */
    public StudentDTO registerStudent(StudentRegistrationDTO registerDTO) {
        // 检查手机号是否已注册
        Student existingStudent = studentMapper.findByParentPhone(registerDTO.getParentPhone());
        if (existingStudent != null) {
            throw new BusinessException("该手机号已注册过学生");
        }

        Student student = new Student();
        BeanUtils.copyProperties(registerDTO, student);
        student.setRegistrationStatus(2); // 直接通过审核
        student.generateStudentCode(); // 生成学生编号

        studentMapper.insert(student);
        log.info("学生注册成功，学生编号：{}", student.getStudentCode());

        return convertToDTO(student);
    }
    
    /**
     * 根据ID查询学生
     */
    public StudentDTO getStudentById(Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new EntityNotFoundException("学生不存在");
        }
        return convertToDTO(student);
    }

    /**
     * 获取所有学生
     */
    public List<StudentDTO> getAllStudents() {
        List<Student> students = studentMapper.findActiveStudents();
        return students.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * 根据年龄组查询学生
     */
    public List<StudentDTO> getStudentsByAgeGroup(Integer ageGroup) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getAgeGroup, ageGroup);
        List<Student> students = studentMapper.selectList(wrapper);
        return students.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * 更新学生信息
     */
    public StudentDTO updateStudent(Long studentId, StudentDTO updateDTO) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new EntityNotFoundException("学生不存在");
        }

        if (updateDTO.getStudentName() != null) {
            student.setStudentName(updateDTO.getStudentName());
        }
        if (updateDTO.getAge() != null) {
            student.setAge(updateDTO.getAge());
            // 重新计算年龄组
            updateAgeGroup(student);
        }
        if (updateDTO.getParentName() != null) {
            student.setParentName(updateDTO.getParentName());
        }
        if (updateDTO.getParentPhone() != null) {
            student.setParentPhone(updateDTO.getParentPhone());
        }
        if (updateDTO.getParentEmail() != null) {
            student.setParentEmail(updateDTO.getParentEmail());
        }

        studentMapper.updateById(student);
        return convertToDTO(student);
    }
    
    private void updateAgeGroup(Student student) {
        Integer age = student.getAge();
        if (age >= 1 && age <= 3) {
            student.setAgeGroup(1);
        } else if (age > 3 && age <= 6) {
            student.setAgeGroup(2);
        } else if (age > 6 && age <= 9) {
            student.setAgeGroup(3);
        } else if (age > 9 && age <= 12) {
            student.setAgeGroup(4);
        }
    }
    
    /**
     * 实体转DTO
     */
    private StudentDTO convertToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        BeanUtils.copyProperties(student, dto);
        return dto;
    }
}
