package com.app.se_final_sales.controller;

import com.app.se_final_sales.service.SalesInvoiceService;
import com.app.se_final_sales.service.SalesOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ViewController {

    private final SalesOrderService salesOrderService;
    private final SalesInvoiceService salesInvoiceService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", salesOrderService.getAllOrders());
        return "orders";
    }

    @GetMapping("/invoices")
    public String invoices(Model model) {
        model.addAttribute("invoices", salesInvoiceService.getAllInvoices());
        return "invoices";
    }
}
