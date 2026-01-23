package com.example.ReservationApp.dto.response.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryInventorySalesOverviewDTO {

    private Long categoryId; 
    private String categoryName; 
    private Long productCount; // 製品数
    private Double percentage; // 割合
    private Long supplierCount; // 仕入先数
    private String supplierNames; // 仕入先名（カンマ区切り）
    private Long totalQuantity; // 在庫数量
    private Double totalPrice; // 販売金額
}
