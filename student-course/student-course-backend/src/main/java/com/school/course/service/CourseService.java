package com.school.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.school.course.entity.*;
import com.school.course.dto.*;
import com.school.course.mapper.*;
import com.school.course.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseScheduleMapper scheduleMapper;
    private final TimeSlotMapper timeSlotMapper;
    private final StudentEnrollmentMapper enrollmentMapper;
    
    private static final String[] DAY_NAMES = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    private final CourseMapper courseMapper;
    private final ClassroomMapper classroomMapper;

    /**
     * 获取课程表视图（按周显示）
     */
    public List<CourseScheduleViewDTO> getWeekScheduleView(Integer studentAgeGroup, LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(6);
        List<CourseSchedule> schedules = scheduleMapper.findWeekSchedule(startDate, endDate);

        Map<Integer, CourseScheduleViewDTO> dayMap = new LinkedHashMap<>();

        // 初始化一周的数据结构
        for (int i = 1; i <= 7; i++) {
            CourseScheduleViewDTO dayView = new CourseScheduleViewDTO();
            dayView.setDayOfWeek(i);
            dayView.setDayName(DAY_NAMES[i]);
            dayView.setTimeSlots(new ArrayList<>());
            dayMap.put(i, dayView);
        }

        // 获取所有时间段
        LambdaQueryWrapper<TimeSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TimeSlot::getIsActive, true)
               .orderByAsc(TimeSlot::getDayOfWeek, TimeSlot::getStartTime);
        List<TimeSlot> timeSlots = timeSlotMapper.selectList(wrapper);

        // 为每个时间段创建视图
        Map<String, TimeSlotViewDTO> timeSlotMap = new HashMap<>();
        for (TimeSlot timeSlot : timeSlots) {
            String key = timeSlot.getDayOfWeek() + "_" + timeSlot.getId();

            TimeSlotViewDTO timeSlotView = new TimeSlotViewDTO();
            timeSlotView.setTimeSlotId(timeSlot.getId());
            timeSlotView.setSlotName(timeSlot.getSlotName());
            timeSlotView.setStartTime(timeSlot.getStartTime());
            timeSlotView.setEndTime(timeSlot.getEndTime());
            timeSlotView.setCourses(new ArrayList<>());

            timeSlotMap.put(key, timeSlotView);
            dayMap.get(timeSlot.getDayOfWeek()).getTimeSlots().add(timeSlotView);
        }
        List<Course> courses = courseMapper.selectList(new LambdaQueryWrapper<>());
        List<Classroom> classrooms=classroomMapper.selectList(new LambdaQueryWrapper<>());
        // 填充课程信息
        for (CourseSchedule schedule : schedules) {

            TimeSlot timeSlot = Optional.ofNullable(schedule.getTimeSlot()).orElseGet(() -> getScheduleTimeSlot(schedule,timeSlots));

            // 使用课程安排的实际日期（SCHEDULE_DATE）的星期几，而不是 TimeSlot 的 DAY_OF_WEEK
            int actualDayOfWeek = schedule.getScheduleDate().getDayOfWeek().getValue();
            String key = actualDayOfWeek + "_" + schedule.getTimeSlotId();
            TimeSlotViewDTO timeSlotView = timeSlotMap.get(key);

            if (timeSlotView != null) {
                Course course = Optional.ofNullable(schedule.getCourse()).orElseGet(() -> getScheduleCourse(schedule,courses));
                Classroom cr= Optional.ofNullable(schedule.getClassroom()).orElseGet(() -> getScheduleClassroom(schedule,classrooms));
                CourseInfoDTO courseInfo = buildCourseInfo(schedule, studentAgeGroup);
                timeSlotView.getCourses().add(courseInfo);
            }
        }
        
        return new ArrayList<>(dayMap.values());
    }

    public Classroom getScheduleClassroom(CourseSchedule schedule, List<Classroom> classrooms) {
        Classroom cr = classrooms.stream()
                .filter(t -> t != null && t.getId() != null && t.getId() == schedule.getClassroomId())
                .findFirst().orElse(null);
        schedule.setClassroom(cr);
        return  cr;
    }
    public Course getScheduleCourse(CourseSchedule schedule, List<Course> courses) {
        Course c = courses.stream()
                .filter(t -> t != null && t.getId() != null && t.getId() == schedule.getCourseId())
                .findFirst().orElse(null);
        schedule.setCourse(c);
        return  c;
    }
    public TimeSlot getScheduleTimeSlot(CourseSchedule schedule, List<TimeSlot> timeSlots) {
        TimeSlot ts = timeSlots.stream()
                .filter(t -> t != null && t.getId() != null && t.getId() == schedule.getTimeSlotId())
                .findFirst().orElse(null);
        schedule.setTimeSlot(ts);
        return ts;
    }


    // 单次查找：原始 for-loop，避免流/装箱开销
    /*public static TimeSlot findByIdOnce(List<TimeSlot> timeSlots, long id) {
        if (timeSlots == null) return null;
        for (TimeSlot ts : timeSlots) {
            if (ts != null) {
                Long tid = ts.getId();
                if (tid != null && tid.longValue() == id) return ts;
            }
        }
        return null;
    }*/

    /**
     * 构建课程信息
     */
    private CourseInfoDTO buildCourseInfo(CourseSchedule schedule, Integer studentAgeGroup) {
        CourseInfoDTO courseInfo = new CourseInfoDTO();
        courseInfo.setCourseId(schedule.getCourseId());
        courseInfo.setScheduleId(schedule.getId());

        courseInfo.setCourseName(schedule.getCourse().getCourseName());
        courseInfo.setClassroomName(schedule.getClassroom().getClassroomName());
        courseInfo.setMaxStudents(schedule.getCourse().getMaxStudents());
        courseInfo.setAgeGroup(schedule.getCourse().getAgeGroup());

        // 获取教室容量
        Integer classroomCapacity = schedule.getClassroom().getMaxCapacity();
        courseInfo.setClassroomCapacity(classroomCapacity);

        // 获取当前选课人数
        Integer currentStudents = enrollmentMapper.countEnrolledStudents(schedule.getId());
        courseInfo.setCurrentStudents(currentStudents);

        // 获取教师信息
        String teacherNames = schedule.getCourse().getTeacherNames();
        if (teacherNames != null) {
            courseInfo.setTeacherNames(Arrays.asList(teacherNames.split(",")));
        } else {
            courseInfo.setTeacherNames(Arrays.asList("张三", "李四")); // 默认教师
        }

        // 判断课程状态和是否可选（使用教室容量判断）
        String status = determineStatus(schedule, currentStudents, studentAgeGroup, classroomCapacity);
        courseInfo.setStatus(status);
        // EMPTY 和 AVAILABLE 状态都可以选课
        courseInfo.setCanEnroll(("AVAILABLE".equals(status) || "EMPTY".equals(status)) &&
                              schedule.getCourse().getAgeGroup().equals(studentAgeGroup));

        return courseInfo;
    }
    
    /**
     * 确定课程状态
     * 使用教室容量判断是否满员
     */
    private String determineStatus(CourseSchedule schedule, Integer currentStudents,
                                   Integer studentAgeGroup, Integer classroomCapacity) {
        Course course = schedule.getCourse();

        // 年龄组不匹配，显示灰色
        if (!course.getAgeGroup().equals(studentAgeGroup)) {
            return "DISABLED";
        }

        // 根据教室容量判断是否已满，显示灰色（不可选）
        if (currentStudents >= classroomCapacity) {
            return "FULL";
        }

        // 有人选课但未满，显示黄色
        if (currentStudents > 0) {
            return "AVAILABLE";
        }

        // 无人选课，显示白色
        return "EMPTY";
    }
}
