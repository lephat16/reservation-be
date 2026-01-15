package com.example.ReservationApp.dto.response.product;



import com.example.ReservationApp.enums.ProductStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品情報を保持するためのデータ転送オブジェクト（DTO）クラス。
 * このクラスは、商品に関する基本情報（名前、価格、在庫数量など）を
 * クライアントとサーバー間でやり取りする際に使用。
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDTO {

    private Long id;
    @NotBlank(message = "商品名は必須です")
    private String name;
    private String productCode;
    private String description;
    private String unit;
    private ProductStatus status;

    @NotBlank(message = "カテゴリー名は必須です")
    private String categoryName; // flatten from Product -> Category
    private Integer totalStock;

}
