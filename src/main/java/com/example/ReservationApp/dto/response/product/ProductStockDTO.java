package com.example.ReservationApp.dto.response.product;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductStockDTO {
    private String productName;
    private List<SupplierPriceDTO> suppliers;
    private List<StockDTO> stocks;
}
