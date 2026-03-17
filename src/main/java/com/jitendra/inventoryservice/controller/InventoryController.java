package com.jitendra.inventoryservice.controller;


import com.jitendra.inventoryservice.dto.InventoryRequestDto;
import com.jitendra.inventoryservice.dto.UpdateStockRequestDto;
import com.jitendra.inventoryservice.model.Inventory;
import com.jitendra.inventoryservice.service.InventoryService;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // Create Inventory
    @PostMapping
    public Inventory createInventory(@RequestBody InventoryRequestDto requestDto) {

        return inventoryService.createInventory(
                requestDto.getProductId(),
                requestDto.getQuantity()
        );
    }

    // Get Inventory
    @GetMapping("/{productId}")
    public Inventory getInventory(@PathVariable Long productId) {
        return inventoryService.getInventory(productId);
    }

    // Check stock
    @PostMapping("/check")
    public String checkStock(@RequestBody InventoryRequestDto requestDto) {

        inventoryService.checkStock(
                requestDto.getProductId(),
                requestDto.getQuantity()
        );

        return "Stock available";
    }
    @PostMapping("/add-stock")
    public Inventory addStock(@RequestBody UpdateStockRequestDto requestDto) {

        return inventoryService.addStock(
                requestDto.getProductId(),
                requestDto.getQuantity()
        );
    }

    // Reduce stock
    @PutMapping("/reduce")
    public Inventory reduceStock(@RequestBody InventoryRequestDto requestDto) {

        return inventoryService.reduceStock(
                requestDto.getProductId(),
                requestDto.getQuantity()
        );
    }
}