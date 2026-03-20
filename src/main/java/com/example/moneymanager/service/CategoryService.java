package com.example.moneymanager.service;

import com.example.moneymanager.dto.request.CategoryRequestDTO;
import com.example.moneymanager.dto.response.CategoryResponseDTO;
import com.example.moneymanager.entity.Category;
import com.example.moneymanager.entity.User;
import com.example.moneymanager.helper.exception.ResourceAlreadyExistsException;
import com.example.moneymanager.helper.exception.ResourceNotFoundException;
import com.example.moneymanager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    private Category toEntity(CategoryRequestDTO dto) {
        User user = new User();
        user.setId(this.userService.getCurrentUser().getId());

        return Category.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .type(dto.getType())
                .user(user)
                .build();
    }

    private CategoryResponseDTO toDTO(Category category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .icon(category.getIcon())
                .type(category.getType())
                .createdDate(category.getCreatedDate())
                .updatedDate(category.getUpdatedDate())
                .build();
    }

    public CategoryResponseDTO saveCategory(CategoryRequestDTO dto) {
        if(categoryRepository.existsByNameAndUserId(dto.getName(), this.userService.getCurrentUser().getId())) {
            throw new ResourceAlreadyExistsException("Category with this name already exists.");
        }

        Category newCategory = toEntity(dto);
        categoryRepository.save(newCategory);
        return toDTO(newCategory);
    }

    public List<CategoryResponseDTO> getCategoriesForCurrentUser() {
        User user = this.userService.getCurrentUser();
        List<Category> categories = categoryRepository.findByUserId(user.getId());
        return categories.stream().map(this::toDTO).toList();
    }

    public List<CategoryResponseDTO> getCategoriesByTypeForCurrentUser(String type) {
        User user = this.userService.getCurrentUser();
        List<Category> categories = this.categoryRepository.findByTypeAndUserId(type, user.getId());
        return categories.stream().map(this::toDTO).toList();
    }

    public CategoryResponseDTO updateCategory(Long categoryId, CategoryRequestDTO dto) {
        User user = this.userService.getCurrentUser();
        Category category = this.categoryRepository.findByIdAndUserId(categoryId, user.getId()).orElseThrow(() -> new ResourceNotFoundException("Category not found or not accessible."));
        category.setName(dto.getName());
        category.setIcon(dto.getIcon());
        category.setType(dto.getType());
        this.categoryRepository.save(category);
        return toDTO(category);

    }

}
