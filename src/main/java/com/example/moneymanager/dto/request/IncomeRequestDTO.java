package com.example.moneymanager.dto.request;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeRequestDTO {
    private String name;
    private String icon;
    private LocalDate date;
    private BigDecimal amount;
    private Long categoryId;
}

