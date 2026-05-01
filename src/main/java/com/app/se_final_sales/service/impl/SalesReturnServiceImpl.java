package com.app.se_final_sales.service.impl;

import com.app.se_final_sales.dto.SalesReturnRequest;
import com.app.se_final_sales.dto.SalesReturnResponse;
import com.app.se_final_sales.entity.*;
import com.app.se_final_sales.exception.ResourceNotFoundException;
import com.app.se_final_sales.mapper.SalesReturnMapper;
import com.app.se_final_sales.repository.*;
import com.app.se_final_sales.service.SalesReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesReturnServiceImpl implements SalesReturnService {

    private final SalesReturnRepository salesReturnRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SalesReturnMapper salesReturnMapper;

    @Override
    public SalesReturnResponse processReturn(SalesReturnRequest request) {
        SalesInvoice invoice = salesInvoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + request.getInvoiceId()));

        User processor = userRepository.findById(request.getProcessedById())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getProcessedById()));

        SalesReturn salesReturn = salesReturnMapper.toEntity(request);
        salesReturn.setInvoice(invoice);
        salesReturn.setProcessedBy(processor);
        salesReturn.setReturnNumber("RET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        salesReturn.setStatus("PENDING");

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SalesReturnLine line : salesReturn.getLines()) {
            Product product = productRepository.findById(line.getProduct().getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + line.getProduct().getProductId()));
            
            // Validate quantity against invoice
            SalesInvoiceLine invoiceLine = invoice.getLines().stream()
                    .filter(il -> il.getProduct().getProductId().equals(product.getProductId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Product " + product.getProductName() + " was not part of the original invoice."));
            
            if (line.getQuantity().compareTo(invoiceLine.getQuantity()) > 0) {
                throw new IllegalArgumentException("Return quantity for " + product.getProductName() + " exceeds invoiced quantity.");
            }

            line.setProduct(product);
            line.setSalesReturn(salesReturn);
            BigDecimal lineTotal = line.getUnitPrice().multiply(line.getQuantity());
            line.setLineTotal(lineTotal);
            totalAmount = totalAmount.add(lineTotal);
        }

        salesReturn.setTotalAmount(totalAmount);

        return salesReturnMapper.toResponse(salesReturnRepository.save(salesReturn));
    }

    @Override
    public SalesReturnResponse getReturnById(Long id) {
        return salesReturnRepository.findById(id)
                .map(salesReturnMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Return not found with ID: " + id));
    }

    @Override
    public List<SalesReturnResponse> getAllReturns() {
        return salesReturnRepository.findAll().stream()
                .map(salesReturnMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SalesReturnResponse approveReturn(Long id) {
        SalesReturn salesReturn = salesReturnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Return not found with ID: " + id));
        
        salesReturn.setStatus("APPROVED");
        return salesReturnMapper.toResponse(salesReturnRepository.save(salesReturn));
    }
}
