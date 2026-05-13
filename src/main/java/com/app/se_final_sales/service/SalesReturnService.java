package com.app.se_final_sales.service;

import com.app.se_final_sales.dto.SalesReturnRequest;
import com.app.se_final_sales.dto.SalesReturnResponse;

import java.util.List;

public interface SalesReturnService {
    SalesReturnResponse processReturn(SalesReturnRequest request);
    SalesReturnResponse getReturnById(Long id);
    List<SalesReturnResponse> getAllReturns();
    SalesReturnResponse approveReturn(Long id);
}
