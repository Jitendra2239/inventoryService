package com.jitendra.inventoryservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockCheckResponseDto {

    private String productId;
    private Boolean available;
}