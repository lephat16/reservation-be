package com.example.ReservationApp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.ReservationApp.dto.supplier.SupplierProductPriceHistoryDTO;
import com.example.ReservationApp.entity.supplier.SupplierProductPriceHistory;

@Mapper(componentModel = "spring")
public interface SupplierProductPriceHistoryMapper {
    
    @Mapping(target = "supplierProductId", source = "supplierProduct.id")
    SupplierProductPriceHistoryDTO toDTO(SupplierProductPriceHistory supplierProductPriceHistory);

    List<SupplierProductPriceHistoryDTO> toDTOList(List<SupplierProductPriceHistory> supplierProductPriceHistories);

    @Mapping(target = "supplierProduct", ignore = true)
    SupplierProductPriceHistory toEntity(SupplierProductPriceHistoryDTO supplierProductPriceHistoryDTO);
}
