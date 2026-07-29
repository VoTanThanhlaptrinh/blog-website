package com.blog.backend.content.application;

import com.blog.backend.content.api.dto.CategoryResponse;
import com.blog.backend.content.api.dto.CreateCategoryRequest;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();
    CategoryResponse getCategoryById(Long id);
    CategoryResponse createCategory(CreateCategoryRequest request);
}
