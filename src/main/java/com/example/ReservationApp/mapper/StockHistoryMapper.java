package com.example.ReservationApp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.ReservationApp.dto.response.inventory.StockHistoryDTO;
import com.example.ReservationApp.entity.inventory.StockHistory;

@Mapper(componentModel = "spring")
public interface StockHistoryMapper {

    @Mapping(target = "inventoryStockId", source = "inventoryStock.id")
    StockHistoryDTO toDTO(StockHistory history);

    List<StockHistoryDTO> toDTOList(List<StockHistory> histories);

    @Mapping(target = "inventoryStock", ignore = true)
    StockHistory toEntity(StockHistoryDTO dto);
}
