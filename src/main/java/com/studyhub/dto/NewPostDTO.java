package com.studyhub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewPostDTO {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;
    private String category;
    private String author;
    @NotNull(message = "Hãy tích chọn trạng thái")
    private String status;
    private String thumbnail;
    private String content;
    private boolean featured;
}
