package com.studyhub.controller;

import com.studyhub.dto.SettingUpsertDTO;
import com.studyhub.enums.SettingStatus;
import com.studyhub.service.SettingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/settings")
public class AdminSettingController {

    private final SettingService settingService;

    @ModelAttribute
    public void exposeRequestUri(HttpServletRequest request, Model model) {
        model.addAttribute("requestURI", request.getRequestURI());
    }

    @GetMapping
    public String listSettings(@RequestParam(required = false) String type,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "type") String sortBy,
                               @RequestParam(defaultValue = "asc") String direction,
                               Model model) {
        model.addAttribute("settings", settingService.getSettings(type, status, keyword, sortBy, direction));
        model.addAttribute("types", settingService.getSettingTypes());
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        return "admin/settings/list";
    }

    @GetMapping("/new")
    public String newSettingPage(Model model) {
        SettingUpsertDTO dto = new SettingUpsertDTO();
        dto.setStatus(SettingStatus.ACTIVE);
        populateDetailModel(model, dto, true, null);
        return "admin/settings/detail";
    }

    @PostMapping("/new")
    public String createSetting(@Valid @ModelAttribute("settingDTO") SettingUpsertDTO settingDTO,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            populateDetailModel(model, settingDTO, true, null);
            return "admin/settings/detail";
        }
        try {
            settingService.createSetting(settingDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Setting created successfully.");
            return "redirect:/admin/settings";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateDetailModel(model, settingDTO, true, null);
            return "admin/settings/detail";
        }
    }

    @GetMapping("/{id}")
    public String editSettingPage(@PathVariable Long id,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            SettingUpsertDTO dto = settingService.getSettingDetail(id);
            populateDetailModel(model, dto, false, id);
            return "admin/settings/detail";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/settings";
        }
    }

    @PostMapping("/{id}")
    public String updateSetting(@PathVariable Long id,
                                @Valid @ModelAttribute("settingDTO") SettingUpsertDTO settingDTO,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            populateDetailModel(model, settingDTO, false, id);
            return "admin/settings/detail";
        }
        try {
            settingService.updateSetting(id, settingDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Setting updated successfully.");
            return "redirect:/admin/settings";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            populateDetailModel(model, settingDTO, false, id);
            return "admin/settings/detail";
        }
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id,
                               @RequestParam(required = false) String type,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "type") String sortBy,
                               @RequestParam(defaultValue = "asc") String direction,
                               RedirectAttributes redirectAttributes) {
        try {
            settingService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Setting status updated.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/settings?type=" + nullToEmpty(type)
                + "&status=" + nullToEmpty(status)
                + "&keyword=" + nullToEmpty(keyword)
                + "&sortBy=" + sortBy
                + "&direction=" + direction;
    }

    private void populateDetailModel(Model model, SettingUpsertDTO dto, boolean isNew, Long id) {
        model.addAttribute("settingDTO", dto);
        model.addAttribute("typeOptions", settingService.getActiveSettingTypes());
        model.addAttribute("isNew", isNew);
        model.addAttribute("settingId", id);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
