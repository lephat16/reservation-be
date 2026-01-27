package com.example.ReservationApp.dto.supplier;

import java.util.List;

import com.example.ReservationApp.enums.SupplierStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(max = 100, message = "名前は100文字以内で入力してください。")
    private String name;
    @NotBlank(message = "連絡先情報は必須です")
    @Size(max = 50, message = "連絡先情報は50文字以内で入力してください。")
    @Pattern(regexp = "^[0-9\\-+()\\s]+$", message = "連絡先情報の形式が正しくありません。")
    private String contactInfo;
    @NotBlank(message = "メールは必須です")
    @Size(max = 255, message = "メールは255文字以内で入力してください。")
    @jakarta.validation.constraints.Email(message = "メールアドレスの形式が正しくありません。")
    private String mail;
    @NotBlank(message = "住所は必須です")
    @Size(min = 10, max = 255, message = "住所は10文字以上255文字以内で入力してください。")
    @Pattern(regexp = ".*\\D.*", message = "住所は数字のみでは入力できません。")
    private String address;
    @NotNull(message = "ステータスは必須です")
    private SupplierStatus supplierStatus;
    private List<String> categoryNames;
}
