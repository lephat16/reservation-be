package com.example.ReservationApp.dto.response.inventory;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryStockDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String warehouseName;
    private Integer quantity;
    private Integer reservedQuantity ;

    private List<StockHistoryDTO> stockHistories;
}
