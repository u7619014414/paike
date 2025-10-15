package com.yl.paike.teacher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yl.paike.teacher.entity.TimeSlot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TimeSlotMapper extends BaseMapper<TimeSlot> {

    /**
     * 查询所有激活的时间段，按星期和开始时间排序
     * 使用 QueryWrapper 在 Service 层实现
     */
    @Select("SELECT * FROM T_TIME_SLOTS WHERE is_active = 1 ORDER BY day_of_week ASC, start_time ASC")
    List<TimeSlot> findByIsActiveTrueOrderByDayOfWeekAscStartTimeAsc();

    /**
     * 根据星期查询激活的时间段
     * 使用 QueryWrapper 在 Service 层实现
     */
    @Select("SELECT * FROM T_TIME_SLOTS WHERE day_of_week = #{dayOfWeek} AND is_active = 1")
    List<TimeSlot> findByDayOfWeekAndIsActiveTrue(@Param("dayOfWeek") Integer dayOfWeek);

    /**
     * 查询所有激活的时间段
     * 使用 QueryWrapper 在 Service 层实现: lambdaQuery().eq(TimeSlot::getIsActive, true).list()
     */
    @Select("SELECT * FROM T_TIME_SLOTS WHERE is_active = 1")
    List<TimeSlot> findByIsActiveTrue();
}
