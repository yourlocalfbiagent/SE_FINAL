package com.app.se_final_sales.service;

import com.app.se_final_sales.dto.PaymentRequest;
import com.app.se_final_sales.dto.PaymentResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse recordPayment(PaymentRequest request);
    List<PaymentResponse> getPaymentsByInvoiceId(Long invoiceId);
}
