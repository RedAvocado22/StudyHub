# StudyHub - Team Rules

## Workflow
- Discuss approach with the reviewer before implementing anything non-trivial.
- No code comments or Javadoc. Code must be self-explanatory through naming.

## Architecture

### Layer responsibilities
- **Controller**: thin — only calls services, binds DTOs, returns views. No business logic.
- **Service**: all business logic lives here. One service per feature (see table below).
- **Repository**: data access only. No logic beyond query definitions.
- **DTO**: used for all data passed between Controller and Service. Never pass raw entities to controllers.

### 1 Service = 1 Feature

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
| `EmailService` | All outbound email — used by other services, never by controllers |

## Code Style
- Use Lombok: `@RequiredArgsConstructor` + `final` fields for injection everywhere.
- Services that modify data must be `@Transactional`.
- No N+1 queries: use FETCH JOIN or `@EntityGraph` when loading associations.

## UI
- **Bootstrap 5** for all styling. No raw `style=""` attributes.
- **Thymeleaf Layout Dialect**: every page extends a layout using `layout:decorate`.
  - Auth pages (login, register, forgot/reset password) → `layout:decorate="~{layout/auth}"`
  - All other pages → `layout:decorate="~{layout/base}"`
- Header, footer, and sidebar stay in `fragments/` and are included via `th:replace`.
- Page content goes inside `<div layout:fragment="content">`.

## Commit Messages

Format: `type: short description` (max 70 characters)

| Type | When to use |
|---|---|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code change that is not a feat or fix |
| `style` | UI/CSS changes only |
| `docs` | Documentation changes |
| `chore` | Build, config, or dependency changes |

Rules:
- Use imperative mood: `add login page` not `added login page`
- Lowercase, no period at the end
- Reference issue number when applicable: `feat: add enrollment form (#12)`

## Package Structure
```
com.studyhub
├── config/        Spring configuration classes
├── controller/    @Controller classes
├── dto/           DTO classes
├── enums/         Enum types
├── model/         @Entity classes
├── repository/    @Repository interfaces
├── security/      Spring Security classes
└── service/       @Service classes
```
