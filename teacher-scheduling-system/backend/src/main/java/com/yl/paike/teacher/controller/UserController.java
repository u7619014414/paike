package com.yl.paike.teacher.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yl.paike.teacher.dto.Result;
import com.yl.paike.teacher.entity.SysUser;
import com.yl.paike.teacher.service.SysUserService;
import com.yl.paike.teacher.util.PasswordUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final SysUserService sysUserService;
    private final PasswordUtil passwordUtil;

    /**
     * 获取所有用户列表
     */
    @GetMapping
    public Result<List<SysUser>> getAllUsers() {
        try {
            List<SysUser> users = sysUserService.listAll();
            // 设置显示名称
            users.forEach(user -> user.setDisplayName(sysUserService.getDisplayName(user)));
            log.info("查询所有用户,共{}个", users.size());
            return Result.success(users);
        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            return Result.error(500, "查询用户列表失败");
        }
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public Result<SysUser> getUserById(@PathVariable Long id) {
        try {
            SysUser user = sysUserService.findById(id);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }
            user.setDisplayName(sysUserService.getDisplayName(user));
            return Result.success(user);
        } catch (Exception e) {
            log.error("查询用户失败", e);
            return Result.error(500, "查询用户失败");
        }
    }

    /**
     * 创建用户
     */
    @PostMapping
    public Result<SysUser> createUser(@Valid @RequestBody Map<String, Object> requestData) {
        try {
            SysUser user = new SysUser();
            user.setUsername((String) requestData.get("username"));
            user.setPassword((String) requestData.get("password"));
            user.setUserType((Integer) requestData.get("userType"));

            Object relatedIdObj = requestData.get("relatedId");
            if (relatedIdObj != null) {
                user.setRelatedId(Long.valueOf(relatedIdObj.toString()));
            }

            user.setIsActive((Boolean) requestData.getOrDefault("isActive", true));

            SysUser created = sysUserService.createUser(user);
            log.info("创建用户成功: {}", created.getUsername());
            return Result.success(created, "用户创建成功");
        } catch (Exception e) {
            log.error("创建用户失败", e);
            return Result.error(500, e.getMessage());
        }
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> requestData) {
        try {
            SysUser user = sysUserService.findById(id);
            if (user == null) {
                return Result.error(404, "用户不存在");
            }

            if (requestData.containsKey("displayName")) {
                // 这里暂时不处理displayName，因为它是通过relatedId关联获取的
            }
            if (requestData.containsKey("userType")) {
                user.setUserType((Integer) requestData.get("userType"));
            }
            if (requestData.containsKey("relatedId")) {
                Object relatedIdObj = requestData.get("relatedId");
                user.setRelatedId(relatedIdObj != null ? Long.valueOf(relatedIdObj.toString()) : null);
            }
            if (requestData.containsKey("isActive")) {
                user.setIsActive((Boolean) requestData.get("isActive"));
            }

            sysUserService.updateUser(user);
            log.info("更新用户成功: {}", user.getUsername());
            return Result.success(null, "用户更新成功");
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return Result.error(500, "更新用户失败");
        }
    }

    /**
     * 重置用户密码（管理员专用）
     */
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> requestData) {
        try {
            String newPassword = requestData.get("newPassword");
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return Result.error(400, "新密码不能为空");
            }

            sysUserService.resetPassword(id, newPassword);
            log.info("重置用户{}密码成功", id);
            return Result.success(null, "密码重置成功");
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return Result.error(500, e.getMessage());
        }
    }

    /**
     * 切换用户状态
     */
    @PutMapping("/{id}/toggle-status")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        try {
            sysUserService.toggleStatus(id);
            log.info("切换用户{}状态成功", id);
            return Result.success(null, "状态切换成功");
        } catch (Exception e) {
            log.error("切换状态失败", e);
            return Result.error(500, e.getMessage());
        }
    }
}
