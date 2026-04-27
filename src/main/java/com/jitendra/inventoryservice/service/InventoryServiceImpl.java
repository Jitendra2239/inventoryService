package com.jitendra.inventoryservice.service;

import com.jitendra.event.*;
import com.jitendra.inventoryservice.dto.InventoryRequestDto;
import com.jitendra.inventoryservice.dto.InventoryResponseDto;
import com.jitendra.inventoryservice.dto.StockCheckRequestDto;

import com.jitendra.inventoryservice.exception.InventoryNotFoundException;
import com.jitendra.inventoryservice.mapper.InventoryMapper;
import com.jitendra.inventoryservice.model.Inventory;
import com.jitendra.inventoryservice.repository.InventoryRepository;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.lang.module.ResolutionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



@RequiredArgsConstructor
@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private  final KafkaTemplate<String,Object> kafkaTemplate;


    @Override
    public InventoryResponseDto createInventory(InventoryRequestDto dto) {

        Inventory inventory = InventoryMapper.toEntity(dto);

        Inventory saved = inventoryRepository.save(inventory);

        return InventoryMapper.toDto(saved);
    }

    @Override
    public InventoryResponseDto getInventory(String productId) {

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for productId: " + productId));

        return InventoryMapper.toDto(inventory);  // ✅ FIX
    }



    @Override
    public InventoryResponseDto reduceStock(String productId, Integer quantity) {

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock");
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);

        return InventoryMapper.toDto(inventoryRepository.save(inventory));
    }

    @Override
    public InventoryResponseDto addStock(String productId, Integer quantity) {

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        inventory.setQuantity(inventory.getQuantity() + quantity);

        return InventoryMapper.toDto(inventoryRepository.save(inventory));
    }
    @Override
    public boolean checkStock(List<StockCheckRequestDto> items) {

        for (StockCheckRequestDto item : items) {

            Inventory inventory = inventoryRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (inventory.getQuantity() < item.getQuantity()) {
                return false;
            }
        }
        return true;
    }
    @KafkaListener(topics = "order-created", groupId = "inventory-group")
    public void consumeOrderCreated(OrderCreatedEvent event) {
        System.out.println("Received OrderCreatedEvent : " + event);
        List<StockCheckRequestDto> items =new ArrayList<>();
        for (OrderItemEvent item : event.getItems()) {
            StockCheckRequestDto stockCheckRequestDto = new StockCheckRequestDto();
            stockCheckRequestDto.setProductId(item.getProductId());
            stockCheckRequestDto.setQuantity(item.getQuantity());
            items.add(stockCheckRequestDto);
        }
        boolean available = checkStock(items);
        InventoryReservedEvent event1=new InventoryReservedEvent();

        if (available) {

            for(OrderItemEvent item : event.getItems()) {
                reduceStock(item.getProductId(), item.getQuantity());
            }
            InventoryReservedEvent event2=new InventoryReservedEvent();
            event2.setOrderId(event.getOrderId());
            event2.setAmount(event.getTotalAmount());
            event2.setUserId(event.getUserId());
            event2.setEmail(event.getEmail());

            kafkaTemplate.send("inventory-reserved",event2 );
        } else {
            InventoryFailedEvent event3=new InventoryFailedEvent();
            event3.setOrderId(event.getOrderId());
            event3.setReason("Inventory Reservation Failed");
            event3.setTimestamp(System.currentTimeMillis());
            kafkaTemplate.send("inventory-failed",event3);
        }
    }
    @KafkaListener(topics = "product-created")
    @SendTo
    public InventoryCreatedEvent handleProductCreated(ProductCreatedEvent event) {

        Inventory inventory = new Inventory();
        inventory.setProductId(event.getProductId());
        inventory.setQuantity(0);

        inventoryRepository.save(inventory);

        return new InventoryCreatedEvent(event.getProductId(), "CREATED");
    }

    @KafkaListener(topics = "inventory-check", groupId = "inventory-group")
    public void checkStock(InventoryCheckEvent event) {

       Optional<Inventory> inventory = inventoryRepository
                .findByProductId(event.getProductId());

        AddToCartResponseEvent response = new AddToCartResponseEvent();
        response.setUserId(event.getUserId());
        response.setProductId(event.getProductId());

        if (inventory.isEmpty()  ) {
            response.setSuccess(false);
            response.setMessage("Out of stock");
        }
        else if(inventory.isPresent() && inventory.get().getQuantity() <event.getQuantity()){

              response.setSuccess(false);
            response.setMessage("Out of stock");

        }
            else {
            response.setSuccess(true);
            response.setMessage("Stock available");
        }

        kafkaTemplate.send("inventory-response", response);
    }

    @KafkaListener(topics = "order-cancelled")
    public void handleOrderCancelled(OrderCancelledEvent event) {

        for (OrderItemEvent item : event.getItems()) {

            Optional<Inventory> inventory = Optional.ofNullable(inventoryRepository.findByProductId(item.getProductId()).orElseThrow(() -> new InventoryNotFoundException("Inventory not found")));

            inventory.get().setQuantity(
                    inventory.get().getQuantity() + item.getQuantity()
            );

            inventoryRepository.save(inventory.get());
        }
    }
}