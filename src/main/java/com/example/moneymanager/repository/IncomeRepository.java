package com.example.moneymanager.repository;

import com.example.moneymanager.entity.Expense;
import com.example.moneymanager.entity.Income;
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
public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByUserIdOrderByDateDesc(Long userId);

    Optional<Income> findByIdAndUserId(Long id, Long userId);

    List<Income> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Income> findTop5ByUserIdOrderByDateDesc(Long userId);

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.user.id = :userId")
    BigDecimal findTotalExpenseByUserId(@Param("userId") Long userId);

    List<Income> findByUserIdAndDateBetweenAndNameContainingIgnoreCase(Long userId, LocalDate startDate, LocalDate endDate, String keyword, Sort sort);
}

