package com.jitendra.inventoryservice.controller;

import com.jitendra.inventoryservice.dto.*;
import com.jitendra.inventoryservice.service.InventoryService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {

    private final InventoryService inventoryService;


    @PostMapping
    public ResponseEntity<InventoryResponseDto> createInventory(
            @RequestBody InventoryRequestDto dto) {

        InventoryResponseDto response = inventoryService.createInventory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponseDto> getInventory(
            @PathVariable String  productId) {

        return ResponseEntity.ok(inventoryService.getInventory(productId));
    }


    @PostMapping("/check")
    public ResponseEntity<Boolean> checkStock(
            @RequestBody List<StockCheckRequestDto> items) {

        return ResponseEntity.ok(inventoryService.checkStock(items));
    }


    @PutMapping("/{productId}/reduce")
    public ResponseEntity<InventoryResponseDto> reduceStock(
            @PathVariable String  productId,
            @RequestParam Integer quantity) {

        return ResponseEntity.ok(
                inventoryService.reduceStock(productId, quantity)
        );
    }


    @PutMapping("/{productId}/add")
    public ResponseEntity<InventoryResponseDto> addStock(
            @PathVariable String  productId,
            @RequestParam Integer quantity) {

        return ResponseEntity.ok(
                inventoryService.addStock(productId, quantity)
        );
    }
}