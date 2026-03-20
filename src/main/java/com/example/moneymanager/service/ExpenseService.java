package com.example.moneymanager.service;

import com.example.moneymanager.dto.request.ExpenseRequestDTO;
import com.example.moneymanager.dto.response.ExpenseResponseDTO;
import com.example.moneymanager.entity.Category;
import com.example.moneymanager.entity.Expense;
import com.example.moneymanager.entity.User;
import com.example.moneymanager.helper.exception.ResourceNotFoundException;
import com.example.moneymanager.repository.CategoryRepository;
import com.example.moneymanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final UserService userService;

    private ExpenseResponseDTO toDTO(Expense expense) {
        Category c = expense.getCategory();
        ExpenseResponseDTO.OutputCategory outCategory = new ExpenseResponseDTO.OutputCategory();
        outCategory.setName(c != null ? c.getName() : null);
        outCategory.setId(c != null ? c.getId() : null);

        return ExpenseResponseDTO.builder()
                .id(expense.getId())
                .name(expense.getName())
                .icon(expense.getIcon())
                .date(expense.getDate())
                .amount(expense.getAmount())
                .category(outCategory)
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }

    private Expense toEntity(ExpenseRequestDTO dto, Category category, User userRef) {
        return Expense.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .date(dto.getDate())
                .amount(dto.getAmount())
                .category(category)
                .user(userRef)
                .build();
    }

    private Category requireCategoryOfCurrentUser(Long categoryId) {
        Long userId = this.userService.getCurrentUser().getId();
        return this.categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or not accessible."));
    }

    public ExpenseResponseDTO addExpense(ExpenseRequestDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category not found or not accessible."));

        User userRef = this.userService.getCurrentUser();

        Expense expense = toEntity(dto, category, userRef);
        expense = this.expenseRepository.save(expense);
        return toDTO(expense);
    }

    public List<ExpenseResponseDTO> getCurrentMonthExpensesForCurrentUser() {
        User user = this.userService.getCurrentUser();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<Expense> list = expenseRepository.findByUserIdAndDateBetween(user.getId(), startDate, endDate);
        return list.stream().map(this::toDTO).toList();

    }

    public List<ExpenseResponseDTO> getLastest5ExpensesForCurrentUser() {
        User user = this.userService.getCurrentUser();
        List<Expense> list = this.expenseRepository.findTop5ByUserIdOrderByDateDesc(user.getId());
        return list.stream().map(this::toDTO).toList();
    }

    public BigDecimal getTotalExpensesForCurrentUser() {
        User user = this.userService.getCurrentUser();
        BigDecimal totalExpenses = this.expenseRepository.findTotalExpenseByUserId(user.getId());
        return totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
    }

    public List<ExpenseResponseDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        User user = userService.getCurrentUser();
        List<Expense> list = expenseRepository.findByUserIdAndDateBetweenAndNameContainingIgnoreCase(user.getId(), startDate, endDate, keyword, sort);
        return list.stream().map(this::toDTO).toList();
    }

    //Notifications
    public List<ExpenseResponseDTO> getExpensesForUserOnDate(Long userId, LocalDate date) {
        List<Expense> list = expenseRepository.findByUserIdAndDate(userId, date);
        return list.stream().map(this::toDTO).toList();
    }


    public ExpenseResponseDTO updateExpense(Long expenseId, ExpenseRequestDTO dto) {
        Long userId = this.userService.getCurrentUser().getId();
        Expense expense = this.expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found or not accessible."));

        if (dto.getCategoryId() != null) {
            Category category = requireCategoryOfCurrentUser(dto.getCategoryId());
            expense.setCategory(category);
        }
        expense.setName(dto.getName());
        expense.setIcon(dto.getIcon());
        expense.setDate(dto.getDate());
        expense.setAmount(dto.getAmount());

        expense = this.expenseRepository.save(expense);
        return toDTO(expense);
    }

    public void deleteExpense(Long expenseId) {
        Long userId = this.userService.getCurrentUser().getId();
        Expense expense = this.expenseRepository.findByIdAndUserId(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found or not accessible."));
        this.expenseRepository.deleteById(expense.getId());
    }
}

