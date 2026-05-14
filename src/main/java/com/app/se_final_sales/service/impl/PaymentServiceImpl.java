package com.app.se_final_sales.service.impl;

import com.app.se_final_sales.dto.PaymentRequest;
import com.app.se_final_sales.dto.PaymentResponse;
import com.app.se_final_sales.entity.Payment;
import com.app.se_final_sales.entity.SalesInvoice;
import com.sefinal.erp.exception.ResourceNotFoundException;
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
        List<Payment> allPayments = paymentRepository.findByInvoiceInvoiceId(invoice.getInvoiceId());
        
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
        return paymentRepository.findByInvoiceInvoiceId(invoiceId).stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getAllPayments(Long companyId) {
        return paymentRepository.findByInvoicePartnerCompanyId(companyId).stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse updatePayment(Long id, PaymentRequest request) {
        Payment existing = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        
        existing.setAmount(request.getAmount());
        existing.setPaymentDate(request.getPaymentDate());
        existing.setPaymentMethod(request.getPaymentMethod());
        existing.setReference(request.getReference());
        
        Payment saved = paymentRepository.save(existing);
        recalculateInvoiceStatus(existing.getInvoice().getInvoiceId());
        return paymentMapper.toResponse(saved);
    }

    @Override
    public void deletePayment(Long id) {
        Payment existing = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        Long invoiceId = existing.getInvoice().getInvoiceId();
        paymentRepository.deleteById(id);
        recalculateInvoiceStatus(invoiceId);
    }

    private void recalculateInvoiceStatus(Long invoiceId) {
        SalesInvoice invoice = salesInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + invoiceId));
        
        List<Payment> allPayments = paymentRepository.findAll().stream()
                .filter(p -> p.getInvoice().getInvoiceId().equals(invoiceId))
                .collect(Collectors.toList());
        
        BigDecimal totalPaid = allPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus("PAID");
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus("PARTIALLY_PAID");
        } else {
            invoice.setStatus("UNPAID");
        }
        salesInvoiceRepository.save(invoice);
    }
}
