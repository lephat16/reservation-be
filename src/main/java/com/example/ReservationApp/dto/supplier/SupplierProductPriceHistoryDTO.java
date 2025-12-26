package com.example.ReservationApp.dto.supplier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierProductPriceHistoryDTO {

    private Long id;
    private Long supplierProductId;
    private BigDecimal price;
    private LocalDate effectiveDate;
    private String note;
    private LocalDateTime createdAt;
}
