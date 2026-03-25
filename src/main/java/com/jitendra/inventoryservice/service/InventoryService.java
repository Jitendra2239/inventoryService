package com.jitendra.inventoryservice.service;

import com.jitendra.inventoryservice.dto.*;

import java.util.List;

public interface InventoryService {

    InventoryResponseDto createInventory(InventoryRequestDto dto);

    InventoryResponseDto getInventory(String productId);

    boolean checkStock(List<StockCheckRequestDto> items);

    InventoryResponseDto reduceStock(String productId, Integer quantity);

    InventoryResponseDto addStock(String productId, Integer quantity);
}