package com.example.ReservationApp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.ReservationApp.dto.response.product.ProductDTO;
import com.example.ReservationApp.entity.product.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "totalStock", ignore = true) //handle in Service
    ProductDTO toDTO(Product product);

        
    List<ProductDTO> toDTOList(List<Product> products);

    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductDTO productDTO);
}
