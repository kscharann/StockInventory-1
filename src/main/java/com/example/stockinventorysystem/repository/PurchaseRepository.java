package com.example.stockinventorysystem.repository;

import com.example.stockinventorysystem.model.EPaymentStatus;
import com.example.stockinventorysystem.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Optional<Purchase> findByPoId(String poId);
    Boolean existsByPoId(String poId);
    
    List<Purchase> findByVendorNameContainingIgnoreCase(String vendorName);
    List<Purchase> findByProductNameContainingIgnoreCase(String productName);
    List<Purchase> findByPaymentStatus(EPaymentStatus paymentStatus);
    
    List<Purchase> findByPurchaseDateBetween(LocalDate startDate, LocalDate endDate);
    List<Purchase> findByDeliveryDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT p FROM Purchase p WHERE p.product.id = :productId")
    List<Purchase> findByProductId(@Param("productId") Long productId);
    
    @Query("SELECT p FROM Purchase p WHERE p.createdBy.id = :userId")
    List<Purchase> findByCreatedBy(@Param("userId") Long userId);
}
