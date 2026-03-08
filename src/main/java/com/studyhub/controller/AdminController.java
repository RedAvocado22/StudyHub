package com.studyhub.controller;

import com.studyhub.dto.CreateUserDTO;
import com.studyhub.enums.CourseLevel;
import com.studyhub.enums.UserRole;
import com.studyhub.enums.UserStatus;
import com.studyhub.security.StudyHubUserDetails;
import com.studyhub.service.CourseManagementService;
import com.studyhub.service.UserManagementService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserManagementService userManagementService;
    private final CourseManagementService courseManagementService;

    @ModelAttribute
    public void exposeRequestUri(HttpServletRequest request, Model model) {
        model.addAttribute("requestURI", request.getRequestURI());
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("userStats", userManagementService.getDashboardStats());
        model.addAttribute("totalCourses", courseManagementService.countPublishedCourses());
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String userList(
            @AuthenticationPrincipal StudyHubUserDetails principal,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Long currentUserId = principal.getUser().getId();

        model.addAttribute("users", userManagementService.findAll(currentUserId, search, role, status, pageable));
        model.addAttribute("search", search);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("size", size);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("statuses", UserStatus.values());
        model.addAttribute("queryString", buildQueryString(search, role, status, size));
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
    public String createUser(@ModelAttribute CreateUserDTO createUserDTO, RedirectAttributes redirectAttributes) {
        try {
            var user = userManagementService.createUser(createUserDTO);
            redirectAttributes.addFlashAttribute("successMessage", "User created and credentials sent to " + user.getEmail() + ".");
            return "redirect:/admin/users/" + user.getId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users/new";
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
        model.addAttribute("user", userManagementService.findById(id));
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("statuses", UserStatus.values());
        return "admin/users/detail";
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
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long managerId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        model.addAttribute("courses", courseManagementService.findAll(search, categoryId, managerId, minPrice, maxPrice, pageable));
        model.addAttribute("categories", courseManagementService.getCategories());
        model.addAttribute("managers", courseManagementService.getManagers());
        model.addAttribute("search", search);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedManager", managerId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("size", size);
        model.addAttribute("queryString", buildCourseQueryString(search, categoryId, managerId, minPrice, maxPrice, size));
        return "admin/courses/list";
    }

    @GetMapping("/courses/{id}")
    public String courseDetail(@PathVariable Long id, Model model) {
        model.addAttribute("course", courseManagementService.findById(id));
        model.addAttribute("categories", courseManagementService.getCategories());
        model.addAttribute("managers", courseManagementService.getManagers());
        model.addAttribute("levels", CourseLevel.values());
        return "admin/courses/detail";
    }

    private String buildQueryString(String search, UserRole role, UserStatus status, int size) {
        StringBuilder sb = new StringBuilder();
        if (search != null && !search.isBlank()) sb.append("&search=").append(search);
        if (role != null) sb.append("&role=").append(role);
        if (status != null) sb.append("&status=").append(status);
        sb.append("&size=").append(size);
        return sb.toString();
    }

    private String buildCourseQueryString(String search, Long categoryId, Long managerId,
                                           BigDecimal minPrice, BigDecimal maxPrice, int size) {
        StringBuilder sb = new StringBuilder();
        if (search != null && !search.isBlank()) sb.append("&search=").append(search);
        if (categoryId != null) sb.append("&categoryId=").append(categoryId);
        if (managerId != null) sb.append("&managerId=").append(managerId);
        if (minPrice != null) sb.append("&minPrice=").append(minPrice);
        if (maxPrice != null) sb.append("&maxPrice=").append(maxPrice);
        sb.append("&size=").append(size);
        return sb.toString();
    }
}
