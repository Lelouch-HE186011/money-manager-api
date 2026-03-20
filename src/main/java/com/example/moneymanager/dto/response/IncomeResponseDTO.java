package com.example.moneymanager.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeResponseDTO {
    private Long id;
    private String name;
    private String icon;
    private LocalDate date;
    private BigDecimal amount;
    private OutputCategory category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutputCategory {
        private Long id;
        private String name;
    }
}

