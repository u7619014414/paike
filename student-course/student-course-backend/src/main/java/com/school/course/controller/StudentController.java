// StudentController.java
package com.school.course.controller;

import com.school.course.dto.ApiResponse;
import com.school.course.dto.StudentDTO;
import com.school.course.dto.StudentRegistrationDTO;
import com.school.course.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentService studentService;

    /**
     * 学生注册
     */
    @PostMapping("/register")
    public ApiResponse<StudentDTO> registerStudent(@Valid @RequestBody StudentRegistrationDTO registerDTO) {
        StudentDTO student = studentService.registerStudent(registerDTO);
        return ApiResponse.success(student, "注册成功");
    }

    /**
     * 查询学生详情
     */
    @GetMapping("/{id}")
    public ApiResponse<StudentDTO> getStudent(@PathVariable Long id) {
        StudentDTO student = studentService.getStudentById(id);
        return ApiResponse.success(student);
    }

    /**
     * 获取所有学生
     */
    @GetMapping
    public ApiResponse<List<StudentDTO>> getAllStudents() {
        List<StudentDTO> students = studentService.getAllStudents();
        return ApiResponse.success(students);
    }

    /**
     * 根据年龄组查询学生
     */
    @GetMapping("/age-group/{ageGroup}")
    public ApiResponse<List<StudentDTO>> getStudentsByAgeGroup(@PathVariable Integer ageGroup) {
        List<StudentDTO> students = studentService.getStudentsByAgeGroup(ageGroup);
        return ApiResponse.success(students);
    }

    /**
     * 更新学生信息
     */
    @PutMapping("/{id}")
    public ApiResponse<StudentDTO> updateStudent(@PathVariable Long id,
                                                 @RequestBody StudentDTO updateDTO) {
        StudentDTO student = studentService.updateStudent(id, updateDTO);
        return ApiResponse.success(student, "更新成功");
    }
}
