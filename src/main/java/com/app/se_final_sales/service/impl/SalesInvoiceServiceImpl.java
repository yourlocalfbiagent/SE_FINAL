package com.app.se_final_sales.service.impl;

import com.app.se_final_sales.dto.SalesInvoiceRequest;
import com.app.se_final_sales.dto.SalesInvoiceResponse;
import com.app.se_final_sales.entity.*;
import com.app.se_final_sales.exception.ResourceNotFoundException;
import com.app.se_final_sales.mapper.SalesInvoiceMapper;
import com.app.se_final_sales.repository.*;
import com.app.se_final_sales.service.SalesInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesInvoiceServiceImpl implements SalesInvoiceService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final BusinessPartnerRepository businessPartnerRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SalesInvoiceMapper salesInvoiceMapper;

    @Override
    public SalesInvoiceResponse createInvoice(SalesInvoiceRequest request) {
        BusinessPartner partner = businessPartnerRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with ID: " + request.getPartnerId()));

        User creator = userRepository.findById(request.getCreatedById())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getCreatedById()));

        SalesInvoice invoice = salesInvoiceMapper.toEntity(request);
        invoice.setPartner(partner);
        invoice.setCreatedBy(creator);
        invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        
        if (request.getSalesOrderId() != null) {
            SalesOrder order = salesOrderRepository.findById(request.getSalesOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.getSalesOrderId()));
            invoice.setSalesOrder(order);
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (SalesInvoiceLine line : invoice.getLines()) {
            Product product = productRepository.findById(line.getProduct().getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + line.getProduct().getProductId()));
            line.setProduct(product);
            line.setInvoice(invoice);
            BigDecimal lineTotal = line.getUnitPrice().multiply(line.getQuantity());
            line.setLineTotal(lineTotal);
            subtotal = subtotal.add(lineTotal);
        }

        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(subtotal.multiply(new BigDecimal("0.10")));
        invoice.setTotalAmount(invoice.getSubtotal().add(invoice.getTaxAmount()));

        return salesInvoiceMapper.toResponse(salesInvoiceRepository.save(invoice));
    }

    @Override
    public SalesInvoiceResponse generateInvoiceFromOrder(Long orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Order not found with ID: " + orderId));

        if (!"CONFIRMED".equals(order.getStatus())) {
            throw new IllegalStateException("Only CONFIRMED orders can be invoiced.");
        }

        SalesInvoice invoice = SalesInvoice.builder()
                .salesOrder(order)
                .partner(order.getPartner())
                .createdBy(order.getCreatedBy())
                .invoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .invoiceDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30)) // Default 30 days
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .status("UNPAID")
                .lines(new ArrayList<>())
                .build();

        for (SalesOrderLine orderLine : order.getLines()) {
            SalesInvoiceLine invoiceLine = SalesInvoiceLine.builder()
                    .invoice(invoice)
                    .product(orderLine.getProduct())
                    .quantity(orderLine.getQuantity())
                    .unitPrice(orderLine.getUnitPrice())
                    .lineTotal(orderLine.getLineTotal())
                    .build();
            invoice.getLines().add(invoiceLine);
        }

        return salesInvoiceMapper.toResponse(salesInvoiceRepository.save(invoice));
    }

    @Override
    public SalesInvoiceResponse getInvoiceById(Long id) {
        return salesInvoiceRepository.findById(id)
                .map(salesInvoiceMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + id));
    }

    @Override
    public List<SalesInvoiceResponse> getAllInvoices() {
        return salesInvoiceRepository.findAll().stream()
                .map(salesInvoiceMapper::toResponse)
                .collect(Collectors.toList());
    }
}
