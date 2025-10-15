package com.yl.paike.teacher.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yl.paike.teacher.dto.ConflictWarningDTO;
import com.yl.paike.teacher.dto.PageResult;
import com.yl.paike.teacher.dto.Result;
import com.yl.paike.teacher.service.ConflictDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/conflicts")
@RequiredArgsConstructor
@Slf4j
public class ConflictController {
    
    private final ConflictDetectionService conflictDetectionService;
    
    @GetMapping("/warnings")
    public Result<PageResult<ConflictWarningDTO>> getConflictWarnings(
            @RequestParam(required = false) Integer conflictType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<ConflictWarningDTO> warningPage = conflictDetectionService.getConflictWarnings(
            conflictType, status, startDate, endDate, pageNum, pageSize);

        PageResult<ConflictWarningDTO> pageResult = new PageResult<>();
        pageResult.setContent(warningPage.getRecords());
        pageResult.setTotalElements(warningPage.getTotal());
        pageResult.setTotalPages(warningPage.getPages());
        pageResult.setPage(pageNum);
        pageResult.setSize(pageSize);

        return Result.success(pageResult);
    }
    
    @PutMapping("/warnings/{id}/resolve")
    public Result<Void> resolveConflictWarning(
            @PathVariable Long id,
            @RequestParam Integer resolution,
            @RequestParam(required = false) String remark) {
        log.info("处理冲突警告: ID={}, 处理结果={}", id, resolution);
        conflictDetectionService.resolveConflictWarning(id, resolution, remark);
        return Result.success(null, "处理成功");
    }
    
    @PostMapping("/check")
    public Result<Void> performConflictCheck(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        log.info("执行冲突检测: {} 至 {}", startDate, endDate);
        conflictDetectionService.performFullConflictCheck(startDate, endDate);
        return Result.success(null, "冲突检测完成");
    }

    @GetMapping("/check-teachers")
    public Result<com.yl.paike.teacher.dto.ConflictCheckResult> checkTeacherConflicts(
            @RequestParam String teacherIds,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam Long timeSlotId) {
        log.info("检查教师冲突: teacherIds={}, date={}, timeSlotId={}", teacherIds, date, timeSlotId);

        java.util.List<Long> ids = java.util.Arrays.stream(teacherIds.split(","))
            .map(Long::parseLong)
            .collect(java.util.stream.Collectors.toList());

        com.yl.paike.teacher.dto.ConflictCheckResult result =
            conflictDetectionService.checkTeacherConflicts(ids, date, timeSlotId);

        return Result.success(result);
    }

    @GetMapping("/check-classroom")
    public Result<com.yl.paike.teacher.dto.ConflictCheckResult> checkClassroomConflict(
            @RequestParam Long classroomId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam Long timeSlotId) {
        log.info("检查教室冲突: classroomId={}, date={}, timeSlotId={}", classroomId, date, timeSlotId);

        com.yl.paike.teacher.dto.ConflictCheckResult result =
            conflictDetectionService.checkClassroomConflicts(classroomId, date, timeSlotId);

        return Result.success(result);
    }
}
