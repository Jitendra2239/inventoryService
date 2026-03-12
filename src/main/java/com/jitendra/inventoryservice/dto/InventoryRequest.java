package com.jitendra.inventoryservice.dto;

import lombok.Data;

@Data
public class InventoryRequest {

    private Long productId;
    private Integer quantity;

    // getters setters
}