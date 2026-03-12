package com.jitendra.inventoryservice.service;


import com.jitendra.inventoryservice.model.Inventory;

public interface InventoryService {

    Inventory getInventory(Long productId);

    void reserveInventory(Long productId, Integer quantity);

    void releaseInventory(Long productId, Integer quantity);

    void deductInventory(Long productId, Integer quantity);

}