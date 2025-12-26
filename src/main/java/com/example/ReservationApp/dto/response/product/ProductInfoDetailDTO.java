package com.example.ReservationApp.dto.response.product;

import java.util.List;

import com.example.ReservationApp.dto.response.inventory.InventoryStockDTO;
import com.example.ReservationApp.dto.response.inventory.StockHistoryDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductInfoDetailDTO {
    
    private ProductDTO productDTO;
    
    private List<SupplierPriceDTO> supplierPriceDTO;

    private List<StockHistoryDTO> stockHistoryDTO;

    private List<InventoryStockDTO> inventoryStockDTO;

}
