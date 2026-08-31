package com.zestindia.assignment.productapi.service;

import com.zestindia.assignment.productapi.dto.request.UpdateProductRequest;
import com.zestindia.assignment.productapi.dto.response.ProductResponse;
import com.zestindia.assignment.productapi.dto.request.CreateProductRequest;
import com.zestindia.assignment.productapi.entity.Product;
import com.zestindia.assignment.productapi.exception.ProductNotFoundException;
import com.zestindia.assignment.productapi.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final String SYSTEM_USER = "system";

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<ProductResponse> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponse::from);
    }

    public ProductResponse findById(Long productId) {
        return ProductResponse.from(findProduct(productId));
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        Product product = new Product(request.productName().trim(), SYSTEM_USER);
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long productId, UpdateProductRequest request) {
        Product product = findProduct(productId);
        product.setProductName(request.productName().trim());
        product.setModifiedBy(SYSTEM_USER);
        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(Long productId) {
        productRepository.delete(findProduct(productId));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
