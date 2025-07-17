package com.example.stockinventorysystem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.stockinventorysystem.model.EInvoiceStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class InvoiceRequest {
    @NotBlank
    @Size(max = 50)
    private String invoiceId;

    @NotBlank
    @Size(max = 100)
    private String clientName;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private LocalDate invoiceDate;

    @Size(max = 500)
    private String description;

    private EInvoiceStatus paymentStatus = EInvoiceStatus.DRAFT;

    // Getters and setters
    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EInvoiceStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(EInvoiceStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
