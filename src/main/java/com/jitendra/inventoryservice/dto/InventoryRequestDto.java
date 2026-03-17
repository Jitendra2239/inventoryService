package com.jitendra.inventoryservice.dto;

import lombok.Data;

@Data
public class InventoryRequestDto {

    private Long productId;
    private Integer quantity;

    // getters setters
}