package com.app.se_final_sales.controller;

import com.app.se_final_sales.service.SalesInvoiceService;
import com.app.se_final_sales.service.SalesOrderService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasAuthority('SALES.read')")
public class ViewController {

    private final SalesOrderService salesOrderService;
    private final SalesInvoiceService salesInvoiceService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long companyId = ((Number) claims.get("companyId")).longValue();
        model.addAttribute("orders", salesOrderService.getAllOrders(companyId));
        return "orders";
    }

    @GetMapping("/invoices")
    public String invoices(Model model) {
        Claims claims = (Claims) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long companyId = ((Number) claims.get("companyId")).longValue();
        model.addAttribute("invoices", salesInvoiceService.getAllInvoices(companyId));
        return "invoices";
    }
}
