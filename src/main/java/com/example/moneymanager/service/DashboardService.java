package com.example.moneymanager.service;

import com.example.moneymanager.dto.RecentTransactionDTO;
import com.example.moneymanager.dto.response.ExpenseResponseDTO;
import com.example.moneymanager.dto.response.IncomeResponseDTO;
import com.example.moneymanager.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final UserService userService;

    public Map<String, Object> getDashboardData() {
        User user = this.userService.getCurrentUser();
        Map<String, Object> returnValue = new LinkedHashMap<>();
        List<IncomeResponseDTO> lastestIncomes = this.incomeService.getLastest5IncomesForCurrentUser();
        List<ExpenseResponseDTO> lastestExpenses = this.expenseService.getLastest5ExpensesForCurrentUser();
        List<RecentTransactionDTO> recentTransactions = Stream.concat(
                lastestIncomes.stream().map(income ->
                RecentTransactionDTO.builder()
                        .id(income.getId())
                        .userId(user.getId())
                        .icon(income.getIcon())
                        .name(income.getName())
                        .amount(income.getAmount())
                        .date(income.getDate())
                        .createdAt(income.getCreatedAt())
                        .updatedAt(income.getUpdatedAt())
                        .type("income")
                        .build()),
                lastestExpenses.stream().map(expense ->
                        RecentTransactionDTO.builder()
                                .id(expense.getId())
                                .userId(user.getId())
                                .icon(expense.getIcon())
                                .name(expense.getName())
                                .amount(expense.getAmount())
                                .date(expense.getDate())
                                .createdAt(expense.getCreatedAt())
                                .updatedAt(expense.getUpdatedAt())
                                .type("expense")
                                .build()))
                .sorted((a,b) -> {
                    int cmp = b.getDate().compareTo(a.getDate());
                    if (cmp == 0 && a.getCreatedAt() != null && b.getCreatedAt() != null) {
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    }
                    return cmp;
                }).collect(Collectors.toList());
        returnValue.put("totalBalance", incomeService.getTotalIncomesForCurrentUser().subtract(expenseService.getTotalExpensesForCurrentUser()));
        returnValue.put("totalIncomes", incomeService.getTotalIncomesForCurrentUser());
        returnValue.put("totalExpenses", expenseService.getTotalExpensesForCurrentUser());
        returnValue.put("recent5Expenses", lastestExpenses);
        returnValue.put("recent5Incomes", lastestIncomes);
        returnValue.put("recentTransactions", recentTransactions);
        return returnValue;

    }
}
