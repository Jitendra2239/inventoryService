package com.jitendra.inventoryservice.service;



import com.jitendra.event.InventoryFailedEvent;
import com.jitendra.event.InventoryReservedEvent;
import com.jitendra.event.OrderCreatedEvent;
import com.jitendra.inventoryservice.exception.ResourceNotFoundException;
import com.jitendra.inventoryservice.model.Inventory;
import com.jitendra.inventoryservice.repository.InventoryRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String,Object> kafkaTemplate;
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
    public Boolean checkStock(Long productId, Integer quantity) {
       Optional<Inventory> inventory = inventoryRepository.findByProductId(productId);
       if(inventory.isPresent()) {
           if (inventory.get().getAvailableQuantity() >=0) return true;
       }
        return true;
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
//    @KafkaListener(topics = "payment-success")
//    public void reserveInventory(PaymentSuccessEvent event){
//
//        System.out.println("Reserving inventory");
//
//        InventoryReservedEvent inventoryEvent =
//                new InventoryReservedEvent(event.getOrderId(),"P100");
//
//        kafkaTemplate.send("inventory-reserved", inventoryEvent);
//
//    }
}