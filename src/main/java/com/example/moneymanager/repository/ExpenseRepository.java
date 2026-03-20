package com.example.moneymanager.repository;

import com.example.moneymanager.entity.Expense;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserIdOrderByDateDesc(Long userId);

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    List<Expense> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Expense> findTop5ByUserIdOrderByDateDesc(Long userId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId")
    BigDecimal findTotalExpenseByUserId(@Param("userId") Long userId);

    List<Expense> findByUserIdAndDateBetweenAndNameContainingIgnoreCase(Long userId, LocalDate startDate, LocalDate endDate, String keyword, Sort sort);

    List<Expense> findByUserIdAndDate(Long userId, LocalDate date);

}

