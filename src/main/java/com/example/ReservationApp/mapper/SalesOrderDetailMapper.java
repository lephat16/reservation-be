package com.example.ReservationApp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.ReservationApp.dto.transaction.SalesOrderDetailDTO;
import com.example.ReservationApp.entity.transaction.SalesOrderDetail;

@Mapper(componentModel = "spring")
public interface SalesOrderDetailMapper {

    @Mapping(target = "salesOrderId", source = "salesOrder.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "sku", source = "supplierProduct.supplierSku")
    SalesOrderDetailDTO toDTO(SalesOrderDetail salesOrderDetail);

    List<SalesOrderDetailDTO> toDTOList(List<SalesOrderDetail> details);

    @Mapping(target = "salesOrder", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "supplierProduct", ignore = true)
    SalesOrderDetail toEntity(SalesOrderDetailDTO salesOrderDetailDTO);
}
