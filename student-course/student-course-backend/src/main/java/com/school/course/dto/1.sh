#!/bin/bash

# 创建DTO包目录（如果不存在）
mkdir -p /data/1vueproj/paike/student-course-backend/src/main/java/com/school/course/dto
mkdir -p /data/1vueproj/paike/student-course-backend/src/main/java/com/school/course/exception

# 创建ApiResponse.java
cat > /data/1vueproj/paike/student-course-backend/src/main/java/com/school/course/dto/ApiResponse.java << 'EOF'
package com.school.course.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(200, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
EOF

# 创建StudentDTO.java
cat > /data/1vueproj/paike/student-course-backend/src/main/java/com/school/course/dto/StudentDTO.java << 'EOF'
package com.school.course.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentDTO {
    private Long id;
    private String studentCode;
    private String studentName;
    private Integer age;
    private Integer ageGroup;
    private String parentName;
    private String parentPhone;
    private String parentEmail;
    private Integer registrationStatus;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
EOF

# 创建StudentRegistrationDTO.java
cat > /data/1vueproj/paike/student-course-backend/src/main/java/com/school/course/dto/StudentRegistrationDTO.java << 'EOF'
package com.school.course.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class StudentRegistrationDTO {
    @NotBlank(message = "学生姓名不能为空")
    @Size(min = 2, max = 20, message = "学生姓名长度应在2-20个字符")
    private String studentName;
    
    @NotNull(message = "年龄不能为空")
    @Min(value = 1, message = "年龄不能小于1岁")
    @Max(value = 12, message = "年龄不能大于12岁")
    private Integer age;
    
    @NotBlank(message = "家长姓名不能为空")
    @Size(min = 2, max = 20, message = "家长姓名长度应在2-20个字符")
    private String parentName;
    
    @NotBlank(message = "家长电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String parentPhone;
    
    @Email(message = "邮箱格式不正确")
    private String parentEmail;
}
EOF

# 创建CourseScheduleViewDTO.java
cat > /data/1vueproj/paike/student-course-backend/src/main/java/com/school/course/dto/CourseScheduleViewDTO.java << 'EOF'
package com.school.course.dto;

import lombok.Data;
import java.util.List;

@Data
public class CourseScheduleViewDTO {
    private Integer dayOfWeek;
    private String dayName;
    private List<TimeSlotViewDTO> timeSlots;
}
EOF

# 创建TimeSlotViewDTO.java
cat > /data/1vueproj/paike/student-course-backend/src/main/java/com/school/course/dto/TimeSlotViewDTO.java << 'EOF'
package com.school.course.dto;

import lombok.Data;
import java.time.LocalTime;
import java.util.List;

@Data
public class TimeSlotViewDTO {
    private Long timeSlotId;
    private String slotName;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<CourseInfoDTO> courses;
}
EOF

# 创建CourseInfoDTO.java
cat > /data/1vueproj/paike/student-course-backend/src/main/java/com/school/course/dto/CourseInfoDTO.java << 'EOF'
package com.school.course.dto;

import lombok.Data;
import java.util.List;

@Data
public class CourseInfoDTO {
    private Long courseId;
    private Long scheduleId;
    private String courseName;
    private String classroomName;
    private List<String> teacherNames;
    private Integer maxStudents;
    private Integer currentStudents;
    private String status; // FULL(红色), AVAILABLE(黄色), EMPTY(白色), DISABLED(灰色)
    private Integer ageGroup;
    private Boolean canEnroll;
}
EOF

# 创建EnrollmentDTO.java
cat > /data/1vueproj/paike/student-course-backend/src/main/java/com/school/course/dto/EnrollmentDTO.java << 'EOF'
package com.school.course.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EnrollmentDTO {
    private Long studentId;
    private Long courseScheduleId;
    private Integer enrollmentStatus;
    private LocalDateTime enrollmentDate;
}
EOF

# 创建BusinessException.java
cat > /data/1vueproj/paike/student-course-backend/src/main/java/com/school/course/exception/BusinessException.java << 'EOF'
package com.school.course.exception;

public class BusinessException extends RuntimeException {
    private int code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
EOF

# 创建EntityNotFoundException.java
cat > /data/1vueproj/paike/student-course-backend/src/main/java/com/school/course/exception/EntityNotFoundException.java << 'EOF'
package com.school.course.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
EOF

# 创建GlobalExceptionHandler.java
cat > /data/1vueproj/paike/student-course-backend/src/main/java/com/school/course/exception/GlobalExceptionHandler.java << 'EOF'
package com.school.course.exception;

import com.school.course.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return ResponseEntity.status(e.getCode())
                .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleEntityNotFoundException(EntityNotFoundException e) {
        log.warn("Entity not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, e.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<Object>> handleValidationException(Exception e) {
        Map<String, String> errors = new HashMap<>();
        
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            ex.getBindingResult().getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            ex.getBindingResult().getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
        }
        
        log.warn("Validation exception: {}", errors);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "参数验证失败"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception e) {
        log.error("Unexpected exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "系统内部错误"));
    }
}
EOF

echo "DTO和Exception类文件创建完成！"
