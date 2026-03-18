package com.studyhub.controller;

import com.studyhub.dto.UserProfileDTO;
import com.studyhub.enums.UserRole;
import com.studyhub.model.User;
import com.studyhub.security.StudyHubUserDetails;
import com.studyhub.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Controller
public class UserProfileController {

    UserProfileService userProfileService;

    @Autowired
    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/profile")
    public String getUserProfile(@AuthenticationPrincipal StudyHubUserDetails userDetails,
                                 Model model) {
        User user = userProfileService.getUserById(userDetails.getUser().getId());
        // Nếu user chưa có ảnh, gán chuỗi base64 mặc định vào luôn
        if (user.getProfileImage() == null || user.getProfileImage().isEmpty()) {
            String defaultImg = "data:image/jpeg;base64,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"; // Chuỗi dài của bạn
            user.setProfileImage(defaultImg);
        }
        UserProfileDTO userProfileDTO = new UserProfileDTO();
        userProfileDTO.setRole(user.getRole());
        userProfileDTO.setUsername(user.getUsername());
        userProfileDTO.setFullName(user.getFullName());
        userProfileDTO.setMobile(user.getMobile());
        model.addAttribute("user", user);
        model.addAttribute("userProfileDTO", userProfileDTO);
        model.addAttribute("roles", UserRole.values());
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal StudyHubUserDetails userDetails,
                                @Valid @ModelAttribute("userProfileDTO") UserProfileDTO userUpdate,
                                BindingResult result,
                                @RequestParam("imageFile") MultipartFile imageFile,
                                Model model) throws IOException {
        Long id = userDetails.getUser().getId();
        User existingUser = userProfileService.getUserById(id);
        if (result.hasErrors()) {
            model.addAttribute("user", existingUser);
            model.addAttribute("roles", UserRole.values());
            return "profile";
        }

        // XỬ LÝ ẢNH: Chuyển sang Base64
        if (imageFile != null && !imageFile.isEmpty()) {
            byte[] bytes = imageFile.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(bytes);
            String contentType = imageFile.getContentType();

            // Tạo chuỗi data URI để hiển thị trực tiếp trên thẻ img src
            String fullBase64 = "data:" + contentType + ";base64," + base64Image;
            existingUser.setProfileImage(fullBase64);
        }
        //update
        existingUser.setFullName(userUpdate.getFullName());
        existingUser.setUsername(userUpdate.getUsername());
        existingUser.setMobile(userUpdate.getMobile());
        existingUser.setRole(userUpdate.getRole());

        userProfileService.save(existingUser);
        //update user in session
        StudyHubUserDetails updatedUserDetails = new StudyHubUserDetails(existingUser);
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUserDetails,
                null,
                updatedUserDetails.getAuthorities()
        );
        //overide to SecurityContext
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        return "redirect:/";
    }
}
