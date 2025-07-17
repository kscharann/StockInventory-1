package com.example.stockinventorysystem.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.stockinventorysystem.dto.MessageResponse;
import com.example.stockinventorysystem.dto.PurchaseRequest;
import com.example.stockinventorysystem.dto.PurchaseResponse;
import com.example.stockinventorysystem.model.EPaymentStatus;
import com.example.stockinventorysystem.model.Purchase;
import com.example.stockinventorysystem.repository.ProductRepository;
import com.example.stockinventorysystem.repository.PurchaseRepository;
import com.example.stockinventorysystem.repository.UserRepository;
import com.example.stockinventorysystem.security.services.UserDetailsImpl;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {
    @Autowired
    PurchaseRepository purchaseRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<PurchaseResponse>> getAllPurchases() {
        List<Purchase> purchases = purchaseRepository.findAll();
        List<PurchaseResponse> purchaseResponses = purchases.stream()
                .map(this::convertToPurchaseResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(purchaseResponses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getPurchaseById(@PathVariable Long id) {
        return purchaseRepository.findById(id)
                .map(purchase -> ResponseEntity.ok(convertToPurchaseResponse(purchase)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/po/{poId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getPurchaseByPoId(@PathVariable String poId) {
        return purchaseRepository.findByPoId(poId)
                .map(purchase -> ResponseEntity.ok(convertToPurchaseResponse(purchase)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vendor/{vendorName}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<PurchaseResponse>> getPurchasesByVendor(@PathVariable String vendorName) {
        List<Purchase> purchases = purchaseRepository.findByVendorNameContainingIgnoreCase(vendorName);
        List<PurchaseResponse> purchaseResponses = purchases.stream()
                .map(this::convertToPurchaseResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(purchaseResponses);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<PurchaseResponse>> getPurchasesByStatus(@PathVariable EPaymentStatus status) {
        List<Purchase> purchases = purchaseRepository.findByPaymentStatus(status);
        List<PurchaseResponse> purchaseResponses = purchases.stream()
                .map(this::convertToPurchaseResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(purchaseResponses);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<PurchaseResponse>> getPurchasesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Purchase> purchases = purchaseRepository.findByPurchaseDateBetween(startDate, endDate);
        List<PurchaseResponse> purchaseResponses = purchases.stream()
                .map(this::convertToPurchaseResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(purchaseResponses);
    }

    @PostMapping
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> createPurchase(@Valid @RequestBody PurchaseRequest purchaseRequest,
                                          Authentication authentication) {
        if (purchaseRepository.existsByPoId(purchaseRequest.getPoId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: PO ID already exists!"));
        }

        Purchase purchase = new Purchase();
        purchase.setVendorName(purchaseRequest.getVendorName());
        purchase.setPoId(purchaseRequest.getPoId());
        purchase.setProductName(purchaseRequest.getProductName());
        purchase.setQuantity(purchaseRequest.getQuantity());
        purchase.setUnitPrice(purchaseRequest.getUnitPrice());

        // Calculate total price if not provided
        if (purchaseRequest.getTotalPrice() != null) {
            purchase.setTotalPrice(purchaseRequest.getTotalPrice());
        } else {
            purchase.setTotalPrice(purchaseRequest.getUnitPrice()
                    .multiply(BigDecimal.valueOf(purchaseRequest.getQuantity())));
        }

        purchase.setPurchaseDate(purchaseRequest.getPurchaseDate());
        purchase.setDeliveryDate(purchaseRequest.getDeliveryDate());
        purchase.setPaymentStatus(purchaseRequest.getPaymentStatus());

        // Set product relationship if productId is provided
        if (purchaseRequest.getProductId() != null) {
            productRepository.findById(purchaseRequest.getProductId())
                    .ifPresent(purchase::setProduct);
        }

        // Set created by user
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            userRepository.findById(userDetails.getId())
                    .ifPresent(purchase::setCreatedBy);
        }

        purchaseRepository.save(purchase);

        return ResponseEntity.status(HttpStatus.CREATED).body(convertToPurchaseResponse(purchase));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updatePurchase(@PathVariable Long id,
                                          @Valid @RequestBody PurchaseRequest purchaseRequest) {
        return purchaseRepository.findById(id)
                .map(purchase -> {
                    // Check if PO ID is being changed and if it already exists
                    if (!purchase.getPoId().equals(purchaseRequest.getPoId()) &&
                        purchaseRepository.existsByPoId(purchaseRequest.getPoId())) {
                        return ResponseEntity
                                .badRequest()
                                .body(new MessageResponse("Error: PO ID already exists!"));
                    }

                    purchase.setVendorName(purchaseRequest.getVendorName());
                    purchase.setPoId(purchaseRequest.getPoId());
                    purchase.setProductName(purchaseRequest.getProductName());
                    purchase.setQuantity(purchaseRequest.getQuantity());
                    purchase.setUnitPrice(purchaseRequest.getUnitPrice());

                    // Calculate total price if not provided
                    if (purchaseRequest.getTotalPrice() != null) {
                        purchase.setTotalPrice(purchaseRequest.getTotalPrice());
                    } else {
                        purchase.setTotalPrice(purchaseRequest.getUnitPrice()
                                .multiply(BigDecimal.valueOf(purchaseRequest.getQuantity())));
                    }

                    purchase.setPurchaseDate(purchaseRequest.getPurchaseDate());
                    purchase.setDeliveryDate(purchaseRequest.getDeliveryDate());
                    purchase.setPaymentStatus(purchaseRequest.getPaymentStatus());

                    // Update product relationship if productId is provided
                    if (purchaseRequest.getProductId() != null) {
                        productRepository.findById(purchaseRequest.getProductId())
                                .ifPresent(purchase::setProduct);
                    } else {
                        purchase.setProduct(null);
                    }

                    purchaseRepository.save(purchase);
                    return ResponseEntity.ok(convertToPurchaseResponse(purchase));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Long id,
                                                @RequestParam EPaymentStatus status) {
        return purchaseRepository.findById(id)
                .map(purchase -> {
                    purchase.setPaymentStatus(status);
                    purchaseRepository.save(purchase);
                    return ResponseEntity.ok(convertToPurchaseResponse(purchase));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePurchase(@PathVariable Long id) {
        return purchaseRepository.findById(id)
                .map(purchase -> {
                    purchaseRepository.delete(purchase);
                    return ResponseEntity.ok(new MessageResponse("Purchase deleted successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private PurchaseResponse convertToPurchaseResponse(Purchase purchase) {
        PurchaseResponse response = new PurchaseResponse();
        response.setId(purchase.getId());
        response.setVendorName(purchase.getVendorName());
        response.setPoId(purchase.getPoId());
        response.setProductName(purchase.getProductName());
        response.setQuantity(purchase.getQuantity());
        response.setUnitPrice(purchase.getUnitPrice());
        response.setTotalPrice(purchase.getTotalPrice());
        response.setPurchaseDate(purchase.getPurchaseDate());
        response.setDeliveryDate(purchase.getDeliveryDate());
        response.setPaymentStatus(purchase.getPaymentStatus());
        response.setCreatedOn(purchase.getCreatedOn());

        if (purchase.getProduct() != null) {
            response.setProductId(purchase.getProduct().getId());
            response.setProductCode(purchase.getProduct().getCode());
        }

        if (purchase.getCreatedBy() != null) {
            response.setCreatedBy(purchase.getCreatedBy().getUsername());
        }

        return response;
    }
}
