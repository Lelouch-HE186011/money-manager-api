package com.example.moneymanager.controller;

import com.example.moneymanager.dto.FilterDTO;
import com.example.moneymanager.dto.response.ExpenseResponseDTO;
import com.example.moneymanager.dto.response.IncomeResponseDTO;
import com.example.moneymanager.helper.ApiResponse;
import com.example.moneymanager.repository.IncomeRepository;
import com.example.moneymanager.service.ExpenseService;
import com.example.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {
    private final ExpenseService  expenseService;
    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<ApiResponse<List<?>>> filterTransactions(@RequestBody FilterDTO filter) {
        LocalDate startDate = filter.getStartDate() != null ? filter.getStartDate() : LocalDate.MIN;
        LocalDate endDate = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();
        String keyword = filter.getKeyword() != null ? filter.getKeyword() : "";
        String sortField = filter.getSortField() != null ? filter.getSortField() : "date";
        Sort.Direction direction = "desc".equalsIgnoreCase(filter.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortField);
        if("income".equals(filter.getType())) {
            List<IncomeResponseDTO> incomes = incomeService.filterIncomes(startDate, endDate, keyword, sort);
            return ApiResponse.success(incomes);
        } else if("expense".equals(filter.getType())) {
            List<ExpenseResponseDTO> expenses = expenseService.filterExpenses(startDate, endDate, keyword, sort);
            return ApiResponse.success(expenses);
        } else {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "Invalid type. Must be 'income' or 'expense'");
        }
    }
}
