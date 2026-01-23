package com.example.ReservationApp.service.product;

import java.util.List;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.product.CategoryDTO;
import com.example.ReservationApp.dto.response.product.CategoryInventorySalesOverviewDTO;
import com.example.ReservationApp.dto.response.product.CategorySummariesDTO;
import com.example.ReservationApp.dto.response.product.CategorySummaryDTO;

public interface CategoryService {

    ResponseDTO<CategoryDTO> createCategory(CategoryDTO categoryDTO);

    ResponseDTO<List<CategoryDTO>> getAllCategories();

    ResponseDTO<CategoryDTO> getCategoryById(Long id);

    ResponseDTO<CategoryDTO> getCategoryByName(String name);

    ResponseDTO<CategoryDTO> updateCategory(Long id, CategoryDTO categoryDTO);

    ResponseDTO<Void> deleteCategory(Long id);

    ResponseDTO<List<CategoryDTO>> getActiveCategories();

    ResponseDTO<List<CategorySummariesDTO>> getAllCategorySummaries();

    ResponseDTO<CategorySummaryDTO> getCategorySummariesById(Long categoryId);

    ResponseDTO<CategoryInventorySalesOverviewDTO> getCategorySalesAndInventoryOverviewById(Long categoryId);
    
}
