package com.studyhub.dto;

import com.studyhub.enums.SettingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettingListItemDTO {
    private Long id;
    private String name;
    private String typeName;
    private String value;
    private Integer priority;
    private SettingStatus status;
}
