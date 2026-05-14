package com.app.se_final_sales.service;

import com.app.se_final_sales.dto.SalesOrderRequest;
import com.app.se_final_sales.dto.SalesOrderResponse;

import java.util.List;

public interface SalesOrderService {
    SalesOrderResponse createOrder(SalesOrderRequest request);
    SalesOrderResponse getOrderById(Long id);
    List<SalesOrderResponse> getAllOrders(Long companyId);
    SalesOrderResponse confirmOrder(Long id);
    SalesOrderResponse updateOrder(Long id, SalesOrderRequest request);
    void deleteOrder(Long id);
}
