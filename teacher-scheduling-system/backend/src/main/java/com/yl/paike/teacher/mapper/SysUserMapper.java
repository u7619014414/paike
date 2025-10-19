package com.yl.paike.teacher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yl.paike.teacher.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM SYS_USERS WHERE USERNAME = #{username}")
    SysUser findByUsername(@Param("username") String username);

    /**
     * 根据用户名和类型查询用户
     */
    @Select("SELECT * FROM SYS_USERS WHERE USERNAME = #{username} AND USER_TYPE = #{userType}")
    SysUser findByUsernameAndType(@Param("username") String username, @Param("userType") Integer userType);
}
