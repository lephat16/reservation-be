package com.example.ReservationApp.dto.response.inventory;

import java.time.LocalDateTime;
import java.util.List;

import com.example.ReservationApp.enums.WarehouseStatus;
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
public class WarehouseDTO {
    private Long id;
    private String name;
    private String location;
    private WarehouseStatus status;
    private Integer stockLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<InventoryStockDTO> stocks;

    public WarehouseDTO(Long id, String name, String location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }
}
