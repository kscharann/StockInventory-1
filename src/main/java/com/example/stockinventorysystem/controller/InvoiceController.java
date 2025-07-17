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

import com.example.stockinventorysystem.dto.InvoiceRequest;
import com.example.stockinventorysystem.dto.InvoiceResponse;
import com.example.stockinventorysystem.dto.MessageResponse;
import com.example.stockinventorysystem.model.EInvoiceStatus;
import com.example.stockinventorysystem.model.Invoice;
import com.example.stockinventorysystem.repository.InvoiceRepository;
import com.example.stockinventorysystem.repository.UserRepository;
import com.example.stockinventorysystem.security.services.UserDetailsImpl;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    @Autowired
    InvoiceRepository invoiceRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
        List<Invoice> invoices = invoiceRepository.findAll();
        List<InvoiceResponse> invoiceResponses = invoices.stream()
                .map(this::convertToInvoiceResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(invoiceResponses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getInvoiceById(@PathVariable Long id) {
        return invoiceRepository.findById(id)
                .map(invoice -> ResponseEntity.ok(convertToInvoiceResponse(invoice)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/invoice/{invoiceId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getInvoiceByInvoiceId(@PathVariable String invoiceId) {
        return invoiceRepository.findByInvoiceId(invoiceId)
                .map(invoice -> ResponseEntity.ok(convertToInvoiceResponse(invoice)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/client/{clientName}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByClient(@PathVariable String clientName) {
        List<Invoice> invoices = invoiceRepository.findByClientNameContainingIgnoreCase(clientName);
        List<InvoiceResponse> invoiceResponses = invoices.stream()
                .map(this::convertToInvoiceResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(invoiceResponses);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByStatus(@PathVariable EInvoiceStatus status) {
        List<Invoice> invoices = invoiceRepository.findByPaymentStatus(status);
        List<InvoiceResponse> invoiceResponses = invoices.stream()
                .map(this::convertToInvoiceResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(invoiceResponses);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Invoice> invoices = invoiceRepository.findByInvoiceDateBetween(startDate, endDate);
        List<InvoiceResponse> invoiceResponses = invoices.stream()
                .map(this::convertToInvoiceResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(invoiceResponses);
    }

    @GetMapping("/pending-approval")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<InvoiceResponse>> getPendingApprovalInvoices() {
        List<Invoice> invoices = invoiceRepository.findPendingApprovalInvoices();
        List<InvoiceResponse> invoiceResponses = invoices.stream()
                .map(this::convertToInvoiceResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(invoiceResponses);
    }

    @GetMapping("/total-amount/{status}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getTotalAmountByStatus(@PathVariable EInvoiceStatus status) {
        BigDecimal totalAmount = invoiceRepository.getTotalAmountByStatus(status);
        return ResponseEntity.ok(new MessageResponse("Total amount for status " + status + ": " +
                (totalAmount != null ? totalAmount : BigDecimal.ZERO)));
    }

    @PostMapping
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> createInvoice(@Valid @RequestBody InvoiceRequest invoiceRequest,
                                         Authentication authentication) {
        if (invoiceRepository.existsByInvoiceId(invoiceRequest.getInvoiceId())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Invoice ID already exists!"));
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceId(invoiceRequest.getInvoiceId());
        invoice.setClientName(invoiceRequest.getClientName());
        invoice.setAmount(invoiceRequest.getAmount());
        invoice.setInvoiceDate(invoiceRequest.getInvoiceDate());
        invoice.setDescription(invoiceRequest.getDescription());
        invoice.setPaymentStatus(invoiceRequest.getPaymentStatus());

        // Set created by user
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            userRepository.findById(userDetails.getId())
                    .ifPresent(invoice::setCreatedBy);
        }

        invoiceRepository.save(invoice);

        return ResponseEntity.status(HttpStatus.CREATED).body(convertToInvoiceResponse(invoice));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateInvoice(@PathVariable Long id,
                                         @Valid @RequestBody InvoiceRequest invoiceRequest) {
        return invoiceRepository.findById(id)
                .map(invoice -> {
                    // Check if Invoice ID is being changed and if it already exists
                    if (!invoice.getInvoiceId().equals(invoiceRequest.getInvoiceId()) &&
                        invoiceRepository.existsByInvoiceId(invoiceRequest.getInvoiceId())) {
                        return ResponseEntity
                                .badRequest()
                                .body(new MessageResponse("Error: Invoice ID already exists!"));
                    }

                    invoice.setInvoiceId(invoiceRequest.getInvoiceId());
                    invoice.setClientName(invoiceRequest.getClientName());
                    invoice.setAmount(invoiceRequest.getAmount());
                    invoice.setInvoiceDate(invoiceRequest.getInvoiceDate());
                    invoice.setDescription(invoiceRequest.getDescription());
                    invoice.setPaymentStatus(invoiceRequest.getPaymentStatus());

                    invoiceRepository.save(invoice);
                    return ResponseEntity.ok(convertToInvoiceResponse(invoice));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Long id,
                                                @RequestParam EInvoiceStatus status) {
        return invoiceRepository.findById(id)
                .map(invoice -> {
                    invoice.setPaymentStatus(status);
                    invoiceRepository.save(invoice);
                    return ResponseEntity.ok(convertToInvoiceResponse(invoice));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> approveInvoice(@PathVariable Long id, Authentication authentication) {
        return invoiceRepository.findById(id)
                .map(invoice -> {
                    if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
                        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                        userRepository.findById(userDetails.getId())
                                .ifPresent(invoice::setApprovedBy);
                    }

                    // Update status to APPROVED if it was PENDING
                    if (invoice.getPaymentStatus() == EInvoiceStatus.PENDING) {
                        invoice.setPaymentStatus(EInvoiceStatus.APPROVED);
                    }

                    invoiceRepository.save(invoice);
                    return ResponseEntity.ok(convertToInvoiceResponse(invoice));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteInvoice(@PathVariable Long id) {
        return invoiceRepository.findById(id)
                .map(invoice -> {
                    invoiceRepository.delete(invoice);
                    return ResponseEntity.ok(new MessageResponse("Invoice deleted successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private InvoiceResponse convertToInvoiceResponse(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.setId(invoice.getId());
        response.setInvoiceId(invoice.getInvoiceId());
        response.setClientName(invoice.getClientName());
        response.setAmount(invoice.getAmount());
        response.setInvoiceDate(invoice.getInvoiceDate());
        response.setDescription(invoice.getDescription());
        response.setPaymentStatus(invoice.getPaymentStatus());
        response.setCreatedOn(invoice.getCreatedOn());
        response.setApprovedOn(invoice.getApprovedOn());

        if (invoice.getCreatedBy() != null) {
            response.setCreatedBy(invoice.getCreatedBy().getUsername());
        }

        if (invoice.getApprovedBy() != null) {
            response.setApprovedBy(invoice.getApprovedBy().getUsername());
        }

        return response;
    }
}
