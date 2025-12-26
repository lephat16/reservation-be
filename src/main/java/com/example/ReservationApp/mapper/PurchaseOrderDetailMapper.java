package com.example.ReservationApp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.ReservationApp.dto.transaction.PurchaseOrderDetailDTO;
import com.example.ReservationApp.entity.transaction.PurchaseOrderDetail;

@Mapper(componentModel = "spring")
public interface PurchaseOrderDetailMapper {
    
    @Mapping(target = "purchaseOrderId", source = "purchaseOrder.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "sku", ignore = true) 
    @Mapping(target = "orderedQty", ignore = true) 
    @Mapping(target = "receivedQty", ignore = true)
    @Mapping(target = "remainingQty", ignore = true)
    PurchaseOrderDetailDTO toDTO(PurchaseOrderDetail detail);

    List<PurchaseOrderDetailDTO> toDTOList(List<PurchaseOrderDetail> details);

    @Mapping(target = "purchaseOrder", ignore = true)   
    @Mapping(target = "product", ignore = true)         
    PurchaseOrderDetail toEntity(PurchaseOrderDetailDTO dto);

    List<PurchaseOrderDetail> toEntityList(List<PurchaseOrderDetailDTO> detailDTOs);
}
