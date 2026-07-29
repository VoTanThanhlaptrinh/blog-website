package com.blog.be.content.application;

import com.blog.be.content.api.dto.CategoryResponse;
import com.blog.be.content.api.dto.CreateCategoryRequest;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();
    CategoryResponse getCategoryById(Long id);
    CategoryResponse createCategory(CreateCategoryRequest request);
}
