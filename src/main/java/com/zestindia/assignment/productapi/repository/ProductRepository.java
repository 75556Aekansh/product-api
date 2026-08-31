package com.zestindia.assignment.productapi.repository;

import com.zestindia.assignment.productapi.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
