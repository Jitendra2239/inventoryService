package com.jitendra.inventoryservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponseDto {

    private String productId;
    private Integer quantity;
    private Boolean inStock;
}