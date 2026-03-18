package com.studyhub.dto;

import com.studyhub.enums.SettingStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettingUpsertDTO {

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "^\\D+$")
    private String name;

    @NotNull
    private Long typeId;

    @Size(max = 100)
    private String value;

    @NotNull
    @Positive
    private Integer priority;

    @Size(max = 200)
    private String description;

    @NotNull
    private SettingStatus status;
}
