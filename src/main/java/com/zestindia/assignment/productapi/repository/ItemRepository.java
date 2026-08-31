package com.zestindia.assignment.productapi.repository;

import com.zestindia.assignment.productapi.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByProductId(Long productId);
}
