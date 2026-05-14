// GoodsReceiptController.java
package com.sefinal.erp.purchasinginventory.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sefinal.erp.purchasinginventory.dao.GoodsReceiptDao;
import com.sefinal.erp.purchasinginventory.model.GoodsReceipt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/goods-receipts")
@Tag(name = "Goods Receipts", description = "Record and retrieve goods receipts")
public class GoodsReceiptController {

    private final GoodsReceiptDao goodsReceiptDao;

    public GoodsReceiptController(GoodsReceiptDao goodsReceiptDao) {
        this.goodsReceiptDao = goodsReceiptDao;
    }

    @GetMapping
    @Operation(summary = "List all goods receipts")
    public List<GoodsReceipt> getAllGoodsReceipts() {
        return goodsReceiptDao.findAll();
    }

    @PostMapping
    @Operation(summary = "Record a new goods receipt")
    public GoodsReceipt createGoodsReceipt(@RequestBody GoodsReceipt goodsReceipt) {
        return goodsReceiptDao.save(goodsReceipt);
    }
}