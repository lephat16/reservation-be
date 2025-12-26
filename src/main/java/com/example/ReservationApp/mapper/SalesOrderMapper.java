package com.example.ReservationApp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.ReservationApp.dto.transaction.SalesOrderDTO;
import com.example.ReservationApp.entity.transaction.SalesOrder;

@Mapper(componentModel = "spring")
public interface SalesOrderMapper {

    @Mapping(target = "details", ignore = true)
    @Mapping(target = "userId", source = "createdBy.id")
    @Mapping(target = "userName", source = "createdBy.name")
    SalesOrderDTO toDTO(SalesOrder salesOrder);

    List<SalesOrderDTO> toDTOList(List<SalesOrder> orders);

    @Mapping(target = "details", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    SalesOrder toEntity(SalesOrderDTO salesOrderDTO);
}
