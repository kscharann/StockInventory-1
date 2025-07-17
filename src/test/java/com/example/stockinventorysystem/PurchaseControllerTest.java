package com.example.stockinventorysystem;

import com.example.stockinventorysystem.controller.PurchaseController;
import com.example.stockinventorysystem.dto.PurchaseRequest;
import com.example.stockinventorysystem.model.EPaymentStatus;
import com.example.stockinventorysystem.model.Purchase;
import com.example.stockinventorysystem.repository.ProductRepository;
import com.example.stockinventorysystem.repository.PurchaseRepository;
import com.example.stockinventorysystem.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchaseController.class)
public class PurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PurchaseRepository purchaseRepository;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "USER")
    public void testGetAllPurchases() throws Exception {
        Purchase purchase = createSamplePurchase();
        when(purchaseRepository.findAll()).thenReturn(Arrays.asList(purchase));

        mockMvc.perform(get("/api/purchases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].vendorName").value("ABC Suppliers"))
                .andExpect(jsonPath("$[0].poId").value("PO-2024-001"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetPurchaseById() throws Exception {
        Purchase purchase = createSamplePurchase();
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));

        mockMvc.perform(get("/api/purchases/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vendorName").value("ABC Suppliers"))
                .andExpect(jsonPath("$.poId").value("PO-2024-001"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetPurchaseByIdNotFound() throws Exception {
        when(purchaseRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/purchases/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    public void testCreatePurchase() throws Exception {
        PurchaseRequest request = createSamplePurchaseRequest();
        Purchase purchase = createSamplePurchase();
        
        when(purchaseRepository.existsByPoId("PO-2024-001")).thenReturn(false);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);

        mockMvc.perform(post("/api/purchases")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vendorName").value("ABC Suppliers"))
                .andExpect(jsonPath("$.poId").value("PO-2024-001"));
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    public void testCreatePurchaseWithDuplicatePoId() throws Exception {
        PurchaseRequest request = createSamplePurchaseRequest();
        
        when(purchaseRepository.existsByPoId("PO-2024-001")).thenReturn(true);

        mockMvc.perform(post("/api/purchases")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: PO ID already exists!"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDeletePurchase() throws Exception {
        Purchase purchase = createSamplePurchase();
        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(purchase));

        mockMvc.perform(delete("/api/purchases/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Purchase deleted successfully"));
    }

    private Purchase createSamplePurchase() {
        Purchase purchase = new Purchase();
        purchase.setId(1L);
        purchase.setVendorName("ABC Suppliers");
        purchase.setPoId("PO-2024-001");
        purchase.setProductName("Laptop Dell XPS 13");
        purchase.setQuantity(5);
        purchase.setUnitPrice(new BigDecimal("1200.00"));
        purchase.setTotalPrice(new BigDecimal("6000.00"));
        purchase.setPurchaseDate(LocalDate.of(2024, 1, 15));
        purchase.setDeliveryDate(LocalDate.of(2024, 1, 25));
        purchase.setPaymentStatus(EPaymentStatus.PENDING);
        return purchase;
    }

    private PurchaseRequest createSamplePurchaseRequest() {
        PurchaseRequest request = new PurchaseRequest();
        request.setVendorName("ABC Suppliers");
        request.setPoId("PO-2024-001");
        request.setProductName("Laptop Dell XPS 13");
        request.setQuantity(5);
        request.setUnitPrice(new BigDecimal("1200.00"));
        request.setPurchaseDate(LocalDate.of(2024, 1, 15));
        request.setDeliveryDate(LocalDate.of(2024, 1, 25));
        request.setPaymentStatus(EPaymentStatus.PENDING);
        return request;
    }
}
