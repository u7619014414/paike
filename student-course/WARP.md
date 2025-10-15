# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

Student Course Selection System (学生选课系统) - A full-stack application for managing student course enrollments with time-based scheduling. The system consists of a Spring Boot backend and a Vue 3 frontend.

## Project Structure

```
student-course/
├── student-course-backend/   # Spring Boot 3.2.0 backend
└── student-course-frontend/   # Vue 3 + Vite + TypeScript frontend
```

## Common Development Commands

### Backend (Spring Boot)

**Working Directory:** `student-course-backend/`

```powershell
# Build the project
mvn clean package

# Run the application (default port: 45081)
mvn spring-boot:run

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=ClassName

# Run a single test method
mvn test -Dtest=ClassName#methodName

# Skip tests during build
mvn clean package -DskipTests

# View dependencies
mvn dependency:tree
```

### Frontend (Vue 3)

**Working Directory:** `student-course-frontend/`

```powershell
# Install dependencies (using npm or pnpm)
npm install
# or
pnpm install

# Start development server (default port: 3000)
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Type check
vue-tsc --noEmit
```

## Architecture & Design Patterns

### Backend Architecture

**Technology Stack:**
- Spring Boot 3.2.0 + Java 17
- MyBatis Plus for database operations
- MySQL 8 for data persistence
- Redis for caching (configurable)
- JWT for authentication
- BCrypt for password encryption

**Layer Structure:**
```
com.school.course/
├── config/          # Configuration classes (CORS, MyBatis, Redis)
├── controller/      # REST API endpoints
├── service/         # Business logic layer
├── mapper/          # MyBatis Plus data access layer
├── entity/          # JPA entities (database models)
├── dto/             # Data Transfer Objects
├── exception/       # Custom exceptions and global error handling
└── util/            # Utility classes (JWT, etc.)
```

**Key Architectural Decisions:**

1. **Transaction Management:** All service methods annotated with `@Transactional` handle database transactions automatically. This ensures data consistency across operations like course enrollment.

2. **Global Exception Handling:** `GlobalExceptionHandler` provides centralized error handling for:
   - `BusinessException`: Custom business logic errors (400)
   - `EntityNotFoundException`: Resource not found (404)
   - Validation errors: Bean validation failures (400)
   - Generic exceptions: Unexpected errors (500)

3. **API Response Format:** All endpoints return standardized `ApiResponse<T>` objects:
   ```java
   {
     "code": 200,
     "message": "操作成功",
     "data": { ... }
   }
   ```

4. **Authentication Flow:**
   - JWT tokens generated with 24-hour expiration
   - Tokens contain `studentId` claim for user identification
   - No backend interceptor; authentication logic handled in services

### Frontend Architecture

**Technology Stack:**
- Vue 3 (Composition API)
- TypeScript
- Vite for build tooling
- Element Plus UI components
- Pinia for state management
- Vue Router for navigation
- Axios for HTTP requests

**Directory Structure:**
```
src/
├── api/               # API client and request utilities
├── components/        # Reusable Vue components
├── router/            # Vue Router configuration
├── stores/            # Pinia state stores
├── types/             # TypeScript type definitions
└── views/             # Page-level components
```

**Key Frontend Patterns:**

1. **Centralized API Client:** `src/api/request.ts` provides:
   - Axios instance with interceptors
   - Automatic loading indicators
   - Standardized error handling with Element Plus notifications
   - Token injection (when implemented)

2. **State Management:** Pinia stores (`student.ts`, `course.ts`) manage:
   - Student authentication state
   - Course selection data
   - Global application state

3. **Route Guards:** Authentication checks in `router/index.ts`:
   - Redirect unauthenticated users to login
   - Prevent authenticated users from accessing login page
   - Token stored in localStorage

4. **Auto-Import Configuration:** 
   - Vue Composition API functions auto-imported
   - Element Plus components auto-registered
   - No need for explicit imports in most cases

## Data Model

### Core Entities

**Student (S_STUDENTS):**
- Auto-generated student code (STU + timestamp)
- Age-based grouping: 1=学前班(1-3), 2=小班(3-6), 3=中班(6-9), 4=大班(9-12)
- Registration status: 1=待审核, 2=已通过, 3=已拒绝
- BCrypt encrypted passwords

**Course (K_COURSES):**
- Course code, name, description
- Age group restrictions (matches student age groups)
- Max student capacity
- Teacher names (comma-separated string)

**CourseSchedule (T_COURSE_SCHEDULE):**
- Links Course + TimeSlot + Classroom + Date
- Status: 1=正常, 2=取消, 3=调课
- Represents a specific class offering at a time/place

**StudentEnrollment:**
- Links Student to CourseSchedule
- Enrollment status: 1=已选课, 2=已取消
- Tracks enrollment date

**TimeSlot:** 
- Defines time periods (e.g., "上午 08:00-09:30")
- Used to prevent scheduling conflicts

**Classroom:**
- Physical locations for courses

### Business Rules Implemented

1. **Enrollment Validation:**
   - Student must have `registrationStatus = 2` (approved)
   - Student age group must match course age group
   - Course must not be at capacity
   - No time slot conflicts for student
   - Cannot enroll in same course twice

2. **Automatic Fields:**
   - `createdAt` and `updatedAt` auto-filled by MyBatis Plus
   - Student code and course code auto-generated if not provided

## Configuration

### Backend Configuration

**Primary Config:** `student-course-backend/src/main/resources/application.yml`
- Server port: 45081
- Active profile: dev
- MyBatis Plus logging enabled

**Dev Config:** `student-course-backend/src/main/resources/application-dev.yml`
- MySQL connection: `jdbc:mysql://asdnn.com:45306/school_system`
- Redis connection: `redis://asdnn.com:45379`
- Environment variables supported: `MYSQL_USER`, `MYSQL_PASSWORD`

### Frontend Configuration

**Environment:** `student-course-frontend/.env`
- `VITE_API_BASE_URL`: Backend API base URL (default: http://localhost:45081/api/)

**Vite Config:** `student-course-frontend/vite.config.ts`
- Dev server port: 3000
- Proxy `/api` requests to `http://localhost:45081`
- Path alias: `@` maps to `src/`

## Development Workflow

### Starting Both Services

```powershell
# Terminal 1 - Backend
cd student-course-backend
mvn spring-boot:run

# Terminal 2 - Frontend  
cd student-course-frontend
npm run dev
```

Access frontend at: http://localhost:3000
Backend API at: http://localhost:45081

### API Development

When adding new endpoints:
1. Create/update entity in `entity/`
2. Create mapper interface in `mapper/` (extends `BaseMapper<T>`)
3. Implement service in `service/` with `@Transactional`
4. Create controller in `controller/` with proper REST mappings
5. Add DTO if needed in `dto/`
6. Update frontend API client in `src/api/`

### Adding Frontend Features

1. Define TypeScript types in `src/types/index.ts`
2. Create API functions in `src/api/`
3. Build components in `src/components/`
4. Add routes to `src/router/index.ts`
5. Create views in `src/views/`
6. Update stores if state management needed

## Database

The system uses MySQL with these naming conventions:
- Students: `S_*` prefix
- Courses: `K_*` prefix  
- Schedules: `T_*` prefix

MyBatis Plus handles:
- Auto-fill for `CREATED_AT` and `UPDATED_AT`
- Automatic ID generation (AUTO_INCREMENT)
- Camel case to underscore conversion

## Notes for AI Assistants

1. **Backend Changes:** Always use `@Transactional` on service methods that modify data
2. **Error Handling:** Throw `BusinessException` for validation errors, `EntityNotFoundException` for missing resources
3. **API Responses:** Always wrap responses in `ApiResponse.success()` or let exception handler manage errors
4. **Frontend API Calls:** Use the centralized `request` instance, not raw axios
5. **Type Safety:** Maintain TypeScript types in `src/types/index.ts` matching backend DTOs
6. **Authentication:** JWT token should be added to axios interceptors (currently handled manually)
7. **Windows Environment:** Use PowerShell commands, be mindful of path separators
