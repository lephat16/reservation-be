package com.example.ReservationApp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.ReservationApp.dto.response.inventory.InventoryStockDTO;
import com.example.ReservationApp.entity.inventory.InventoryStock;

@Mapper(componentModel = "spring")
public interface InventoryStockMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "sku", source = "supplierProduct.supplierSku")
    @Mapping(target = "warehouseName", source = "warehouse.name")
    @Mapping(target = "stockHistories", ignore = true) // handle separately
    @Mapping(target = "product", ignore = true) // handle separately
    @Mapping(target = "supplierProduct", ignore = true) // handle separately
    InventoryStockDTO toDTO(InventoryStock stock);

    List<InventoryStockDTO> toDTOList(List<InventoryStock> stocks);

    @Mapping(target = "warehouse", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "supplierProduct", ignore = true)
    @Mapping(target = "stockHistories", ignore = true)
    @Mapping(target = "virtual", ignore = true)
    InventoryStock toEntity(InventoryStockDTO dto);
}
