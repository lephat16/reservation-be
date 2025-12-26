package com.example.ReservationApp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.ReservationApp.dto.response.product.CategoryDTO;
import com.example.ReservationApp.entity.product.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "productCount", ignore = true) // handle in service
    @Mapping(target = "supplierNames", ignore = true) // handle in service
    CategoryDTO toDTO(Category category);

    List<CategoryDTO> toDTOList(List<Category> categories);

    @Mapping(target = "status", ignore = true)
    Category toEntity(CategoryDTO dto);
}
