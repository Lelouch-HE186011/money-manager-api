package com.example.moneymanager.service;

import com.example.moneymanager.dto.request.IncomeRequestDTO;
import com.example.moneymanager.dto.response.ExpenseResponseDTO;
import com.example.moneymanager.dto.response.IncomeResponseDTO;
import com.example.moneymanager.entity.Category;
import com.example.moneymanager.entity.Expense;
import com.example.moneymanager.entity.Income;
import com.example.moneymanager.entity.User;
import com.example.moneymanager.helper.exception.ResourceNotFoundException;
import com.example.moneymanager.repository.CategoryRepository;
import com.example.moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {
    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final UserService userService;

    private IncomeResponseDTO toDTO(Income income) {
        Category c = income.getCategory();
        IncomeResponseDTO.OutputCategory outCategory = new IncomeResponseDTO.OutputCategory();
        outCategory.setName(c != null ? c.getName() : null);
        outCategory.setId(c != null ? c.getId() : null);

        return IncomeResponseDTO.builder()
                .id(income.getId())
                .name(income.getName())
                .icon(income.getIcon())
                .date(income.getDate())
                .amount(income.getAmount())
                .category(outCategory)
                .createdAt(income.getCreatedAt())
                .updatedAt(income.getUpdatedAt())
                .build();
    }

    private Income toEntity(IncomeRequestDTO dto, Category category, User userRef) {
        return Income.builder()
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

    public IncomeResponseDTO addIncome(IncomeRequestDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category not found or not accessible."));

        User userRef = this.userService.getCurrentUser();

        Income income = toEntity(dto, category, userRef);
        income = this.incomeRepository.save(income);
        return toDTO(income);
    }

    public List<IncomeResponseDTO> getCurrentMonthIncomesForCurrentUser() {
        User user = this.userService.getCurrentUser();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<Income> list = incomeRepository.findByUserIdAndDateBetween(user.getId(), startDate, endDate);
        return list.stream().map(this::toDTO).toList();

    }

    public List<IncomeResponseDTO> getLastest5IncomesForCurrentUser() {
        User user = this.userService.getCurrentUser();
        List<Income> list = this.incomeRepository.findTop5ByUserIdOrderByDateDesc(user.getId());
        return list.stream().map(this::toDTO).toList();
    }

    public BigDecimal getTotalIncomesForCurrentUser() {
        User user = this.userService.getCurrentUser();
        BigDecimal total = this.incomeRepository.findTotalExpenseByUserId(user.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<IncomeResponseDTO> filterIncomes(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        User user = userService.getCurrentUser();
        List<Income> list = this.incomeRepository.findByUserIdAndDateBetweenAndNameContainingIgnoreCase(user.getId(), startDate, endDate, keyword, sort);
        return list.stream().map(this::toDTO).toList();
    }


    public IncomeResponseDTO updateIncome(Long incomeId, IncomeRequestDTO dto) {
        Long userId = this.userService.getCurrentUser().getId();
        Income income = this.incomeRepository.findByIdAndUserId(incomeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found or not accessible."));

        if (dto.getCategoryId() != null) {
            Category category = requireCategoryOfCurrentUser(dto.getCategoryId());
            income.setCategory(category);
        }
        income.setName(dto.getName());
        income.setIcon(dto.getIcon());
        income.setDate(dto.getDate());
        income.setAmount(dto.getAmount());

        income = this.incomeRepository.save(income);
        return toDTO(income);
    }

    public void deleteIncome(Long incomeId) {
        Long userId = this.userService.getCurrentUser().getId();
        Income income = this.incomeRepository.findByIdAndUserId(incomeId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found or not accessible."));
        this.incomeRepository.deleteById(income.getId());
    }
}

