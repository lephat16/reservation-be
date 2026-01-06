package com.example.ReservationApp.service.supplier;

import java.util.List;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.response.supplier.CategoryProductsDTO;
import com.example.ReservationApp.dto.supplier.SupplierProductDTO;
import com.example.ReservationApp.dto.supplier.SupplierProductPriceHistoryDTO;

public interface SupplierProductService {

    ResponseDTO<SupplierProductDTO> createSupplierProduct(Long supplierId, Long productId, SupplierProductDTO spDTO);

    ResponseDTO<List<SupplierProductDTO>> getProductsBySupplier(Long supplierId);

    ResponseDTO<SupplierProductDTO> updateSupplierProduct(Long spId, SupplierProductDTO spDTO);

    ResponseDTO<Void> deleteSupplierProduct(Long spId);

    ResponseDTO<List<SupplierProductPriceHistoryDTO>> getPriceHistory();

    ResponseDTO<List<CategoryProductsDTO>> getSupplierProductsWithStock(Long supplierId);
    
    ResponseDTO<List<CategoryProductsDTO>> getSupplierProductsWithLeadTime(Long supplierId);
}
