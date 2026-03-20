package com.example.moneymanager.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FilterDTO {

    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String keyword;

    @JsonAlias("sort")
    private String sortField;
    private String sortOrder; //asc or desc
}
