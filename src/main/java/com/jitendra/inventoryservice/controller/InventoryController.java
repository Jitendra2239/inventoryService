package com.jitendra.inventoryservice.controller;

import com.jitendra.inventoryservice.dto.InventoryRequest;
import com.jitendra.inventoryservice.model.Inventory;
import com.jitendra.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    @GetMapping("/hello")
  public  String getHello(){
      return "Hello World";
  }
    @GetMapping("/{productId}")
    public Inventory getInventory(@PathVariable Long productId) {
        return inventoryService.getInventory(productId);
    }

    @PostMapping("/reserve")
    public String reserveInventory(@RequestBody InventoryRequest request) {

        inventoryService.reserveInventory(
                request.getProductId(),
                request.getQuantity());

        return "Inventory reserved";
    }

    @PostMapping("/release")
    public String releaseInventory(@RequestBody InventoryRequest request) {

        inventoryService.releaseInventory(
                request.getProductId(),
                request.getQuantity());

        return "Inventory released";
    }

    @PostMapping("/deduct")
    public String deductInventory(@RequestBody InventoryRequest request) {

        inventoryService.deductInventory(
                request.getProductId(),
                request.getQuantity());

        return "Inventory deducted";
    }
}