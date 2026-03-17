package com.jitendra.inventoryservice.service;
import com.jitendra.event.*;
import com.jitendra.inventoryservice.exception.InsufficientStockException;
import com.jitendra.inventoryservice.exception.InventoryNotFoundException;
import com.jitendra.inventoryservice.model.Inventory;
import com.jitendra.inventoryservice.repository.InventoryRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Optional;



@RequiredArgsConstructor
@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private  final KafkaTemplate<String,Object> kafkaTemplate;

    @Override
    public Inventory createInventory(Long productId, Integer quantity) {

        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setQuantity(quantity);

        return inventoryRepository.save(inventory);
    }

    @Override
    public Inventory getInventory(Long productId) {

        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for productId: " + productId));
    }



    @Override
    public Inventory reduceStock(Long productId, Integer quantity) {

        Inventory inventory = getInventory(productId);

        if (inventory.getQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Stock not available for productId: " + productId);
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);

        return inventoryRepository.save(inventory);
    }
@Override
    public boolean checkStock(Long productId, Integer quantity) {
       Optional<Inventory> inventory = inventoryRepository.findByProductId(productId);
       if(inventory.isPresent()) {
           if (inventory.get().getQuantity() >=0) return true;
       }
        return true;
    }
    @Override
    public Inventory addStock(Long productId, Integer quantity) {

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for productId: " + productId));

        inventory.setQuantity(inventory.getQuantity() + quantity);

        return inventoryRepository.save(inventory);
    }

    @KafkaListener(topics = "order-created", groupId = "inventory-group")
    public void consumeOrderCreated(OrderCreatedEvent event) {

        boolean available = checkStock(event.getProductId(), event.getQuantity());
        InventoryReservedEvent event1=new InventoryReservedEvent();

        if (available) {
            InventoryReservedEvent event2=new InventoryReservedEvent();
            event2.setOrderId(event.getOrderId());
            event2.setProductId(event.getProductId());
            kafkaTemplate.send("inventory-reserved",event2 );
        } else {
            InventoryFailedEvent  event3=new InventoryFailedEvent();
            event3.setOrderId(event.getOrderId());
            event3.setProductId(event.getProductId());
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
}