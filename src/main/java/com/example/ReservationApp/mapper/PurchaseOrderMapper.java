package com.example.ReservationApp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.ReservationApp.dto.transaction.PurchaseOrderDTO;
import com.example.ReservationApp.entity.transaction.PurchaseOrder;

@Mapper(componentModel = "spring")
public interface PurchaseOrderMapper {
    
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "userId", source = "createdBy.id")
    @Mapping(target = "userName", source = "createdBy.name")
    @Mapping(target = "details", ignore = true)
    PurchaseOrderDTO toDTO(PurchaseOrder purchaseOrder);

    List<PurchaseOrderDTO> toDTOList(List<PurchaseOrder> purchaseOrders);

    @Mapping(target = "supplier", ignore = true)  
    @Mapping(target = "details", ignore = true)   
    @Mapping(target = "createdBy", ignore = true)   
    PurchaseOrder toEntity(PurchaseOrderDTO dto);
}
