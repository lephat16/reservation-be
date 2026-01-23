package com.example.ReservationApp.dto.supplier;

import java.util.List;

import com.example.ReservationApp.enums.SupplierStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仕入先情報を保持するDTOクラス。
 * 
 * このクラスは仕入先（仕入先）のID、名前、連絡先、住所などの情報を
 * データ転送用にカプセル化。
 * 
 * Builderパターンを使用してインスタンスを作成可能。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class SupplierDTO {

    private Long id;
    @NotBlank(message = "名前は必須です")
    private String name;
    @NotBlank(message = "連絡先情報は必須です")
    private String contactInfo;
    @NotBlank(message = "メールは必須です")
    private String mail;
    @NotBlank(message = "住所は必須です")
    private String address;
    @NotNull(message = "ステータスは必須です")
    private SupplierStatus supplierStatus;
    private List<String> categoryNames;
}
