package com.yl.paike.teacher.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yl.paike.teacher.entity.SysUser;
import com.yl.paike.teacher.entity.Teacher;
import com.yl.paike.teacher.mapper.SysUserMapper;
import com.yl.paike.teacher.mapper.TeacherMapper;
import com.yl.paike.teacher.service.SysUserService;
import com.yl.paike.teacher.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final TeacherMapper teacherMapper;
    private final PasswordUtil passwordUtil;

    @Override
    public SysUser findByUsername(String username) {
        SysUser user = sysUserMapper.findByUsername(username);
        if (user != null) {
            user.setDisplayName(getDisplayName(user));
        }
        return user;
    }

    @Override
    public boolean validatePassword(String username, String password) {
        SysUser user = findByUsername(username);
        if (user == null || !user.getIsActive()) {
            return false;
        }
        return passwordUtil.matches(password, user.getPassword());
    }

    @Override
    @Transactional
    public void updateLastLoginTime(Long userId) {
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, userId)
                .set(SysUser::getLastLoginTime, LocalDateTime.now());
        sysUserMapper.update(null, updateWrapper);
    }

    @Override
    @Transactional
    public SysUser createUser(SysUser user) {
        // 加密密码
        if (user.getPassword() != null) {
            user.setPassword(passwordUtil.encryptPassword(user.getPassword()));
        }

        // 设置默认值
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }

        sysUserMapper.insert(user);
        return user;
    }

    @Override
    public String getDisplayName(SysUser user) {
        if (user == null) {
            return "未知用户";
        }

        // 根据用户类型获取显示名称
        switch (user.getUserType()) {
            case 1: // 管理员
                return "系统管理员";
            case 2: // 教师
                if (user.getRelatedId() != null) {
                    Teacher teacher = teacherMapper.selectById(user.getRelatedId());
                    if (teacher != null) {
                        return teacher.getTeacherName();
                    }
                }
                return "教师";
            case 3: // 学生
                // 如果有学生表，可以类似地查询学生姓名
                return "学生";
            default:
                return user.getUsername();
        }
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        if (!passwordUtil.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 加密新密码
        String encryptedPassword = passwordUtil.encryptPassword(newPassword);

        // 更新密码
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, userId)
                .set(SysUser::getPassword, encryptedPassword)
                .set(SysUser::getUpdatedAt, LocalDateTime.now());
        sysUserMapper.update(null, updateWrapper);

        log.info("用户 {} 修改密码成功", user.getUsername());
    }

    @Override
    public List<SysUser> listAll() {
        return sysUserMapper.selectList(null);
    }

    @Override
    public SysUser findById(Long id) {
        return sysUserMapper.selectById(id);
    }

    @Override
    @Transactional
    public void updateUser(SysUser user) {
        sysUserMapper.updateById(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 加密新密码
        String encryptedPassword = passwordUtil.encryptPassword(newPassword);

        // 更新密码
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, userId)
                .set(SysUser::getPassword, encryptedPassword)
                .set(SysUser::getUpdatedAt, LocalDateTime.now());
        sysUserMapper.update(null, updateWrapper);

        log.info("管理员重置用户 {} 密码成功", user.getUsername());
    }

    @Override
    @Transactional
    public void toggleStatus(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 切换状态
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, userId)
                .set(SysUser::getIsActive, !user.getIsActive())
                .set(SysUser::getUpdatedAt, LocalDateTime.now());
        sysUserMapper.update(null, updateWrapper);

        log.info("切换用户 {} 状态: {} -> {}", user.getUsername(), user.getIsActive(), !user.getIsActive());
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, String name) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 注意：displayName是通过关联表获取的，这里暂时不支持直接修改
        // 如果需要修改，需要根据userType去更新对应的教师表或学生表
        log.warn("updateProfile方法暂未实现，用户 {} 尝试更新个人资料", user.getUsername());
    }
}
