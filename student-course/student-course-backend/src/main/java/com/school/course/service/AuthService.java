package com.school.course.service;

import com.school.course.dto.LoginRequest;
import com.school.course.dto.LoginResponse;
import com.school.course.dto.StudentDTO;
import com.school.course.entity.Student;
import com.school.course.exception.BusinessException;
import com.school.course.mapper.StudentMapper;
import com.school.course.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final StudentMapper studentMapper;
    private final JwtUtil jwtUtil;

    /**
     * 学生登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应（包含token和学生信息）
     */
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("学生登录请求: {}", loginRequest.getStudentId());

        // 根据学号查询学生
        Student student = studentMapper.findByStudentCode(loginRequest.getStudentId());

        if (student == null) {
            log.warn("学生不存在: {}", loginRequest.getStudentId());
            throw new BusinessException("学号或密码错误");
        }

        // 验证密码（简单比对，实际应使用BCrypt）
        if (!loginRequest.getPassword().equals(student.getPassword())) {
            log.warn("密码错误: {}", loginRequest.getStudentId());
            throw new BusinessException("学号或密码错误");
        }

        // 检查学生状态
        if (!student.getIsActive()) {
            log.warn("学生账号已被禁用: {}", loginRequest.getStudentId());
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 检查注册状态
        if (student.getRegistrationStatus() != 2) {
            log.warn("学生注册状态异常: {}, status: {}", loginRequest.getStudentId(), student.getRegistrationStatus());
            String message = switch (student.getRegistrationStatus()) {
                case 1 -> "账号审核中，请等待管理员审核";
                case 3 -> "账号审核未通过，请联系管理员";
                default -> "账号状态异常，请联系管理员";
            };
            throw new BusinessException(message);
        }

        // 生成JWT Token
        String token = jwtUtil.generateToken(student.getStudentCode(), student.getStudentName());

        // 构建学生DTO（不返回密码）
        StudentDTO studentDTO = convertToDTO(student);

        log.info("学生登录成功: {}", loginRequest.getStudentId());

        return new LoginResponse(token, studentDTO);
    }

    /**
     * 验证Token
     *
     * @param token JWT Token
     * @return 学生信息
     */
    public StudentDTO validateToken(String token) {
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new BusinessException("Token无效或已过期");
        }

        String studentCode = jwtUtil.getStudentIdFromToken(token);
        Student student = studentMapper.findByStudentCode(studentCode);

        if (student == null || !student.getIsActive()) {
            throw new BusinessException("学生不存在或账号已被禁用");
        }

        return convertToDTO(student);
    }

    /**
     * 将Student实体转换为StudentDTO
     *
     * @param student 学生实体
     * @return 学生DTO
     */
    private StudentDTO convertToDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setStudentCode(student.getStudentCode());
        dto.setStudentName(student.getStudentName());
        dto.setAge(student.getAge());
        dto.setAgeGroup(student.getAgeGroup());
        dto.setParentName(student.getParentName());
        dto.setParentPhone(student.getParentPhone());
        dto.setParentEmail(student.getParentEmail());
        dto.setRegistrationStatus(student.getRegistrationStatus());
        dto.setIsActive(student.getIsActive());
        dto.setCreatedAt(student.getCreatedAt());
        // 注意：不设置密码字段
        return dto;
    }
}
