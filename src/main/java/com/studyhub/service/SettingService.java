package com.studyhub.service;

import com.studyhub.dto.SettingListItemDTO;
import com.studyhub.dto.SettingUpsertDTO;
import com.studyhub.enums.SettingStatus;
import com.studyhub.model.Setting;
import com.studyhub.repository.SettingRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettingService {

    private final SettingRepository settingRepository;

    public List<SettingListItemDTO> getSettings(String typeName, String status, String keyword, String sortBy, String direction) {
        Specification<Setting> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (typeName != null && !typeName.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.join("type").get("name"), typeName));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), SettingStatus.valueOf(status)));
            }

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(root.get("value"), "")), pattern)
                ));
            }

            predicates.add(criteriaBuilder.isNotNull(root.get("type")));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = buildSort(sortBy, direction);

        return settingRepository.findAll(specification, sort)
                .stream()
                .map(this::toListItem)
                .toList();
    }

    public List<Setting> getActiveSettingTypes() {
        return settingRepository.findByTypeIsNullAndStatusOrderByNameAsc(SettingStatus.ACTIVE);
    }

    public List<String> getSettingTypes() {
        return settingRepository.findByTypeIsNullOrderByNameAsc()
                .stream()
                .map(Setting::getName)
                .toList();
    }

    public SettingUpsertDTO getSettingDetail(Long id) {
        Setting setting = settingRepository.findWithTypeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found."));

        if (setting.getType() == null) {
            throw new IllegalArgumentException("Setting not found.");
        }

        return new SettingUpsertDTO(
                setting.getName(),
                setting.getType().getId(),
                setting.getValue(),
                setting.getPriority(),
                setting.getDescription(),
                setting.getStatus()
        );
    }

    @Transactional
    public void createSetting(SettingUpsertDTO dto) {
        validateUniqueNameInType(dto.getTypeId(), dto.getName(), null);
        Setting type = getValidType(dto.getTypeId());

        Setting setting = Setting.builder()
                .name(dto.getName().trim())
                .type(type)
                .value(emptyToNull(dto.getValue()))
                .priority(dto.getPriority())
                .description(emptyToNull(dto.getDescription()))
                .status(dto.getStatus())
                .build();

        settingRepository.save(setting);
    }

    @Transactional
    public void updateSetting(Long id, SettingUpsertDTO dto) {
        Setting setting = settingRepository.findWithTypeById(id)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found."));

        if (setting.getType() == null) {
            throw new IllegalArgumentException("Cannot update setting type.");
        }

        validateUniqueNameInType(dto.getTypeId(), dto.getName(), id);
        Setting type = getValidType(dto.getTypeId());

        setting.setName(dto.getName().trim());
        setting.setType(type);
        setting.setValue(emptyToNull(dto.getValue()));
        setting.setPriority(dto.getPriority());
        setting.setDescription(emptyToNull(dto.getDescription()));
        setting.setStatus(dto.getStatus());

        settingRepository.save(setting);
    }

    @Transactional
    public void toggleStatus(Long id) {
        Setting setting = settingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found."));

        if (setting.getType() == null) {
            throw new IllegalArgumentException("Cannot change status of setting type.");
        }

        if (setting.getStatus() == SettingStatus.ACTIVE) {
            setting.setStatus(SettingStatus.INACTIVE);
        } else {
            setting.setStatus(SettingStatus.ACTIVE);
        }

        settingRepository.save(setting);
    }

    private Sort buildSort(String sortBy, String direction) {
        String field = switch (sortBy == null || sortBy.isBlank() ? "type" : sortBy) {
            case "id" -> "id";
            case "name" -> "name";
            case "type" -> "type.name";
            case "value" -> "value";
            case "priority" -> "priority";
            case "status" -> "status";
            default -> "type.name";
        };

        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(dir, field).and(Sort.by(Sort.Direction.ASC, "id"));
    }

    private void validateUniqueNameInType(Long typeId, String name, Long currentId) {
        String normalizedName = name == null ? "" : name.trim();
        boolean exists = currentId == null
                ? settingRepository.existsByType_IdAndNameIgnoreCase(typeId, normalizedName)
                : settingRepository.existsByType_IdAndNameIgnoreCaseAndIdNot(typeId, normalizedName, currentId);
        if (exists) {
            throw new IllegalArgumentException("Name already exists in selected type.");
        }
    }

    private Setting getValidType(Long typeId) {
        Setting type = settingRepository.findById(typeId)
                .orElseThrow(() -> new IllegalArgumentException("Type not found."));
        if (type.getType() != null || type.getStatus() != SettingStatus.ACTIVE) {
            throw new IllegalArgumentException("Type must be an active root setting.");
        }
        return type;
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private SettingListItemDTO toListItem(Setting setting) {
        return new SettingListItemDTO(
                setting.getId(),
                setting.getName(),
                setting.getType() != null ? setting.getType().getName() : "",
                setting.getValue(),
                setting.getPriority(),
                setting.getStatus()
        );
    }
}
