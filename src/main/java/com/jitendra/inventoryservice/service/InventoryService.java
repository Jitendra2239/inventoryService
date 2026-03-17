package com.jitendra.inventoryservice.service;


import com.jitendra.inventoryservice.model.Inventory;

public interface InventoryService {

    Inventory createInventory(Long productId, Integer quantity);

    Inventory getInventory(Long productId);

    boolean checkStock(Long productId, Integer quantity);

    Inventory reduceStock(Long productId, Integer quantity);
    Inventory addStock(Long productId, Integer quantity);
}