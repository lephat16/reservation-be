package com.example.ReservationApp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.ReservationApp.dto.supplier.SupplierProductDTO;
import com.example.ReservationApp.entity.supplier.SupplierProduct;

@Mapper(componentModel = "spring")
public interface SupplierProductMapper {
    
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "priceHistories", ignore = true) 
    SupplierProductDTO toDTO(SupplierProduct supplierProduct);

    List<SupplierProductDTO> toDTOList(List<SupplierProduct> supplierProducts);

    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "priceHistories", ignore = true)
    SupplierProduct toEntity(SupplierProductDTO spDTO);
}
