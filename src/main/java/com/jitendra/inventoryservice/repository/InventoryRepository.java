package com.jitendra.inventoryservice.repository;

import java.util.Optional;

import com.jitendra.inventoryservice.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;


public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(Long productId);

}