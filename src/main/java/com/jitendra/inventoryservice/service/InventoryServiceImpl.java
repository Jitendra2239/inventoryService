package com.jitendra.inventoryservice.service;

import com.jitendra.inventoryservice.exception.ResourceNotFoundException;
import com.jitendra.inventoryservice.model.Inventory;
import com.jitendra.inventoryservice.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    public Inventory getInventory(Long productId) {

        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inventory not found"));
    }

    @Override
    public void reserveInventory(Long productId, Integer quantity) {

        Inventory inventory = getInventory(productId);

        if (inventory.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Not enough inventory");
        }

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() - quantity);

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() + quantity);

        inventoryRepository.save(inventory);
    }

    @Override
    public void releaseInventory(Long productId, Integer quantity) {

        Inventory inventory = getInventory(productId);

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() + quantity);

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() - quantity);

        inventoryRepository.save(inventory);
    }

    @Override
    public void deductInventory(Long productId, Integer quantity) {

        Inventory inventory = getInventory(productId);

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() - quantity);

        inventoryRepository.save(inventory);
    }
}