# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a multi-module school course management system ("排课系统") with separate student and teacher portals. The system manages course scheduling, classroom allocation, time slots, and student enrollment.

**Architecture**: Multiple Spring Boot backends (MyBatis Plus) + Vue 3 frontends, with a shared MySQL database.

## Project Structure

```
paike/
├── student-course/
│   ├── student-course-backend/          # Student system backend (Port: 45081)
│   └── student-course-frontend/         # Student system frontend (Port: 3000)
├── teacher-scheduling-system/
│   ├── backend/                         # Teacher backend (Port: 45082)
│   └── frontend/                        # Teacher frontend (Port: 45100)
├── classroom-timeslot-management/       # Classroom & timeslot admin UI (Port: 45103)
├── database-schema.sql                  # Shared database schema
├── MYBATIS_PLUS_MIGRATION.md            # MyBatis Plus migration documentation
├── start-all.bat                        # Windows: Start all services
├── start-backends.bat                   # Windows: Start backend services only
└── start-frontends.bat                  # Windows: Start frontend services only
```

**Active Modules**:
- `student-course/` - Student course selection system
- `teacher-scheduling-system/` - Teacher scheduling and course assignment
- `classroom-timeslot-management/` - Classroom and timeslot administration

## System Requirements

- **Java**: 17+
- **Node.js**: 16+
- **Maven**: 3.6+
- **MySQL**: 8.0+ (Port: 45306 or 3306)
- **Redis**: 6.0+ (optional for caching)

## Quick Start Commands

### One-Command Startup (Windows)

```bash
# Start all services (recommended for development)
start-all.bat

# Or start individually
start-backends.bat   # Only backend services
start-frontends.bat  # Only frontend services
```

### Database Setup
```bash
# Create database
mysql -u root -p -e "CREATE DATABASE school_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Import schema
mysql -u root -p school_system < database-schema.sql

# Import initial teacher data
mysql -u root -p school_system < teacher-scheduling-system/backend/src/main/resources/init-teachers.sql
```

### Student Course System

**Backend** (Port 45081):
```bash
cd student-course/student-course-backend
mvn clean install
mvn spring-boot:run
```

**Frontend** (Port 3000):
```bash
cd student-course/student-course-frontend
pnpm install
pnpm dev
```

**Build for Production**:
```bash
# Backend
cd student-course/student-course-backend
mvn clean package
java -jar target/student-course-backend-1.0.0.jar

# Frontend
cd student-course/student-course-frontend
pnpm build
# Deploy dist/ folder to web server
```

### Teacher Scheduling System

**Backend** (Port 45082):
```bash
cd teacher-scheduling-system/backend
mvn spring-boot:run

# Or use the script (Windows)
teacher-scheduling-system\start-backend.bat
```

**Frontend** (Port 45100):
```bash
cd teacher-scheduling-system/frontend
pnpm install
pnpm dev

# Or use the script (Windows)
teacher-scheduling-system\start-frontend.bat
```

### Classroom & Timeslot Management (Port 45103)

```bash
cd classroom-timeslot-management
pnpm install
pnpm dev

# Build for production
pnpm build
```

**Note**: This module connects to KrakenD API Gateway on port 45180, which proxies to backend services.

## Architecture & Technical Stack

### Backend (Spring Boot 3.2.0)
- **Framework**: Spring Boot, MyBatis Plus
- **Database**: MySQL 8.0 with MyBatis Plus 3.5.5
- **Caching**: Redis (optional)
- **Validation**: Bean Validation
- **API**: RESTful architecture

**Package Structure**:
```
# Student Course Backend (com.school.course)
com.school.course/
├── config/          # MybatisPlusConfig, MyMetaObjectHandler, CorsConfig, RedisConfig
├── controller/      # REST API endpoints (Student, Course, Enrollment, TimeSlot, Auth)
├── dto/            # Data Transfer Objects (ApiResponse, various DTOs)
├── entity/         # MyBatis Plus entities with @TableName/@TableField annotations
├── exception/      # GlobalExceptionHandler, BusinessException, EntityNotFoundException
├── mapper/         # MyBatis Plus mappers extending BaseMapper (replaces JPA repositories)
├── service/        # Business logic services
└── util/           # JwtUtil for authentication

# Teacher Scheduling Backend (com.yl.paike.teacher)
com.yl.paike.teacher/
├── config/          # MybatisPlusConfig, MyMetaObjectHandler, WebConfig, RedisConfig
├── controller/      # Teacher, Course, Schedule, Conflict, Classroom, TimeSlot controllers
├── dto/            # Result, PageResult, various DTOs
├── entity/         # Teacher, Course, CourseSchedule, TeacherAssignment, ConflictWarning, etc.
├── exception/      # GlobalExceptionHandler, BusinessException, ConflictException
├── mapper/         # MyBatis Plus mappers for all entities
├── service/        # Service interfaces and implementations (impl/)
└── util/           # Constants, BeanConverter
```

**Key Configuration**:
- Default ports: 45081 (student), 45082 (teacher with /api context path)
- ORM: MyBatis Plus 3.5.5 (`mybatis-plus-spring-boot3-starter` for Spring Boot 3.x)
- Database: MySQL 8.0+ on port 45306 (default) or 3306
- Database schema: Managed via SQL scripts (`database-schema.sql`)
- Active profile: `dev` (see `application-dev.yml` for database credentials)
- **IMPORTANT**: System migrated from JPA to MyBatis Plus (see `MYBATIS_PLUS_MIGRATION.md`)

### Frontend (Vue 3 + TypeScript)
- **Framework**: Vue 3 with Composition API
- **Language**: TypeScript
- **UI Library**: Element Plus
- **Build Tool**: Vite
- **State Management**: Pinia
- **HTTP Client**: Axios
- **Auto-imports**: unplugin-auto-import, unplugin-vue-components

**Directory Structure**:
```
src/
├── api/            # API request functions
├── components/     # Reusable components
├── router/         # Vue Router configuration
├── stores/         # Pinia stores
├── types/          # TypeScript type definitions
├── utils/          # Utility functions (request.ts for Axios)
└── views/          # Page components
```

**API Proxy Configuration**:
- Student frontend (port 3000) → Backend (port 45081)
- Classroom management (port 45103) → KrakenD Gateway (port 45180) → Backend services

## Database Schema

**Core Tables**:
- `T_TIME_SLOTS` - Time slots for scheduling (with day_of_week 1-7)
- `J_CLASSROOMS` - Classroom information
- `K_COURSES` - Course definitions (with age_group 1-4)
- `T_COURSE_SCHEDULE` - Links courses to time slots and classrooms
- `X_STUDENTS` - Student information
- `T_STUDENT_ENROLLMENT` - Student course enrollments

**Naming Conventions**:
- Table prefixes: T_ (time/schedule), J_ (classroom), K_ (course), X_ (student)
- Columns: snake_case (e.g., `course_code`, `max_capacity`)
- Boolean fields: `is_active`, `is_approved`
- Timestamps: `created_at`, `updated_at`

**Important**: MyBatis Plus entities use `@TableField` annotations to match exact database column names.

## Common Development Tasks

### Adding a New Entity
1. Create table in `database-schema.sql` with appropriate table prefix (T_, J_, K_, X_)
2. Create MyBatis Plus entity in `entity/` package:
   - Use `@TableName("TABLE_NAME")` for table mapping
   - Use `@TableId(value = "ID", type = IdType.AUTO)` for primary key
   - Use `@TableField("COLUMN_NAME")` for all fields
   - Add auto-fill annotations for `createdAt`/`updatedAt` if needed
3. Create Mapper interface extending `BaseMapper<Entity>` with `@Mapper` annotation
4. Create service class/interface for business logic
5. Create DTOs for API requests/responses
6. Create controller with REST endpoints
7. Update `@MapperScan` in `MybatisPlusConfig` if using new package

### Adding a New Frontend Page
1. Create Vue component in `src/views/`
2. Add route in `src/router/index.ts`
3. Create API functions in `src/api/`
4. Define TypeScript types in `src/types/`
5. Use Element Plus components for UI

### Database Configuration
Edit `student-course/student-course-backend/src/main/resources/application-dev.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://asdnn.com:45306/school_system
    username: school_user
    password: school_pass123
```

Or for teacher system: `teacher-scheduling-system/backend/src/main/resources/application-dev.yml`

### CORS Configuration
CORS is configured in backend `config/CorsConfig.java`. Allowed origins include localhost ports used by frontends.

## Port Reference

| Service | Port | Path | Context Path |
|---------|------|------|--------------|
| Student Backend | 45081 | student-course/student-course-backend | / |
| Student Frontend | 3000 | student-course/student-course-frontend | / |
| Teacher Backend | 45082 | teacher-scheduling-system/backend | /api |
| Teacher Frontend | 45100 | teacher-scheduling-system/frontend | / |
| Classroom Management | 45103 | classroom-timeslot-management | / |
| KrakenD Gateway | 45180 | (external service) | / |
| MySQL Database | 45306 or 3306 | - | - |
| Redis | 45379 | (optional) | - |

## API Endpoints

### Student System (Port 45081)

**Students**:
- `GET /api/students` - Get all students
- `POST /api/students/register` - Register new student
- `PUT /api/students/{id}` - Update student
- `DELETE /api/students/{id}` - Delete student

**Courses**:
- `GET /api/courses` - Get all courses
- `GET /api/courses/schedule` - Get course schedule view
- `POST /api/courses` - Create course
- `PUT /api/courses/{id}` - Update course
- `DELETE /api/courses/{id}` - Delete course

**Enrollments**:
- `GET /api/enrollments/student/{studentId}` - Get student enrollments
- `POST /api/enrollments` - Enroll student in course
- `DELETE /api/enrollments` - Remove enrollment

**Time Slots**:
- `GET /api/time-slots` - Get all time slots (paginated)
- `GET /api/time-slots/all` - Get all time slots
- `POST /api/time-slots` - Create time slot
- `POST /api/time-slots/batch-create` - Batch create time slots for multiple days
- `PUT /api/time-slots/{id}` - Update time slot
- `DELETE /api/time-slots/{id}` - Delete time slot

**Classrooms**:
- `GET /api/classrooms` - Get classrooms (paginated)
- `GET /api/classrooms/all` - Get all classrooms
- `POST /api/classrooms` - Create classroom
- `PUT /api/classrooms/{id}` - Update classroom
- `DELETE /api/classrooms/{id}` - Delete classroom
- `GET /api/classrooms/usage` - Get classroom usage statistics

**Authentication**:
- `POST /api/auth/login` - Student login (returns JWT token)

### Teacher System (Port 45082, Context: /api)

**Teachers**:
- `GET /api/teachers` - Get all teachers (with pagination)
- `POST /api/teachers` - Create new teacher
- `PUT /api/teachers/{id}` - Update teacher
- `DELETE /api/teachers/{id}` - Delete teacher

**Courses**:
- `GET /api/courses` - Get all courses
- `POST /api/courses` - Create course
- `PUT /api/courses/{id}` - Update course
- `DELETE /api/courses/{id}` - Delete course

**Schedules**:
- `GET /api/schedules` - Get course schedules
- `POST /api/schedules` - Create schedule
- `PUT /api/schedules/{id}` - Update schedule
- `DELETE /api/schedules/{id}` - Delete schedule
- `POST /api/schedules/assign-teacher` - Assign teacher to schedule

**Conflicts**:
- `GET /api/conflicts` - Get all conflicts
- `POST /api/conflicts/check` - Check for scheduling conflicts
- `GET /api/conflicts/schedule/{scheduleId}` - Get conflicts for specific schedule

**Classrooms**:
- `GET /api/classrooms` - Get all classrooms (paginated)
- `POST /api/classrooms` - Create classroom
- `PUT /api/classrooms/{id}` - Update classroom
- `DELETE /api/classrooms/{id}` - Delete classroom

**Time Slots**:
- `GET /api/time-slots` - Get all time slots (paginated)
- `POST /api/time-slots` - Create time slot
- `PUT /api/time-slots/{id}` - Update time slot
- `DELETE /api/time-slots/{id}` - Delete time slot

## Development Notes

### MyBatis Plus Migration
**CRITICAL**: This project was migrated from Spring Data JPA to MyBatis Plus. Key differences:
- Entities use `@TableName` and `@TableField` instead of `@Entity`, `@Table`, `@Column`
- Repositories are now called Mappers and extend `BaseMapper<Entity>`
- Pagination starts from 1 (not 0 like JPA)
- No automatic lazy loading - must manually load associations
- Methods like `save()` → `insert()` or `updateById()`
- See `MYBATIS_PLUS_MIGRATION.md` for complete migration details

### Entity Field Mapping
MyBatis Plus entities use exact column name mapping:
- Use `@TableName("TABLE_NAME")` for table mapping
- Use `@TableField("COLUMN_NAME")` for field mapping
- Auto-fill fields: `@TableField(value = "CREATED_AT", fill = FieldFill.INSERT)`
- Transient fields: `@TableField(exist = false)` for non-database fields

### Age Groups
Courses are categorized into 4 age groups (1-4). Students should only enroll in courses matching their age group.

### Day of Week
Time slots use `day_of_week` field: 1 = Monday, 2 = Tuesday, ..., 7 = Sunday

### Conflict Detection
The system prevents scheduling conflicts:
- Same classroom cannot be used by multiple courses at the same time slot
- Teacher assignments should avoid time conflicts
- `ConflictDetectionService` handles all conflict checking logic

### Authentication (Student System)
Student system has JWT-based authentication:
- `AuthController` handles login with `/api/auth/login`
- `JwtUtil` manages token generation and validation
- Student passwords stored in database

### Redis Caching
Redis is configured but optional. The system works with in-memory caching if Redis is unavailable.

### MyBatis Plus Query Patterns
**Basic CRUD**:
```java
// Insert
entity.generateCode(); // Call manually if needed
mapper.insert(entity);

// Update
mapper.updateById(entity);

// Query
Entity entity = mapper.selectById(id);
List<Entity> all = mapper.selectList(null);

// Delete
mapper.deleteById(id);
```

**Conditional Queries**:
```java
// Using LambdaQueryWrapper (type-safe, recommended)
LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Student::getAgeGroup, 1)
       .like(Student::getName, "张")
       .orderByDesc(Student::getCreatedAt);
List<Student> students = mapper.selectList(wrapper);

// Using @Select annotation in Mapper
@Select("SELECT * FROM X_STUDENTS WHERE STUDENT_CODE = #{code}")
Student findByCode(@Param("code") String code);
```

**Pagination** (remember: pages start at 1):
```java
// In controller, accept pageNum from frontend (0-based)
// In service, convert to MyBatis Plus format
Page<Entity> page = new Page<>(pageNum + 1, pageSize);
mapper.selectPage(page, queryWrapper);
return page; // Contains total, records, current, size
```

## Testing

**Backend Tests**:
```bash
# Student system
cd student-course/student-course-backend
mvn test

# Teacher system
cd teacher-scheduling-system/backend
mvn test
```

**Run Backend Without Tests** (faster for development):
```bash
mvn spring-boot:run -DskipTests
```

**Frontend**:
No test framework currently configured. Manual testing recommended.

## Troubleshooting

### Port Already in Use
```bash
# Windows
netstat -ano | findstr :<PORT>
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:<PORT> | xargs kill
```

### Database Connection Issues
1. Verify MySQL is running on configured port (45306 or 3306)
2. Check credentials in `application-dev.yml`
3. Ensure database `school_system` exists
4. Verify JDBC connection string format

### Frontend Cannot Connect to Backend
1. Check backend is running and accessible
2. Verify proxy configuration in `vite.config.ts`
3. Check CORS settings in backend
4. Inspect browser console for errors

### Maven Build Fails
1. Ensure Java 17+ is installed: `java -version`
2. Clear Maven cache: `mvn clean`
3. Check `pom.xml` dependencies
4. Verify MyBatis Plus version is `3.5.5` with `mybatis-plus-spring-boot3-starter`

### MyBatis Plus Mapper Not Found
```
Error: Invalid bound statement (not found): com.school.course.mapper.StudentMapper.selectById
```
**Solution**:
- Ensure `@MapperScan("com.school.course.mapper")` is in `MybatisPlusConfig`
- Verify Mapper interface has `@Mapper` annotation
- Check `mybatis-plus.mapper-locations: classpath*:/mapper/**/*.xml` in `application.yml`

### Auto-fill Not Working
```
createdAt and updatedAt are null after insert
```
**Solution**:
- Verify `MyMetaObjectHandler` has `@Component` annotation
- Check field annotations: `@TableField(value = "CREATED_AT", fill = FieldFill.INSERT)`
- Ensure field names in handler match entity field names (camelCase)

### pnpm Not Found
```bash
# Install pnpm globally
npm install -g pnpm

# Or use npm instead
npm install
npm run dev
```
