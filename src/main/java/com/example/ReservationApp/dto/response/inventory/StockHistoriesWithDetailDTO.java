package com.example.ReservationApp.dto.response.inventory;

import java.math.BigDecimal;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class StockHistoriesWithDetailDTO {

    private Long id;
    private Long inventoryStockId;

    private Integer changeQty;
    private String type;

    private String refType;
    private Long refId;

    private String notes;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createdAt;

    private String supplierSku;
    private String productName;
    private String unit;
    private String warehouseName;
    private String userName;
    private BigDecimal price;
    private String participantName;
    private Integer signedQty;
    private Long afterQty;
    private Long beforeQty;
}
