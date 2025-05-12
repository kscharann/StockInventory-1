package com.example.stockinventorysystem.controller;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.stockinventorysystem.dto.MessageResponse;
import com.example.stockinventorysystem.dto.ProductRequest;
import com.example.stockinventorysystem.dto.ProductResponse;
import com.example.stockinventorysystem.model.Product;
import com.example.stockinventorysystem.repository.ProductRepository;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    ProductRepository productRepository;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductResponse> productResponses = products.stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productResponses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(product -> ResponseEntity.ok(convertToProductResponse(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        if (productRepository.existsByCode(productRequest.getCode())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Product code already exists!"));
        }

        Product product = new Product();
        product.setCode(productRequest.getCode());
        product.setName(productRequest.getName());
        product.setBrand(productRequest.getBrand());
        product.setCategory(productRequest.getCategory());
        product.setPrice(productRequest.getPrice());
        product.setColor(productRequest.getColor());
        product.setThumbnailImage(productRequest.getThumbnailImage());
        product.setSubImages(productRequest.getSubImages());
        product.setDescription(productRequest.getDescription());

        productRepository.save(product);

        return ResponseEntity.status(HttpStatus.CREATED).body(convertToProductResponse(product));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest productRequest) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setName(productRequest.getName());
                    product.setBrand(productRequest.getBrand());
                    product.setCategory(productRequest.getCategory());
                    product.setPrice(productRequest.getPrice());
                    product.setColor(productRequest.getColor());
                    product.setThumbnailImage(productRequest.getThumbnailImage());
                    product.setSubImages(productRequest.getSubImages());
                    product.setDescription(productRequest.getDescription());
                    
                    productRepository.save(product);
                    return ResponseEntity.ok(convertToProductResponse(product));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(product -> {
                    productRepository.delete(product);
                    return ResponseEntity.ok(new MessageResponse("Product deleted successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private ProductResponse convertToProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setCode(product.getCode());
        response.setName(product.getName());
        response.setBrand(product.getBrand());
        response.setCategory(product.getCategory());
        response.setPrice(product.getPrice());
        response.setColor(product.getColor());
        response.setThumbnailImage(product.getThumbnailImage());
        response.setSubImages(product.getSubImages());
        response.setDescription(product.getDescription());
        return response;
    }
}