package com.aues.library.service;

import com.aues.library.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    Category createCategory(Category category);
    Category getCategoryById(Long id);
    List<Category> getAllCategories();
    Category updateCategory(Long id, Category updatedCategory);
    void deleteCategory(Long id);
    List<Category> searchCategories(Optional<String> name);
}
