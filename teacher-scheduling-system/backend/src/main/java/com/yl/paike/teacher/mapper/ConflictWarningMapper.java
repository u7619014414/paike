package com.yl.paike.teacher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yl.paike.teacher.entity.ConflictWarning;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ConflictWarningMapper extends BaseMapper<ConflictWarning> {

    /**
     * 根据冲突类型查询
     * 使用 QueryWrapper 在 Service 层实现
     */
    @Select("SELECT * FROM SYS_CONFLICT_WARNINGS WHERE conflict_type = #{conflictType}")
    List<ConflictWarning> findByConflictType(@Param("conflictType") Integer conflictType);

    /**
     * 根据状态查询
     * 使用 QueryWrapper 在 Service 层实现
     */
    @Select("SELECT * FROM SYS_CONFLICT_WARNINGS WHERE status = #{status}")
    List<ConflictWarning> findByStatus(@Param("status") Integer status);

    /**
     * 根据日期范围查询冲突警告
     * 使用 QueryWrapper 在 Service 层实现
     */
    @Select("SELECT * FROM SYS_CONFLICT_WARNINGS WHERE conflict_date BETWEEN #{startDate} AND #{endDate}")
    List<ConflictWarning> findByConflictDateBetween(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * 查询指定日期范围内未解决的冲突
     */
    @Select("SELECT * FROM SYS_CONFLICT_WARNINGS " +
            "WHERE conflict_date BETWEEN #{startDate} AND #{endDate} " +
            "AND status = 1 " +
            "ORDER BY created_at DESC")
    List<ConflictWarning> findUnresolvedConflicts(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    /**
     * 删除指定状态且创建时间早于指定时间的冲突警告
     */
    @Delete("DELETE FROM SYS_CONFLICT_WARNINGS WHERE status = #{status} AND created_at < #{beforeDate}")
    void deleteByStatusAndCreatedAtBefore(@Param("status") Integer status,
                                          @Param("beforeDate") LocalDateTime beforeDate);
}
