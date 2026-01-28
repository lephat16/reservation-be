package com.example.ReservationApp.service.product;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.product.CategoryDTO;
import com.example.ReservationApp.dto.response.product.CategoryInventorySalesOverviewDTO;
import com.example.ReservationApp.dto.response.product.CategorySummariesDTO;
import com.example.ReservationApp.dto.response.product.CategorySummaryDTO;

public interface CategoryService {

    ResponseDTO<CategoryDTO> createCategory(CategoryDTO categoryDTO, MultipartFile file);

    ResponseDTO<List<CategoryDTO>> getAllCategories();

    ResponseDTO<CategoryDTO> getCategoryById(Long id);

    ResponseDTO<CategoryDTO> getCategoryByName(String name);

    ResponseDTO<CategoryDTO> updateCategory(Long id, CategoryDTO categoryDTO, MultipartFile file);

    ResponseDTO<Void> deleteCategory(Long id);

    ResponseDTO<List<CategoryDTO>> getActiveCategories();

    ResponseDTO<List<CategorySummariesDTO>> getAllCategorySummaries();

    ResponseDTO<CategorySummaryDTO> getCategorySummariesById(Long categoryId);

    ResponseDTO<CategoryInventorySalesOverviewDTO> getCategorySalesAndInventoryOverviewById(Long categoryId);

}
