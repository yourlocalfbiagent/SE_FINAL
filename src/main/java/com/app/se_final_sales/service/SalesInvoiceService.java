package com.app.se_final_sales.service;

import com.app.se_final_sales.dto.SalesInvoiceRequest;
import com.app.se_final_sales.dto.SalesInvoiceResponse;

import java.util.List;

public interface SalesInvoiceService {
    SalesInvoiceResponse createInvoice(SalesInvoiceRequest request);
    SalesInvoiceResponse generateInvoiceFromOrder(Long orderId);
    SalesInvoiceResponse getInvoiceById(Long id);
    List<SalesInvoiceResponse> getAllInvoices();
}
