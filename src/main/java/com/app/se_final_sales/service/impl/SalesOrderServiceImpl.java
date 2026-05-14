package com.app.se_final_sales.service.impl;

import com.app.se_final_sales.dto.SalesOrderRequest;
import com.app.se_final_sales.dto.SalesOrderResponse;
import com.app.se_final_sales.entity.*;
import com.sefinal.erp.entity.BusinessPartner;
import com.sefinal.erp.entity.Product;
import com.sefinal.erp.entity.User;
import com.sefinal.erp.exception.ResourceNotFoundException;
import com.app.se_final_sales.mapper.SalesOrderMapper;
import com.app.se_final_sales.repository.BusinessPartnerRepository;
import com.app.se_final_sales.repository.ProductRepository;
import com.app.se_final_sales.repository.SalesOrderRepository;
import com.app.se_final_sales.repository.UserRepository;
import com.app.se_final_sales.service.SalesOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final BusinessPartnerRepository businessPartnerRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SalesOrderMapper salesOrderMapper;

    @Override
    public SalesOrderResponse createOrder(SalesOrderRequest request) {
        // Validate Partner
        BusinessPartner partner = businessPartnerRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with ID: " + request.getPartnerId()));

        // Validate Creator
        User creator = userRepository.findById(request.getCreatedById())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getCreatedById()));

        SalesOrder salesOrder = salesOrderMapper.toEntity(request);
        salesOrder.setPartner(partner);
        salesOrder.setCreatedBy(creator);
        salesOrder.setSalesOrderNumber("SO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        salesOrder.setStatus("DRAFT");

        // Calculate totals and set lines
        BigDecimal subtotal = BigDecimal.ZERO;
        for (SalesOrderLine line : salesOrder.getLines()) {
            Product product = productRepository.findById(line.getProduct().getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + line.getProduct().getProductId()));
            
            line.setProduct(product);
            line.setSalesOrder(salesOrder);
            
            BigDecimal lineTotal = line.getUnitPrice().multiply(line.getQuantity());
            line.setLineTotal(lineTotal);
            subtotal = subtotal.add(lineTotal);
        }

        salesOrder.setSubtotal(subtotal);
        // Assuming 10% tax for now as a default logic if not provided
        salesOrder.setTaxAmount(subtotal.multiply(new BigDecimal("0.10"))); 
        salesOrder.setTotalAmount(salesOrder.getSubtotal().add(salesOrder.getTaxAmount()));

        SalesOrder savedOrder = salesOrderRepository.save(salesOrder);
        return salesOrderMapper.toResponse(savedOrder);
    }

    @Override
    public SalesOrderResponse getOrderById(Long id) {
        return salesOrderRepository.findById(id)
                .map(salesOrderMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with ID: " + id));
    }

    @Override
    public List<SalesOrderResponse> getAllOrders(Long companyId) {
        return salesOrderRepository.findByPartnerCompanyId(companyId).stream()
                .map(salesOrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SalesOrderResponse confirmOrder(Long id) {
        SalesOrder salesOrder = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with ID: " + id));
        
        if (!"DRAFT".equals(salesOrder.getStatus())) {
            throw new IllegalStateException("Only DRAFT orders can be confirmed.");
        }
        
        salesOrder.setStatus("CONFIRMED");
        return salesOrderMapper.toResponse(salesOrderRepository.save(salesOrder));
    }

    @Override
    public SalesOrderResponse updateOrder(Long id, SalesOrderRequest request) {
        SalesOrder existing = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with ID: " + id));
        
        if (!"DRAFT".equals(existing.getStatus())) {
            throw new IllegalStateException("Only DRAFT orders can be updated.");
        }

        // Simple update logic: clear lines and re-add from request
        existing.getLines().clear();
        
        SalesOrder updatedEntity = salesOrderMapper.toEntity(request);
        
        // Validate Partner
        BusinessPartner partner = businessPartnerRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with ID: " + request.getPartnerId()));
        existing.setPartner(partner);

        // Recalculate totals
        BigDecimal subtotal = BigDecimal.ZERO;
        for (SalesOrderLine line : updatedEntity.getLines()) {
            Product product = productRepository.findById(line.getProduct().getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + line.getProduct().getProductId()));
            
            line.setProduct(product);
            line.setSalesOrder(existing);
            
            BigDecimal lineTotal = line.getUnitPrice().multiply(line.getQuantity());
            line.setLineTotal(lineTotal);
            subtotal = subtotal.add(lineTotal);
            existing.getLines().add(line);
        }

        existing.setSubtotal(subtotal);
        existing.setTaxAmount(subtotal.multiply(new BigDecimal("0.10"))); 
        existing.setTotalAmount(existing.getSubtotal().add(existing.getTaxAmount()));

        return salesOrderMapper.toResponse(salesOrderRepository.save(existing));
    }

    @Override
    public void deleteOrder(Long id) {
        SalesOrder salesOrder = salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with ID: " + id));
        
        if (!"DRAFT".equals(salesOrder.getStatus())) {
            throw new IllegalStateException("Only DRAFT orders can be deleted.");
        }
        
        salesOrderRepository.delete(salesOrder);
    }
}
