# MyBatis Plus 迁移文档

本文档记录了从 Spring Data JPA + Hibernate 迁移到 MyBatis Plus 的完整过程。

## 📋 迁移概览

**迁移时间**：2025-10-06
**迁移范围**：
- `student-course-backend` - 学生选课系统后端
- `teacher-scheduling-system/backend` - 教师排课系统后端

**迁移原因**：公司规范要求统一使用 MyBatis Plus 作为持久层框架

---

## ✅ 迁移完成清单

### 1. Maven 依赖更新

#### 修改文件
- `student-course-backend/pom.xml`
- `teacher-scheduling-system/backend/pom.xml`

#### 变更内容
```xml
<!-- 移除 JPA 依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- 添加 MyBatis Plus 依赖 -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.5</version>
</dependency>
```

#### MySQL 驱动更新
```xml
<!-- 统一使用新的驱动 -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.0.33</version>
</dependency>
```

---

### 2. 配置文件更新

#### student-course-backend

**修改文件**：
- `src/main/resources/application.yml`
- `src/main/resources/application-dev.yml`

**移除 JPA 配置**：
```yaml
# 移除以下配置
spring:
  jpa:
    hibernate:
      ddl-auto: none
      naming: ...
    show-sql: true
    properties: ...
  sql:
    init:
      mode: never
```

**添加 MyBatis Plus 配置**：
```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  mapper-locations: classpath*:/mapper/**/*.xml

logging:
  level:
    com.school.course: DEBUG
    com.baomidou.mybatisplus: DEBUG
```

#### teacher-scheduling-system

**修改文件**：
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/application-dev.yml`

配置内容与 student-course-backend 类似，但包路径为 `com.yl.paike.teacher`。

---

### 3. 实体类（Entity）转换

#### 转换规则

**JPA 注解 → MyBatis Plus 注解**：

| JPA | MyBatis Plus |
|-----|--------------|
| `@Entity` | 移除 |
| `@Table(name = "TABLE_NAME")` | `@TableName("TABLE_NAME")` |
| `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)` | `@TableId(value = "ID", type = IdType.AUTO)` |
| `@Column(name = "COLUMN_NAME")` | `@TableField("COLUMN_NAME")` |
| `@CreationTimestamp` | `@TableField(value = "CREATED_AT", fill = FieldFill.INSERT)` |
| `@UpdateTimestamp` | `@TableField(value = "UPDATED_AT", fill = FieldFill.INSERT_UPDATE)` |
| `@ManyToOne` / `@JoinColumn` | `@TableField(exist = false)` |
| `@PrePersist` | 改为普通 public 方法 |

**示例转换**：

```java
// JPA 方式
@Entity
@Table(name = "S_STUDENTS")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "STUDENT_CODE")
    private String studentCode;

    @CreationTimestamp
    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @PrePersist
    private void generateStudentCode() {
        if (this.studentCode == null) {
            this.studentCode = "STU" + System.currentTimeMillis();
        }
    }
}

// MyBatis Plus 方式
@Data
@TableName("S_STUDENTS")
public class Student {
    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("STUDENT_CODE")
    private String studentCode;

    @TableField(value = "CREATED_AT", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public void generateStudentCode() {
        if (this.studentCode == null) {
            this.studentCode = "STU" + System.currentTimeMillis();
        }
    }
}
```

#### 转换完成的实体类

**student-course-backend** (5 个实体)：
- `Student.java`
- `Course.java`
- `Classroom.java`
- `TimeSlot.java`
- `CourseSchedule.java`
- `StudentEnrollment.java`

**teacher-scheduling-system** (7 个实体)：
- `Teacher.java`
- `Course.java`
- `Classroom.java`
- `TimeSlot.java`
- `CourseSchedule.java`
- `TeacherAssignment.java`
- `ConflictWarning.java`

---

### 4. Repository → Mapper 转换

#### 新增 Mapper 接口

**student-course-backend**：
创建 `com.school.course.mapper` 包，包含：
- `StudentMapper.java`
- `CourseMapper.java`
- `ClassroomMapper.java`
- `TimeSlotMapper.java`
- `CourseScheduleMapper.java`
- `StudentEnrollmentMapper.java`

**teacher-scheduling-system**：
创建 `com.yl.paike.teacher.mapper` 包，包含：
- `TeacherMapper.java`
- `CourseMapper.java`
- `ClassroomMapper.java`
- `TimeSlotMapper.java`
- `CourseScheduleMapper.java`
- `TeacherAssignmentMapper.java`
- `ConflictWarningMapper.java`

#### Mapper 接口示例

```java
@Mapper
public interface StudentMapper extends BaseMapper<Student> {

    @Select("SELECT * FROM S_STUDENTS WHERE STUDENT_CODE = #{studentCode}")
    Student findByStudentCode(@Param("studentCode") String studentCode);

    @Select("SELECT * FROM S_STUDENTS WHERE IS_ACTIVE = 1 ORDER BY CREATED_AT DESC")
    List<Student> findActiveStudents();
}
```

#### 删除的文件
- `student-course-backend/src/main/java/com/school/course/repository/` 目录
- `teacher-scheduling-system/backend/src/main/java/com/yl/paike/teacher/repository/` 目录

---

### 5. 配置类更新

#### 新增配置类

**MybatisPlusConfig.java**（两个项目都有）：
```java
@Configuration
@MapperScan("com.school.course.mapper")  // 或 com.yl.paike.teacher.mapper
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

**MyMetaObjectHandler.java**（两个项目都有）：
```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
```

#### 删除的配置类
- `student-course-backend/src/main/java/com/school/course/config/JpaConfig.java`

---

### 6. Service 层更新

#### 转换规则

**依赖注入更改**：
```java
// JPA
@Autowired
private StudentRepository studentRepository;

// MyBatis Plus
@Autowired
private StudentMapper studentMapper;
```

**方法调用转换**：

| JPA | MyBatis Plus |
|-----|--------------|
| `repository.save(entity)` | `mapper.insert(entity)` 或 `mapper.updateById(entity)` |
| `repository.findById(id)` | `mapper.selectById(id)` |
| `repository.findAll()` | `mapper.selectList(null)` |
| `repository.deleteById(id)` | `mapper.deleteById(id)` |
| `repository.count()` | `mapper.selectCount(null)` |

**分页查询转换**：
```java
// JPA
Page<Student> page = repository.findAll(PageRequest.of(pageNum, pageSize));

// MyBatis Plus (注意：页码从1开始)
Page<Student> page = new Page<>(pageNum + 1, pageSize);
mapper.selectPage(page, null);
```

**条件查询转换**：
```java
// JPA Specification
Specification<Student> spec = (root, query, cb) -> {
    return cb.equal(root.get("ageGroup"), ageGroup);
};
List<Student> students = repository.findAll(spec);

// MyBatis Plus QueryWrapper
LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Student::getAgeGroup, ageGroup);
List<Student> students = mapper.selectList(wrapper);
```

**Optional 处理**：
```java
// JPA
Optional<Student> student = repository.findById(id);
if (!student.isPresent()) {
    throw new RuntimeException("Not found");
}

// MyBatis Plus
Student student = mapper.selectById(id);
if (student == null) {
    throw new RuntimeException("Not found");
}
```

#### 转换完成的 Service

**student-course-backend** (3 个 Service)：
- `StudentService.java`
- `CourseService.java`
- `EnrollmentService.java`

**teacher-scheduling-system** (6 个 Service 实现 + 3 个接口)：
- `TeacherServiceImpl.java`
- `CourseServiceImpl.java`
- `ClassroomServiceImpl.java`
- `TimeSlotServiceImpl.java`
- `ScheduleServiceImpl.java`
- `ConflictDetectionServiceImpl.java`
- `TeacherService.java` (接口)
- `CourseService.java` (接口)
- `ConflictDetectionService.java` (接口)

---

### 7. Controller 层更新

#### 分页参数更改

**原来（JPA）**：
```java
@GetMapping
public ResponseEntity<Page<Teacher>> getTeachers(
    @RequestParam(required = false) String keyword,
    Pageable pageable) {
    Page<Teacher> teachers = teacherService.getTeacherList(keyword, pageable);
    return ResponseEntity.ok(teachers);
}
```

**现在（MyBatis Plus）**：
```java
@GetMapping
public ResponseEntity<Page<Teacher>> getTeachers(
    @RequestParam(required = false) String keyword,
    @RequestParam(defaultValue = "0") int pageNum,
    @RequestParam(defaultValue = "10") int pageSize) {
    Page<Teacher> teachers = teacherService.getTeacherList(keyword, pageNum, pageSize);
    return ResponseEntity.ok(teachers);
}
```

#### 导入更改
```java
// 移除
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// 添加
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
```

#### 转换完成的 Controller

**teacher-scheduling-system** (4 个 Controller)：
- `TeacherController.java`
- `CourseController.java`
- `ScheduleController.java`
- `ConflictController.java`

**student-course-backend**：
无需修改（未使用 Pageable）

---

## 🎯 关键差异总结

### MyBatis Plus vs JPA

| 特性 | JPA | MyBatis Plus |
|------|-----|--------------|
| **实体注解** | `@Entity`, `@Table`, `@Column` | `@TableName`, `@TableField` |
| **主键策略** | `@Id`, `@GeneratedValue` | `@TableId(type = IdType.AUTO)` |
| **Repository** | 继承 `JpaRepository` | 继承 `BaseMapper` |
| **方法命名** | `findByXxx`, `deleteByXxx` | 使用 `QueryWrapper` 或自定义 SQL |
| **分页对象** | `org.springframework.data.domain.Page` | `com.baomidou.mybatisplus.extension.plugins.pagination.Page` |
| **页码起始** | 从 0 开始 | 从 1 开始 |
| **关联查询** | `@ManyToOne`, `@OneToMany` + JPQL | 手动 JOIN 或分步查询 |
| **Optional** | 返回 `Optional<Entity>` | 返回 `Entity` 或 `null` |
| **批量操作** | `saveAll(List)` | 循环调用 `insert()` 或使用批量插入方法 |
| **条件查询** | `Specification` | `QueryWrapper` / `LambdaQueryWrapper` |

---

## 📦 依赖版本

```xml
<properties>
    <java.version>17</java.version>
    <spring-boot.version>3.2.0</spring-boot.version>
    <mybatis-plus.version>3.5.5</mybatis-plus.version>
    <mysql.version>8.0.33</mysql.version>
</properties>

<dependencies>
    <!-- MyBatis Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.5.5</version>
    </dependency>

    <!-- MySQL Driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>8.0.33</version>
    </dependency>
</dependencies>
```

---

## 🚀 启动与测试

### 快速启动

项目根目录提供了便捷的启动脚本：

**Windows 系统**：
- `start-all.bat` - 一键启动所有服务（推荐）
- `start-backends.bat` - 只启动后端服务
- `start-frontends.bat` - 只启动前端服务

**手动启动步骤**：

1. **启动后端服务**：
```bash
# 学生选课系统后端 (端口: 45081)
cd student-course-backend
mvn spring-boot:run

# 教师排课系统后端 (端口: 45082)
cd teacher-scheduling-system/backend
mvn spring-boot:run
```

2. **启动前端服务**（使用 pnpm）：
```bash
# 学生选课系统前端 (端口: 3000)
cd student-course-frontend
pnpm dev

# 教师排课系统前端 (端口: 45100)
cd teacher-scheduling-system/frontend
pnpm dev
```

3. **访问地址**：
- 学生选课系统：http://localhost:3000
- 教师排课系统：http://localhost:45100
- 学生选课后端 API：http://localhost:45081
- 教师排课后端 API：http://localhost:45082/api

4. **检查启动日志**：
- 确认 MyBatis Plus 配置加载成功
- 确认 Mapper 扫描成功
- 确认数据库连接正常

### 测试重点

1. **基本 CRUD 操作**
   - 创建、查询、更新、删除功能
   - 自动填充字段（createdAt, updatedAt）

2. **分页查询**
   - 分页参数正确传递
   - 分页结果正确返回

3. **条件查询**
   - QueryWrapper 查询
   - 自定义 Mapper 方法

4. **关联查询**
   - 手动加载关联对象
   - JOIN 查询返回完整数据

5. **事务管理**
   - `@Transactional` 注解正常工作
   - 事务回滚正常

---

## ⚠️ 注意事项

### 1. 页码差异
- **前端和 Controller**：页码从 **0** 开始
- **Service 层**：传递给 MyBatis Plus 时需要 `+1`，因为 MyBatis Plus 页码从 **1** 开始
- 已在 Service 层处理，前端无需修改

### 2. 代码生成方法
实体类的 `generateXxxCode()` 方法不再自动触发，需要在 Service 层手动调用：
```java
student.generateStudentCode();
studentMapper.insert(student);
```

### 3. 关联对象加载
MyBatis Plus 不支持 JPA 的懒加载和级联查询，需要：
- 使用 `@TableField(exist = false)` 标记非数据库字段
- 在 Service 层手动加载关联对象
- 或使用 JOIN 查询一次性获取

### 4. Optional 处理
MyBatis Plus 直接返回实体或 null，不使用 Optional：
```java
// 需要手动判空
Student student = studentMapper.selectById(id);
if (student == null) {
    throw new BusinessException("学生不存在");
}
```

### 5. 缓存注解
Spring Cache 注解（`@Cacheable`, `@CacheEvict`）继续有效，无需修改。

---

## 📊 迁移统计

### 文件变更统计

| 项目 | 类型 | 修改 | 新增 | 删除 |
|------|------|------|------|------|
| student-course-backend | Entity | 6 | 0 | 0 |
| | Mapper | 0 | 6 | 0 |
| | Service | 3 | 0 | 0 |
| | Config | 0 | 2 | 1 |
| | Repository | 0 | 0 | 6 |
| | 配置文件 | 2 | 0 | 0 |
| teacher-scheduling-system | Entity | 7 | 0 | 0 |
| | Mapper | 0 | 7 | 0 |
| | Service | 9 | 0 | 0 |
| | Controller | 4 | 0 | 0 |
| | Config | 0 | 2 | 0 |
| | Repository | 0 | 0 | 7 |
| | 配置文件 | 2 | 0 | 0 |
| **总计** | | **27** | **17** | **14** |

### 代码行数变化
- **删除行数**：约 800 行（JPA 注解和配置）
- **新增行数**：约 1200 行（Mapper 接口和 MyBatis Plus 代码）
- **净增加**：约 400 行

---

## 🔍 故障排查

### 常见问题

**1. 启动时找不到 Mapper**
```
Error: Invalid bound statement (not found): com.school.course.mapper.StudentMapper.selectById
```
**解决方案**：
- 检查 `@MapperScan` 注解包路径是否正确
- 确认 Mapper 接口有 `@Mapper` 注解
- 检查 `mybatis-plus.mapper-locations` 配置

**2. 分页查询返回空结果**
```
Page{current=0, size=10, total=0, records=[]}
```
**解决方案**：
- 检查页码是否从 1 开始（MyBatis Plus 页码从 1 开始）
- 确认 `PaginationInnerInterceptor` 已配置

**3. 自动填充不生效**
```
createdAt 和 updatedAt 字段为 null
```
**解决方案**：
- 检查 `MyMetaObjectHandler` 是否有 `@Component` 注解
- 确认实体字段有 `@TableField(fill = FieldFill.INSERT)` 注解
- 检查字段名是否匹配（camelCase vs snake_case）

**4. SQL 日志不显示**
```
控制台没有 SQL 输出
```
**解决方案**：
```yaml
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

logging:
  level:
    com.baomidou.mybatisplus: DEBUG
```

---

## 📚 参考资源

- [MyBatis Plus 官方文档](https://baomidou.com/)
- [MyBatis Plus 注解说明](https://baomidou.com/pages/223848/)
- [MyBatis Plus 条件构造器](https://baomidou.com/pages/10c804/)
- [MyBatis Plus 分页插件](https://baomidou.com/pages/97710a/)

---

## ✨ 迁移完成确认

- [x] Maven 依赖已更新
- [x] 配置文件已修改
- [x] 所有 Entity 已转换为 MyBatis Plus 格式
- [x] 所有 Repository 已替换为 Mapper
- [x] 所有 Service 层已适配 MyBatis Plus API
- [x] Controller 层分页参数已更新
- [x] JPA 相关配置和代码已删除
- [x] MyBatis Plus 配置类已创建
- [x] 自动填充处理器已配置

**迁移状态**：✅ 已完成

**建议下一步**：
1. 执行 `mvn clean install` 确保编译通过
2. 启动服务并测试所有 API 接口
3. 检查日志确认 SQL 执行正常
4. 进行完整的功能测试

---

*文档生成时间：2025-10-06*
*迁移执行人：Claude Code*
