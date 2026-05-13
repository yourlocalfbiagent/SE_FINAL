package com.app.se_final_sales.service.impl;

import com.app.se_final_sales.dto.PaymentRequest;
import com.app.se_final_sales.dto.PaymentResponse;
import com.app.se_final_sales.entity.Payment;
import com.app.se_final_sales.entity.SalesInvoice;
import com.app.se_final_sales.exception.ResourceNotFoundException;
import com.app.se_final_sales.mapper.PaymentMapper;
import com.app.se_final_sales.repository.PaymentRepository;
import com.app.se_final_sales.repository.SalesInvoiceRepository;
import com.app.se_final_sales.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentResponse recordPayment(PaymentRequest request) {
        SalesInvoice invoice = salesInvoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + request.getInvoiceId()));

        if ("PAID".equals(invoice.getStatus())) {
            throw new IllegalStateException("Invoice is already fully paid.");
        }

        Payment payment = paymentMapper.toEntity(request);
        payment.setInvoice(invoice);
        
        Payment savedPayment = paymentRepository.save(payment);

        // Check if invoice is now fully paid
        // We'll need a custom query or sum the payments
        List<Payment> allPayments = paymentRepository.findAll().stream()
                .filter(p -> p.getInvoice().getInvoiceId().equals(invoice.getInvoiceId()))
                .collect(Collectors.toList());
        
        BigDecimal totalPaid = allPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus("PAID");
            salesInvoiceRepository.save(invoice);
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus("PARTIALLY_PAID");
            salesInvoiceRepository.save(invoice);
        }

        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    public List<PaymentResponse> getPaymentsByInvoiceId(Long invoiceId) {
        // This is inefficient but works for now as a POC
        return paymentRepository.findAll().stream()
                .filter(p -> p.getInvoice().getInvoiceId().equals(invoiceId))
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }
}
