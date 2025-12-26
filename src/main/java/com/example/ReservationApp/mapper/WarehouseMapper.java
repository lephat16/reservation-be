package com.example.ReservationApp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.ReservationApp.dto.response.inventory.WarehouseDTO;
import com.example.ReservationApp.entity.inventory.Warehouse;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {

    @Mapping(target = "stocks", ignore = true) 
    WarehouseDTO toDTO(Warehouse warehouse);

    List<WarehouseDTO> toDTOList(List<Warehouse> warehouses);

    @Mapping(target = "inventoryStocks", ignore = true)
    Warehouse toEntity(WarehouseDTO dto);
}
