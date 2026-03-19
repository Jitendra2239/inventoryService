package com.jitendra.inventoryservice.service;


import com.jitendra.event.OrderItemDto;
import com.jitendra.inventoryservice.model.Inventory;

import java.util.List;

public interface InventoryService {

    Inventory createInventory(Long productId, Integer quantity);

    Inventory getInventory(Long productId);

    boolean checkStock(List<OrderItemDto> orderItemDtos);

    Inventory reduceStock(Long productId, Integer quantity);
    Inventory addStock(Long productId, Integer quantity);
}