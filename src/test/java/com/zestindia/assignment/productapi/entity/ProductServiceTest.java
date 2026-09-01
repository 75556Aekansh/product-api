package com.zestindia.assignment.productapi.entity;

import com.zestindia.assignment.productapi.dto.request.CreateProductRequest;
import com.zestindia.assignment.productapi.exception.ProductNotFoundException;
import com.zestindia.assignment.productapi.repository.ProductRepository;
import com.zestindia.assignment.productapi.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUpAuthentication() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "aekansh",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTrimsProductNameAndUsesAuthenticatedUser() {
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        productService.create(
                new CreateProductRequest("  Keyboard  ")
        );

        ArgumentCaptor<Product> productCaptor =
                ArgumentCaptor.forClass(Product.class);

        verify(productRepository).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();

        assertThat(savedProduct.getProductName())
                .isEqualTo("Keyboard");

        assertThat(savedProduct.getCreatedBy())
                .isEqualTo("aekansh");
    }

    @Test
    void findByIdThrowsWhenProductDoesNotExist() {
        when(productRepository.findById(99L))
                .thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product with id 99 was not found");

        verify(productRepository).findById(eq(99L));
    }
}