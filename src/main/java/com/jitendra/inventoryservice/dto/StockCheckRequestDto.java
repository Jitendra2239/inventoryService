package com.jitendra.inventoryservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockCheckRequestDto {

    private String productId;
    private Integer quantity;
}