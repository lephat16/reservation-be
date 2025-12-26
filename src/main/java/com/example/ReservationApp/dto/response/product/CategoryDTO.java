package com.example.ReservationApp.dto.response.product;

import java.time.LocalDateTime;
import java.util.List;

import com.example.ReservationApp.enums.CategoryStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoryDTO {

    private Long id;
    @NotBlank(message = "カテゴリー名は必須です")
    private String name;

    private String description;
    private String imageUrl;
    private CategoryStatus status; // ACTIVE / INACTIVE
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long productCount;
    private List<String> supplierNames;
}
