package com.zestindia.assignment.productapi.controller;

import com.zestindia.assignment.productapi.dto.response.ItemResponse;
import com.zestindia.assignment.productapi.dto.response.PageResponse;
import com.zestindia.assignment.productapi.dto.request.CreateProductRequest;
import com.zestindia.assignment.productapi.dto.response.ProductResponse;
import com.zestindia.assignment.productapi.service.ItemService;
import com.zestindia.assignment.productapi.service.ProductService;
import com.zestindia.assignment.productapi.dto.request.UpdateProductRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private static final int MAX_PAGE_SIZE = 100;

    private final ProductService productService;
    private final ItemService itemService;

    public ProductController(ProductService productService,ItemService itemService) {
        this.productService = productService;
        this.itemService=itemService;
    }

    @GetMapping
    public PageResponse<ProductResponse> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE)
        );
        return PageResponse.from(productService.findAll(pageable), product -> product);
    }

    @GetMapping("/{productId}")
    public ProductResponse findById(@PathVariable Long productId) {
        return productService.findById(productId);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{productId}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{productId}")
    public ProductResponse update(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return productService.update(productId, request);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@PathVariable Long productId) {
        productService.delete(productId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}/items")
    public List<ItemResponse> findItemsByProductId(
            @PathVariable Long productId
    ) {
        return itemService.findAllByProductId(productId);
    }

}
