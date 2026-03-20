package com.example.moneymanager.controller;

import com.example.moneymanager.dto.request.CategoryRequestDTO;
import com.example.moneymanager.dto.response.CategoryResponseDTO;
import com.example.moneymanager.helper.ApiResponse;
import com.example.moneymanager.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> saveCategory(@Valid @RequestBody CategoryRequestDTO categoryDTO) {
        CategoryResponseDTO category = this.categoryService.saveCategory(categoryDTO);
        return ApiResponse.created(category);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getCategories() {
        List<CategoryResponseDTO> categories = categoryService.getCategoriesForCurrentUser();
        return ApiResponse.success(categories);
    }

    @GetMapping("/{type}")
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getCategoriesByTypeForCurrentUser(@PathVariable String type) {
        List<CategoryResponseDTO> categories = categoryService.getCategoriesByTypeForCurrentUser(type);
        return ApiResponse.success(categories);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> updateCategory(@PathVariable Long categoryId, @RequestBody CategoryRequestDTO categoryDTO) {
        CategoryResponseDTO updatedCategory = categoryService.updateCategory(categoryId, categoryDTO);
        return ApiResponse.success(updatedCategory,"Update category successfully");
    }
}
