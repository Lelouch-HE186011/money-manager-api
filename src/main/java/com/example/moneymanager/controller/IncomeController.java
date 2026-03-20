package com.example.moneymanager.controller;

import com.example.moneymanager.dto.request.IncomeRequestDTO;
import com.example.moneymanager.dto.response.IncomeResponseDTO;
import com.example.moneymanager.helper.ApiResponse;
import com.example.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/incomes")
public class IncomeController {
    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<ApiResponse<IncomeResponseDTO>> addIncome(@RequestBody IncomeRequestDTO dto) {
        IncomeResponseDTO created = this.incomeService.addIncome(dto);
        return ApiResponse.created(created);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<IncomeResponseDTO>>> listMine() {
        List<IncomeResponseDTO> incomes = this.incomeService.getCurrentMonthIncomesForCurrentUser();
        return ApiResponse.success(incomes);
    }

    @PutMapping("/{incomeId}")
    public ResponseEntity<ApiResponse<IncomeResponseDTO>> update(
            @PathVariable Long incomeId,
            @RequestBody IncomeRequestDTO dto
    ) {
        IncomeResponseDTO updated = this.incomeService.updateIncome(incomeId, dto);
        return ApiResponse.success(updated, "Update income successfully");
    }

    @DeleteMapping("/{incomeId}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long incomeId) {
        this.incomeService.deleteIncome(incomeId);
        return ApiResponse.success("ok", "Delete income successfully");
    }
}

