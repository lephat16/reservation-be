package com.example.ReservationApp.dto.supplier;

import java.math.BigDecimal;
import java.util.List;

import com.example.ReservationApp.enums.SupplierProductStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierProductDTO {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private Long productId;
    private String productName;

    private String supplierSku;
    private BigDecimal currentPrice;
    private Integer leadTime;
    private SupplierProductStatus status;

    private List<SupplierProductPriceHistoryDTO> priceHistories;
}
