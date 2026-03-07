# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Team Rules

### Workflow
- Discuss approach with the reviewer before implementing anything non-trivial. Ask, don't assume.
- Never add code comments or Javadoc. Code must be self-explanatory through naming.

### Architecture
- **1 service = 1 feature**. Each `@Service` owns exactly one concern. Current services:

  | Service | Responsibility |
  |---|---|
  | `AuthService` | Register, verify email, resend verification |
  | `PasswordService` | Forgot password, reset password, change password |
  | `UserProfileService` | View/edit own profile |
  | `UserManagementService` | Admin CRUD on user accounts |
  | `SettingService` | CRUD + activate/deactivate settings |
  | `CourseManagementService` | CRUD courses, publish/unpublish |
  | `CourseContentService` | CRUD chapters and lessons |
  | `EnrollmentService` | Enroll, approve/reject, import/export |
  | `PaymentService` | PayOS + VnPay integration |
  | `LessonAccessService` | My courses, lesson viewer, progress tracking |
  | `PostService` | CRUD posts, publish/hide |
  | `BlogService` | Public blog list, blog details, comments |
  | `EmailService` | All outbound email — used by other services |

- Controllers are thin: no business logic, only call services and return views.
- DTOs for all Controller↔Service data transfer. Never pass raw entity objects to controllers.
- Use `@RequiredArgsConstructor` + `final` fields (Lombok) for injection everywhere.

### Code style
- Lombok: `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Data`, `@RequiredArgsConstructor` — use whichever fits.
- Services that modify data must be `@Transactional`.
- No N+1 queries: use FETCH JOIN or `@EntityGraph` when loading associations.

### UI
- **Bootstrap 5** for everything. No raw inline `style=""` attributes.
- **Thymeleaf Layout Dialect**: all pages extend `layout/base.html` or `layout/auth.html` using `layout:decorate`.
- Header, footer, and sidebar must stay in `fragments/` and be included via `th:replace`.
- Auth pages (login, register, forgot/reset password) extend `layout/auth.html`.
- All other pages extend `layout/base.html`.

## Project Overview

StudyHub is a Learning Management System (LMS) built with Spring Boot 3.3.4 / Java 17. It supports course creation, enrollment with online payment (PayOS bank transfer, VnPay internet banking), lesson delivery (video, PDF, rich text), and a blog system.

## Commands

```bash
# Run the application (requires MySQL running)
mvn spring-boot:run

# Build
mvn clean package

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=StudyHubApplicationTests
```

## Database Setup

MySQL is required. The datasource in `src/main/resources/application.properties` connects to `localhost:3306/studyhub` with `username=sa` / `password=123`. The DB is auto-created on first run (`createDatabaseIfNotExist=true`). Schema is managed by Hibernate with `ddl-auto=update`.

Mail credentials in `application.properties` (Gmail SMTP) must be configured before email features work.

## Architecture

The required multi-layer architecture (per `StudyHub_Specs.pdf`):

| Layer | Annotation | Notes |
|---|---|---|
| Presentation | `@Controller` | Returns Thymeleaf views |
| Business Logic | `@Service` | IoC/DI |
| Data Access | `@Repository` | Spring Data JPA |
| DTOs | `@Component` | Passed between layers |
| UI | Thymeleaf + Bootstrap 5 | Header/footer/sidebar as reusable fragments |

Thymeleaf fragments for header, footer, and sidebar **must** be kept as separate components and included across pages via `th:replace` / `th:insert`.

## Domain Model

```
Setting (self-referencing: type_id → Setting)  ← used as generic master data (categories, etc.)
User (roles: ADMIN, MANAGER, MEMBER; statuses: UNVERIFIED, ACTIVE, INACTIVE)
Course → Chapter (ordered) → Lesson (ordered; contentType: VIDEO, PDF, TEXT)
Course.category → Setting
Course.manager → User
Enrollment (user → User, course → Course; statuses: PENDING, APPROVED, REJECTED)
```

- `Setting` is the hierarchical master data table. Settings with no `type` are the root types (e.g., "Course Category", "User Role"). Settings with a `type` are values under that type.
- `Lesson.preview = true` means the lesson is viewable without enrollment.
- `Course.published` controls public visibility.
- `Enrollment.progress` tracks learner progress (0–100%).

## Role-Based Access

| Feature | ADMIN | MANAGER | MEMBER | Guest |
|---|---|---|---|---|
| System Settings, User Mgmt | ✓ | | | |
| Post Management | ✓ | | ✓ (own) | |
| Course List/Details | ✓ | ✓ (own) | | |
| Course Content/Chapter/Lesson | ✓ | ✓ (own) | | |
| Enrollment List/Details | ✓ | ✓ (own courses) | | |
| Learning Enroll / Payment | ✓ | ✓ | ✓ | ✓ |
| My Enrollments / My Courses / Lesson Viewer | ✓ | ✓ | ✓ | |
| Public pages (Home, Courses, Blog) | ✓ | ✓ | ✓ | ✓ |

Manager restrictions: can only access courses assigned to them (`course.manager == currentUser`), cannot change course price or published status.

## Security

`SecurityConfig` currently has all requests permitted (`anyRequest().permitAll()`) and CSRF disabled — this is a scaffold state. Authorization enforcement must be implemented in `@Service` or `@Controller` layer based on the role matrix above. `BCryptPasswordEncoder` is configured as the password encoder bean.

## Key Business Rules

- **Email verification**: new registrations are `UNVERIFIED` until the user clicks the verification link. Unverified users cannot log in but can request a new verification email.
- **Password reset links** expire after 30 minutes (`app.password-reset-expiry-minutes=30`).
- **Enrollment**: both Guest and Member can enroll. If the enrollee email doesn't exist in the system, a new account is auto-created with a random password sent by email. Notification emails go to both the registering user and the enrolled person.
- **Deletions**: a User can only be deleted if they have no transactions. A Course can only be deleted if it has no related enrollments.
- **Setting name uniqueness** is scoped within the same `type` group.
- **Admin Dashboard**: Admins see stats for all courses; Managers see only their assigned courses.
