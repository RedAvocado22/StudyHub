# StudyHub - Team Rules

## Workflow
- Discuss approach with the reviewer before implementing anything non-trivial.
- No code comments or Javadoc. Code must be self-explanatory through naming.
- Review all changed files yourself before committing. Never commit broken code.
- Never add `Co-Authored-By` lines to commit messages.

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
| `FileUploadService` | Save/delete uploaded files (images + documents) |

## Code Style
- Use Lombok: `@RequiredArgsConstructor` + `final` fields for injection everywhere.
- Services that modify data must be `@Transactional`.
- No N+1 queries: use FETCH JOIN or `@EntityGraph` when loading associations.

## How to Code a New Feature

Follow the same pattern used by every existing feature. Example: `HomeController` → `CourseManagementService` → `CourseRepository`.

### Step-by-step

1. **DTO first** — create a DTO in `dto/` with `@Data @AllArgsConstructor @NoArgsConstructor`.
2. **Repository** — add query methods to the existing `@Repository` interface. Use `@EntityGraph` on any method that touches lazy associations to avoid N+1.
3. **Service** — add a method to the matching `@Service` (one service per feature). Mark the class or method `@Transactional`. Return DTOs, never entities.
4. **Controller** — add a thin `@GetMapping` / `@PostMapping` method. Call the service, put results in `Model` or `RedirectAttributes`, return a view name or redirect.
5. **Template** — create or edit an HTML file in `templates/`. Use `layout:decorate="~{layout/base}"` (or `layout/auth` for auth pages). Put content inside `<div layout:fragment="content">`.

### Form submission pattern
```java
@PostMapping("/some-action")
public String doAction(@Valid @ModelAttribute SomeDTO dto,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
        return "some/page";          // return same page, errors shown inline
    }
    try {
        someService.doSomething(dto);
        redirectAttributes.addFlashAttribute("successMessage", "Action completed.");
        return "redirect:/some-page";
    } catch (IllegalArgumentException e) {
        model.addAttribute("errorMessage", e.getMessage());
        return "some/page";
    }
}
```

## Notification System (Toast Messages)

All pages show Bootstrap toast notifications automatically. Both layout files (`layout/base.html` and `layout/auth.html`) render toasts for `successMessage` and `errorMessage` flash attributes.

### How to trigger a toast from a controller

**Success toast** — use `RedirectAttributes` on any redirect:
```java
redirectAttributes.addFlashAttribute("successMessage", "Your message here.");
return "redirect:/some-page";
```

**Error toast** — same pattern:
```java
redirectAttributes.addFlashAttribute("errorMessage", "Something went wrong.");
return "redirect:/some-page";
```

**On the same page (no redirect)** — add to `Model` instead:
```java
model.addAttribute("errorMessage", "Validation failed.");
return "some/page";
```

### How to trigger a toast from a Spring Security handler

Security handlers (success/failure/logout) run before DispatcherServlet, so `RedirectAttributes` is unavailable. Use `SessionFlashMapManager` directly:
```java
FlashMap flashMap = new FlashMap();
flashMap.put("successMessage", "Welcome back!");
new SessionFlashMapManager().saveOutputFlashMap(flashMap, request, response);
```

### Auth events that already show toasts

| Event | Message | Handler |
|---|---|---|
| Login success | "Welcome back, [fullName]!" | `CustomAuthSuccessHandler` |
| Login failed (wrong credentials) | "Invalid email/username or password." | `CustomAuthFailureHandler` |
| Login failed (unverified) | "Your email has not been verified yet." | `CustomAuthFailureHandler` |
| Login failed (inactive account) | "Your account has been deactivated. Please contact support." | `CustomAuthFailureHandler` |
| Logout | "You have been logged out successfully." | `CustomLogoutSuccessHandler` |

Do not add duplicate messages for these events — they are already handled.

## Dummy Data Reference

`DataSeeder.java` (`config/DataSeeder.java`) resets and re-seeds the database on every startup (`ddl-auto=create`). All test accounts share the same password: **`password123`**

### Test accounts

| Username | Email | Role | Status |
|---|---|---|---|
| `admin` | admin@studyhub.com | ADMIN | ACTIVE |
| `manager1` | manager1@studyhub.com | MANAGER | ACTIVE |
| `manager2` | manager2@studyhub.com | MANAGER | ACTIVE |
| `marketing1` | marketing1@studyhub.com | MARKETING | ACTIVE |
| `alice` | alice@studyhub.com | MEMBER | ACTIVE |
| `bob` | bob@studyhub.com | MEMBER | ACTIVE |
| `carol` | carol@studyhub.com | MEMBER | ACTIVE |
| `dave` | dave@studyhub.com | MEMBER | ACTIVE |
| `eve` | eve@studyhub.com | MEMBER | ACTIVE |
| `unverified` | unverified@studyhub.com | MEMBER | UNVERIFIED |

### Seeded data summary

- **Categories (Settings)**: Programming, Design, Marketing, Data Science, Business
- **Courses** (5 total):
  - *Java Programming Fundamentals* — BEGINNER, 499,000đ, published, manager1
  - *Spring Boot Masterclass* — INTERMEDIATE, 799,000đ, published, manager1
  - *UI/UX Design Essentials* — BEGINNER, 350,000đ, published, manager2
  - *Machine Learning with Python* — ADVANCED, 999,000đ, **unpublished**, manager2
  - *Digital Marketing Strategy* — INTERMEDIATE, 450,000đ, published, manager1
- **Enrollments** (10 total): mix of APPROVED, PENDING, and REJECTED statuses across alice, bob, carol, dave, eve

### Extending dummy data

Add new entities at the bottom of the appropriate `seed*` method in `DataSeeder`. Keep consistent with the patterns already used (builder, no manual IDs, use saved references for FK relationships).

## File Uploads (`FileUploadService`)

- Use `uploadImage(MultipartFile)` for avatars and thumbnails.
  - Accepted: `image/jpeg`, `image/png`, `image/gif`, `image/webp`. Max **10 MB**.
- Use `uploadDocument(MultipartFile)` for PDFs and Office files.
  - Accepted: `.pdf`, `.doc`, `.docx`, `.xls`, `.xlsx`. Max **10 MB**.
- Both methods return a URL string (e.g. `/uploads/images/uuid.jpg`). Store this directly in the entity field.
- Call `delete(oldUrl)` before replacing an existing file to remove the old one from disk.
- Never handle file I/O in controllers or repositories. Always delegate to `FileUploadService`.
- Forms that upload files must have `enctype="multipart/form-data"` and use `<input type="file" name="...">`.

### Controller example
```java
@PostMapping("/profile/avatar")
public String uploadAvatar(@RequestParam MultipartFile avatar,
                           @AuthenticationPrincipal StudyHubUserDetails principal,
                           RedirectAttributes redirectAttributes) {
    try {
        String url = fileUploadService.uploadImage(avatar);
        userProfileService.updateAvatar(principal.getUser().getId(), url);
        redirectAttributes.addFlashAttribute("successMessage", "Avatar updated.");
    } catch (IllegalArgumentException e) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
    }
    return "redirect:/profile";
}
```

## Reusable Fragments

### Toast (`fragments/toast.html`)
Included automatically in `layout/base.html` and `layout/admin.html`. Trigger from any controller:
```java
redirectAttributes.addFlashAttribute("successMessage", "Done!");
redirectAttributes.addFlashAttribute("errorMessage", "Failed.");
redirectAttributes.addFlashAttribute("infoMessage", "Note.");
```

### Pagination (`fragments/pagination.html`)
```html
<div th:replace="~{fragments/pagination :: pagination(
    page=${users},
    baseUrl='/admin/users',
    queryString=${queryString})}">
</div>
```
- `page` — Spring `Page<?>` object from the service.
- `baseUrl` — path without query params.
- `queryString` — all active filter params as a string, each prefixed with `&`, plus `&size=N`. Do **not** include `&page=`. Build this in the controller and pass it via the model.

```java
// Controller example
private String buildQueryString(String search, UserRole role, int size) {
    StringBuilder sb = new StringBuilder();
    if (search != null && !search.isBlank()) sb.append("&search=").append(search);
    if (role != null) sb.append("&role=").append(role);
    sb.append("&size=").append(size);
    return sb.toString();
}
```

### Confirm Modal (`fragments/confirm-modal.html`)
Include the modal on the page, then point a trigger button at it:
```html
<!-- 1. Include the modal (once per action per page) -->
<div th:replace="~{fragments/confirm-modal :: confirm-modal(
    modalId='deleteUser',
    title='Delete User',
    message='This action cannot be undone. Are you sure?',
    formAction=@{/admin/users/{id}/delete(id=${user.id})},
    buttonLabel='Delete',
    buttonClass='btn-danger')}">
</div>

<!-- 2. Trigger button (anywhere on the page) -->
<button class="btn btn-danger btn-sm"
        data-bs-toggle="modal" data-bs-target="#deleteUser">
    Delete
</button>
```

## UI
- **Bootstrap 5** for all styling. No raw `style=""` attributes.
- **Thymeleaf Layout Dialect**: every page extends a layout using `layout:decorate`.
  - Auth pages (login, register, forgot/reset password) → `layout:decorate="~{layout/auth}"`
  - Admin pages → `layout:decorate="~{layout/admin}"`
  - All other pages → `layout:decorate="~{layout/base}"`
- Header, footer, sidebar, toast, pagination, and confirm-modal stay in `fragments/` and are included via `th:replace`.
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
