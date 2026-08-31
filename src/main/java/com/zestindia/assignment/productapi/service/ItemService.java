package com.zestindia.assignment.productapi.service;

import com.zestindia.assignment.productapi.dto.response.ItemResponse;
import com.zestindia.assignment.productapi.exception.ProductNotFoundException;
import com.zestindia.assignment.productapi.repository.ItemRepository;
import com.zestindia.assignment.productapi.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemService {

    private final ProductRepository productRepository;
    private final ItemRepository itemRepository;

    public ItemService(
            ProductRepository productRepository,
            ItemRepository itemRepository
    ) {
        this.productRepository = productRepository;
        this.itemRepository = itemRepository;
    }

    public List<ItemResponse> findAllByProductId(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        return itemRepository.findAllByProductId(productId)
                .stream()
                .map(ItemResponse::from)
                .toList();
    }
}