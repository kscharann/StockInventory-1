package com.example.stockinventorysystem.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.stockinventorysystem.model.EInvoiceStatus;
import com.example.stockinventorysystem.model.Invoice;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceId(String invoiceId);
    Boolean existsByInvoiceId(String invoiceId);

    List<Invoice> findByClientNameContainingIgnoreCase(String clientName);
    List<Invoice> findByPaymentStatus(EInvoiceStatus paymentStatus);

    List<Invoice> findByInvoiceDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT i FROM Invoice i WHERE i.createdBy.id = :userId")
    List<Invoice> findByCreatedBy(@Param("userId") Long userId);

    @Query("SELECT i FROM Invoice i WHERE i.approvedBy.id = :userId")
    List<Invoice> findByApprovedBy(@Param("userId") Long userId);

    @Query("SELECT i FROM Invoice i WHERE i.approvedBy IS NULL AND i.paymentStatus = 'PENDING'")
    List<Invoice> findPendingApprovalInvoices();

    @Query("SELECT SUM(i.amount) FROM Invoice i WHERE i.paymentStatus = :status")
    BigDecimal getTotalAmountByStatus(@Param("status") EInvoiceStatus status);
}
