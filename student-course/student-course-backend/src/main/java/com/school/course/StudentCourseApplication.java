// src/main/java/com/school/course/StudentCourseApplication.java
package com.school.course;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class StudentCourseApplication {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("启动学生选课系统后端服务");
        System.out.println("========================================");

        SpringApplication.run(StudentCourseApplication.class, args);

        System.out.println("========================================");
        System.out.println("学生选课系统启动完成！");
        System.out.println("访问地址: http://localhost:45081");
        System.out.println("API文档: http://localhost:45081/actuator/health");
        System.out.println("========================================");
    }
}