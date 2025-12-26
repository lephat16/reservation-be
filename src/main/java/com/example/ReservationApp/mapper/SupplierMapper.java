package com.example.ReservationApp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.ReservationApp.dto.supplier.SupplierDTO;
import com.example.ReservationApp.entity.supplier.Supplier;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    @Mapping(target = "categoryNames", expression = "java(supplier.getSupplierProducts().stream().map(sp -> sp.getProduct().getCategory().getName()).distinct().toList())")
    @Mapping(target = "supplierStatus", source = "status")
    SupplierDTO toDTO(Supplier supplier);

    List<SupplierDTO> toDTOList(List<Supplier> suppliers);

    @Mapping(target = "supplierProducts", ignore = true)
    @Mapping(target = "status", source = "supplierStatus")
    Supplier toEntity(SupplierDTO supplierDTO);
}
