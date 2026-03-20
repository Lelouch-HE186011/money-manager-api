package com.example.moneymanager.controller;

import com.example.moneymanager.dto.request.ExpenseRequestDTO;
import com.example.moneymanager.dto.response.ExpenseResponseDTO;
import com.example.moneymanager.helper.ApiResponse;
import com.example.moneymanager.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponseDTO>> addExpense(@RequestBody ExpenseRequestDTO dto) {
        ExpenseResponseDTO created = this.expenseService.addExpense(dto);
        return ApiResponse.created(created);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseResponseDTO>>> getExpenses() {
        List<ExpenseResponseDTO> expenses = this.expenseService.getCurrentMonthExpensesForCurrentUser();
        return ApiResponse.success(expenses);
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseResponseDTO>> update(
            @PathVariable Long expenseId,
            @RequestBody ExpenseRequestDTO dto
    ) {
        ExpenseResponseDTO updated = this.expenseService.updateExpense(expenseId, dto);
        return ApiResponse.success(updated, "Update expense successfully");
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long expenseId) {
        this.expenseService.deleteExpense(expenseId);
        return ApiResponse.success("ok", "Delete expense successfully");
    }
}

