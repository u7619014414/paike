package com.yl.paike.teacher.controller;

import com.yl.paike.teacher.dto.ChangePasswordRequest;
import com.yl.paike.teacher.dto.LoginRequest;
import com.yl.paike.teacher.dto.LoginResponse;
import com.yl.paike.teacher.dto.Result;
import com.yl.paike.teacher.dto.UserInfo;
import com.yl.paike.teacher.entity.SysUser;
import com.yl.paike.teacher.service.SysUserService;
import com.yl.paike.teacher.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtUtil jwtUtil;
    private final SysUserService sysUserService;

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录: {}", request.getUsername());

        // 从数据库验证用户名和密码
        if (!sysUserService.validatePassword(request.getUsername(), request.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }

        // 获取用户信息
        SysUser user = sysUserService.findByUsername(request.getUsername());
        if (user == null || !user.getIsActive()) {
            return Result.error(401, "用户不存在或已被禁用");
        }

        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getUsername(), user.getId());

        // 更新最后登录时间
        sysUserService.updateLastLoginTime(user.getId());

        // 构造用户信息
        UserInfo userInfo = UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getDisplayName())
                .role(getUserRole(user.getUserType()))
                .build();

        // 构造响应
        LoginResponse response = LoginResponse.builder()
                .token(token)
                .userInfo(userInfo)
                .build();

        log.info("用户 {} 登录成功", request.getUsername());
        return Result.success(response, "登录成功");
    }

    /**
     * 登出接口
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        log.info("用户登出");
        // JWT是无状态的，登出主要在前端清除token
        // 如果需要实现token黑名单，可以在这里添加逻辑
        return Result.success(null, "登出成功");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current-user")
    public Result<UserInfo> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String username = jwtUtil.getUsernameFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);

            SysUser user = sysUserService.findByUsername(username);
            if (user == null) {
                return Result.error(401, "用户不存在");
            }

            UserInfo userInfo = UserInfo.builder()
                    .id(userId)
                    .username(username)
                    .name(user.getDisplayName())
                    .role(getUserRole(user.getUserType()))
                    .build();

            return Result.success(userInfo);
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            return Result.error(401, "未授权");
        }
    }

    /**
     * 更新个人资料
     */
    @PostMapping("/update-profile")
    public Result<Void> updateProfile(@RequestBody Map<String, String> requestData,
                                      @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            String name = requestData.get("name");

            if (name == null || name.trim().isEmpty()) {
                return Result.error(400, "显示名称不能为空");
            }

            sysUserService.updateProfile(userId, name);
            log.info("用户ID {} 更新个人资料成功", userId);
            return Result.success(null, "个人资料更新成功");
        } catch (Exception e) {
            log.error("更新个人资料失败", e);
            return Result.error(500, "更新个人资料失败");
        }
    }

    /**
     * 修改密码接口
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                       @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);

            sysUserService.changePassword(userId, request.getOldPassword(), request.getNewPassword());

            log.info("用户ID {} 修改密码成功", userId);
            return Result.success(null, "密码修改成功");
        } catch (RuntimeException e) {
            log.error("修改密码失败", e);
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error(500, "修改密码失败");
        }
    }

    /**
     * 根据用户类型获取角色
     */
    private String getUserRole(Integer userType) {
        return switch (userType) {
            case 1 -> "admin";
            case 2 -> "teacher";
            case 3 -> "student";
            default -> "user";
        };
    }
}
