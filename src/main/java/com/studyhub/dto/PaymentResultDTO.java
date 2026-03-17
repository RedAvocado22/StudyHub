package com.studyhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResultDTO {
    private boolean success;
    private String redirectUrl;
    private Long enrollmentId;
}
