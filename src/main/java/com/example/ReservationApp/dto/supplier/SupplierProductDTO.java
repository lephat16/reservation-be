package com.example.ReservationApp.dto.supplier;

import java.math.BigDecimal;
import java.util.List;

import com.example.ReservationApp.enums.SupplierProductStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class SupplierProductDTO {
    private Long id;
    private Long supplierId;
    private String supplierName;

    private Long productId;
    private String productName;

    @NotBlank(message = "SKUは必須です")
    @Size(min = 3, max = 20, message = "SKUは3文字以上、20文字以下である必要があります")
    @Pattern(regexp = "^[A-Z0-9\\-]+$", message = "SKUは英大文字、数字、ハイフンのみ使用可能です")
    private String supplierSku;

    @NotNull(message = "現在価格は必須です")
    @DecimalMin(value = "0.0", inclusive = true, message = "現在価格は0以上である必要があります")
    private BigDecimal currentPrice;

    @NotNull(message = "リードタイムは必須です")
    @Min(value = 0, message = "リードタイムは0以上である必要があります")
    private Integer leadTime;

    @NotNull(message = "ステータスは必須です")
    private SupplierProductStatus status;

    // 商品価格変更時に入力されるコメント (最大50文字)
    private String note;

    private List<SupplierProductPriceHistoryDTO> priceHistories;
}
