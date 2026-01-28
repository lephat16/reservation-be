package com.example.ReservationApp.controller.product;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.product.CategoryDTO;
import com.example.ReservationApp.dto.response.product.CategorySummariesDTO;
import com.example.ReservationApp.dto.response.product.CategorySummaryDTO;
import com.example.ReservationApp.service.product.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/add")
    public ResponseEntity<ResponseDTO<CategoryDTO>> createCategory(
            @RequestPart("category") @Valid CategoryDTO categoryDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        return ResponseEntity.ok(categoryService.createCategory(categoryDTO, file));
    }

    @GetMapping("/all")
    public ResponseEntity<ResponseDTO<List<CategoryDTO>>> getAllCategories() {

        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<CategoryDTO>> getCategoryById(@PathVariable Long id) {

        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ResponseDTO<CategoryDTO>> updateCategory(
            @PathVariable Long id,
            @RequestPart("category") @Valid CategoryDTO categoryDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        return ResponseEntity.ok(categoryService.updateCategory(id, categoryDTO, file));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ResponseDTO<Void>> deleteCategory(@PathVariable Long id) {

        return ResponseEntity.ok(categoryService.deleteCategory(id));
    }

    @GetMapping("/active")
    public ResponseEntity<ResponseDTO<List<CategoryDTO>>> getActiveCategories() {

        return ResponseEntity.ok(categoryService.getActiveCategories());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ResponseDTO<CategoryDTO>> getCategoryByName(@PathVariable String name) {

        return ResponseEntity.ok(categoryService.getCategoryByName(name));
    }

    @GetMapping("/summaries")
    public ResponseEntity<ResponseDTO<List<CategorySummariesDTO>>> getAllCategorySummaries() {

        return ResponseEntity.ok(categoryService.getAllCategorySummaries());
    }

    @GetMapping("/summaries/{categoryId}")
    public ResponseEntity<ResponseDTO<CategorySummaryDTO>> getCategorySummariesById(@PathVariable Long categoryId) {

        return ResponseEntity.ok(categoryService.getCategorySummariesById(categoryId));
    }
}
