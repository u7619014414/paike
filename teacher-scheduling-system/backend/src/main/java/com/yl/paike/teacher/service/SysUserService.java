package com.yl.paike.teacher.service;

import com.yl.paike.teacher.entity.SysUser;

import java.util.List;

public interface SysUserService {

    /**
     * 根据用户名查询用户
     */
    SysUser findByUsername(String username);

    /**
     * 验证用户名和密码
     */
    boolean validatePassword(String username, String password);

    /**
     * 更新最后登录时间
     */
    void updateLastLoginTime(Long userId);

    /**
     * 创建用户
     */
    SysUser createUser(SysUser user);

    /**
     * 根据用户类型和关联ID获取显示名称
     */
    String getDisplayName(SysUser user);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 查询所有用户
     */
    List<SysUser> listAll();

    /**
     * 根据ID查询用户
     */
    SysUser findById(Long id);

    /**
     * 更新用户信息
     */
    void updateUser(SysUser user);

    /**
     * 重置密码（管理员专用）
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * 切换用户状态
     */
    void toggleStatus(Long userId);

    /**
     * 更新个人资料
     */
    void updateProfile(Long userId, String name);
}
