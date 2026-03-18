package com.studyhub.controller;

import com.studyhub.dto.*;
import com.studyhub.enums.CourseLevel;
import com.studyhub.enums.UserRole;
import com.studyhub.enums.UserStatus;
import com.studyhub.model.User;
import com.studyhub.security.StudyHubUserDetails;
import com.studyhub.service.CourseManagementService;
import com.studyhub.service.FileUploadService;
import com.studyhub.service.UserManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserManagementService userManagementService;
    private final CourseManagementService courseManagementService;
    private final com.studyhub.service.CourseContentService courseContentService;
    private final FileUploadService fileUploadService;

    @ModelAttribute
    public void exposeRequestUri(HttpServletRequest request, Model model) {
        model.addAttribute("requestURI", request.getRequestURI());
    }

    @GetMapping
    public String dashboard(@AuthenticationPrincipal StudyHubUserDetails principal, Model model) {
        model.addAttribute("userStats", userManagementService.getDashboardStats());
        if (principal.getUser().getRole() == UserRole.MANAGER) {
            model.addAttribute("totalCourses", courseManagementService.countPublishedCoursesByManager(principal.getUser().getId()));
        } else {
            model.addAttribute("totalCourses", courseManagementService.countPublishedCourses());
        }
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userList(
            @AuthenticationPrincipal StudyHubUserDetails principal,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Long currentUserId = principal.getUser().getId();

        model.addAttribute("users", userManagementService.findAll(currentUserId, search, role, status, pageable));
        model.addAttribute("search", search);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("size", size);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("statuses", UserStatus.values());
        model.addAttribute("queryString", buildQueryString(search, role, status, sortBy, direction, size));
        return "admin/users/list";
    }

    @GetMapping("/users/new")
    public String createUserForm(Model model) {
        model.addAttribute("createUserDTO", new CreateUserDTO());
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("statuses", UserStatus.values());
        return "admin/users/create";
    }

    @PostMapping("/users")
    public String createUser(@Valid @ModelAttribute CreateUserDTO createUserDTO,
                             BindingResult result,
                             @RequestParam(required = false) MultipartFile avatar,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("statuses", UserStatus.values());
            return "admin/users/create";
        }
        try {
            if (avatar != null && !avatar.isEmpty()) {
                createUserDTO.setProfileImageUrl(fileUploadService.uploadImage(avatar));
            }
            var user = userManagementService.createUser(createUserDTO);
            redirectAttributes.addFlashAttribute("successMessage", "User created and credentials sent to " + user.getEmail() + ".");
            return "redirect:/admin/users/" + user.getId();
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "error.dto", e.getMessage());
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("statuses", UserStatus.values());
            return "admin/users/create";
        }
    }

    @GetMapping("/users/{id}")
    public String userDetail(
            @AuthenticationPrincipal StudyHubUserDetails principal,
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (principal.getUser().getId().equals(id)) {
            redirectAttributes.addFlashAttribute("infoMessage", "To edit your own account, go to your profile.");
            return "redirect:/profile";
        }
        var user = userManagementService.findById(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("statuses", UserStatus.values());
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setFullName(user.getFullName());
        updateUserDTO.setEmail(user.getEmail());
        updateUserDTO.setUsername(user.getUsername());
        updateUserDTO.setMobile(user.getMobile());
        model.addAttribute("updateUserDTO", updateUserDTO);
        return "admin/users/detail";
    }

    @PostMapping("/users/{id}")
    public String updateUserInfo(@PathVariable Long id,
                                 @Valid @ModelAttribute UpdateUserDTO updateUserDTO,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("user", userManagementService.findById(id));
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("statuses", UserStatus.values());
            return "admin/users/detail";
        }
        try {
            userManagementService.updateInfo(id, updateUserDTO);
            redirectAttributes.addFlashAttribute("successMessage", "User info updated successfully.");
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg.contains("email")) {
                result.rejectValue("email", "error.dto", msg);
            } else {
                result.rejectValue("username", "error.dto", msg);
            }
            model.addAttribute("user", userManagementService.findById(id));
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("statuses", UserStatus.values());
            return "admin/users/detail";
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userManagementService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
            return "redirect:/admin/users";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users/" + id;
        }
    }

    @PostMapping("/users/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status,
            RedirectAttributes redirectAttributes) {
        try {
            userManagementService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update status: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/users/{id}/role")
    public String updateRole(
            @PathVariable Long id,
            @RequestParam UserRole role,
            RedirectAttributes redirectAttributes) {
        try {
            userManagementService.updateRole(id, role);
            redirectAttributes.addFlashAttribute("successMessage", "User role updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update role: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @GetMapping("/courses")
    public String courseList(
            @AuthenticationPrincipal StudyHubUserDetails principal,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long managerId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        User currentUser = principal.getUser();
        if (currentUser.getRole() == UserRole.MANAGER) {
            managerId = currentUser.getId();
        }

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        var pageable = PageRequest.of(page, size, sort);
        model.addAttribute("courses", courseManagementService.findAll(search, categoryId, managerId, minPrice, maxPrice, pageable));
        model.addAttribute("categories", courseManagementService.getCategories());
        model.addAttribute("managers", courseManagementService.getManagers());
        model.addAttribute("search", search);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedManager", managerId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        model.addAttribute("size", size);
        model.addAttribute("queryString", buildCourseQueryString(search, categoryId, managerId, minPrice, maxPrice, sortBy, direction, size));
        return "admin/courses/list";
    }

    @GetMapping("/courses/{id}")
    public String courseDetail(@AuthenticationPrincipal StudyHubUserDetails principal, @PathVariable Long id, Model model) {
        CourseDetailDTO course = courseManagementService.findById(id);
        verifyCourseAccess(course, principal.getUser());

        model.addAttribute("course", course);
        model.addAttribute("categories", courseManagementService.getCategories());
        model.addAttribute("managers", courseManagementService.getManagers());
        model.addAttribute("levels", CourseLevel.values());
        return "admin/courses/detail";
    }

    @PostMapping("/courses/{id}")
    public String updateCourse(@AuthenticationPrincipal StudyHubUserDetails principal,
                               @PathVariable Long id,
                               @ModelAttribute CourseUpdateDTO courseUpdateDTO,
                               RedirectAttributes redirectAttributes) {
        try {
            CourseDetailDTO course = courseManagementService.findById(id);
            verifyCourseAccess(course, principal.getUser());

            courseManagementService.updateCourse(id, courseUpdateDTO, principal.getUser().getRole());
            redirectAttributes.addFlashAttribute("successMessage", "Course updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update course: " + e.getMessage());
        }
        return "redirect:/admin/courses/" + id;
    }

    @PostMapping("/courses/{id}/delete")
    public String deleteCourse(@AuthenticationPrincipal StudyHubUserDetails principal,
                               @PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            CourseDetailDTO course = courseManagementService.findById(id);
            verifyCourseAccess(course, principal.getUser());
            courseManagementService.deleteCourse(id);
            redirectAttributes.addFlashAttribute("successMessage", "Course deleted successfully.");
            return "redirect:/admin/courses";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/courses/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete course: " + e.getMessage());
            return "redirect:/admin/courses/" + id;
        }
    }

    @GetMapping("/courses/{id}/content")
    public String courseContent(@AuthenticationPrincipal StudyHubUserDetails principal, @PathVariable Long id, Model model) {
        CourseDetailDTO course = courseManagementService.findById(id);
        verifyCourseAccess(course, principal.getUser());

        model.addAttribute("course", course);
        model.addAttribute("chapters", courseContentService.getChaptersByCourseId(id));
        model.addAttribute("newChapter", new ChapterDTO());
        model.addAttribute("newLesson", new LessonDTO());
        return "admin/courses/content";
    }

    @PostMapping("/courses/{id}/chapters")
    public String addChapter(@AuthenticationPrincipal StudyHubUserDetails principal, @PathVariable Long id, @ModelAttribute ChapterDTO chapterDTO, RedirectAttributes redirectAttributes) {
        try {
            verifyCourseAccess(courseManagementService.findById(id), principal.getUser());
            courseContentService.addChapter(id, chapterDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Chapter added.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/courses/" + id + "/content";
    }

    @PostMapping("/courses/{id}/chapters/{chapterId}")
    public String updateChapter(@AuthenticationPrincipal StudyHubUserDetails principal, @PathVariable Long id, @PathVariable Long chapterId, @ModelAttribute ChapterDTO chapterDTO, RedirectAttributes redirectAttributes) {
        try {
            verifyCourseAccess(courseManagementService.findById(id), principal.getUser());
            courseContentService.updateChapter(chapterId, chapterDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Chapter updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/courses/" + id + "/content";
    }

    @PostMapping("/courses/{id}/chapters/{chapterId}/delete")
    public String deleteChapter(@AuthenticationPrincipal StudyHubUserDetails principal, @PathVariable Long id, @PathVariable Long chapterId, RedirectAttributes redirectAttributes) {
        try {
            verifyCourseAccess(courseManagementService.findById(id), principal.getUser());
            courseContentService.deleteChapter(chapterId);
            redirectAttributes.addFlashAttribute("successMessage", "Chapter deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/courses/" + id + "/content";
    }

    @PostMapping("/courses/{id}/chapters/{chapterId}/lessons")
    public String addLesson(@AuthenticationPrincipal StudyHubUserDetails principal, @PathVariable Long id, @PathVariable Long chapterId, @ModelAttribute LessonDTO lessonDTO, RedirectAttributes redirectAttributes) {
        try {
            verifyCourseAccess(courseManagementService.findById(id), principal.getUser());
            courseContentService.addLesson(chapterId, lessonDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Lesson added.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/courses/" + id + "/content";
    }

    @PostMapping("/courses/{id}/lessons/{lessonId}")
    public String updateLesson(@AuthenticationPrincipal StudyHubUserDetails principal, @PathVariable Long id, @PathVariable Long lessonId, @ModelAttribute LessonDTO lessonDTO, RedirectAttributes redirectAttributes) {
        try {
            verifyCourseAccess(courseManagementService.findById(id), principal.getUser());
            courseContentService.updateLesson(lessonId, lessonDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Lesson updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/courses/" + id + "/content";
    }

    @PostMapping("/courses/{id}/lessons/{lessonId}/delete")
    public String deleteLesson(@AuthenticationPrincipal StudyHubUserDetails principal, @PathVariable Long id, @PathVariable Long lessonId, RedirectAttributes redirectAttributes) {
        try {
            verifyCourseAccess(courseManagementService.findById(id), principal.getUser());
            courseContentService.deleteLesson(lessonId);
            redirectAttributes.addFlashAttribute("successMessage", "Lesson deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/courses/" + id + "/content";
    }

    private void verifyCourseAccess(CourseDetailDTO course, User user) {
        if (user.getRole() == UserRole.MANAGER && !user.getId().equals(course.getManagerId())) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to manage this course.");
        }
    }

    private String buildQueryString(String search, UserRole role, UserStatus status, String sortBy, String direction, int size) {
        StringBuilder sb = new StringBuilder();
        if (search != null && !search.isBlank()) sb.append("&search=").append(search);
        if (role != null) sb.append("&role=").append(role);
        if (status != null) sb.append("&status=").append(status);
        if (sortBy != null) sb.append("&sortBy=").append(sortBy);
        if (direction != null) sb.append("&direction=").append(direction);
        sb.append("&size=").append(size);
        return sb.toString();
    }

    private String buildCourseQueryString(String search, Long categoryId, Long managerId,
                                          BigDecimal minPrice, BigDecimal maxPrice,
                                          String sortBy, String direction, int size) {
        StringBuilder sb = new StringBuilder();
        if (search != null && !search.isBlank()) sb.append("&search=").append(search);
        if (categoryId != null) sb.append("&categoryId=").append(categoryId);
        if (managerId != null) sb.append("&managerId=").append(managerId);
        if (minPrice != null) sb.append("&minPrice=").append(minPrice);
        if (maxPrice != null) sb.append("&maxPrice=").append(maxPrice);
        if (sortBy != null) sb.append("&sortBy=").append(sortBy);
        if (direction != null) sb.append("&direction=").append(direction);
        sb.append("&size=").append(size);
        return sb.toString();
    }
}
