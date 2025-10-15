package com.school.course.controller;

import com.school.course.dto.ApiResponse;
import com.school.course.dto.LoginRequest;
import com.school.course.dto.LoginResponse;
import com.school.course.dto.StudentDTO;
import com.school.course.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 学生登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应（包含token和学生信息）
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("收到登录请求: {}", loginRequest.getStudentId());
        LoginResponse response = authService.login(loginRequest);
        return ApiResponse.success(response, "登录成功");
    }

    /**
     * 验证Token
     *
     * @param token JWT Token
     * @return 学生信息
     */
    @GetMapping("/validate")
    public ApiResponse<StudentDTO> validateToken(@RequestHeader("Authorization") String token) {
        // 移除 "Bearer " 前缀
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        StudentDTO student = authService.validateToken(token);
        return ApiResponse.success(student, "Token验证成功");
    }

    /**
     * 退出登录
     * 注意：JWT是无状态的，退出登录主要在前端清除token
     * 这里提供一个接口用于日志记录或其他操作
     *
     * @return 响应
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String actualToken = token.substring(7);
            // 这里可以记录退出日志或添加token黑名单逻辑
            log.info("用户退出登录");
        }
        return ApiResponse.success(null, "退出登录成功");
    }
}
