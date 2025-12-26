package com.example.ReservationApp.service.supplier;

import java.util.List;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.supplier.SupplierDTO;

public interface SupplierService {
    
    ResponseDTO<SupplierDTO> addSupplier(SupplierDTO supplierDTO);

    ResponseDTO<List<SupplierDTO>> getAllSuppliers();

    ResponseDTO<SupplierDTO> getSupplierById(Long id);

    ResponseDTO<SupplierDTO> updateSupplier(Long id, SupplierDTO supplierDTO);

    ResponseDTO<Void> deleteSupplier(Long id);
}
