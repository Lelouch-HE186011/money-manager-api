package com.example.moneymanager.repository;

import com.example.moneymanager.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserId(Long userId);

    Optional<Category> findByIdAndUserId(Long id, Long userId);

   List<Category> findByTypeAndUserId(String type, Long userId);

   Boolean existsByNameAndUserId(String name, Long userId);
}
