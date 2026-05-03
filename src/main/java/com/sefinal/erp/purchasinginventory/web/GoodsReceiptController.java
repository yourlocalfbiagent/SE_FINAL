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

@RestController
@RequestMapping("/api/goods-receipts")
public class GoodsReceiptController {

    private final GoodsReceiptDao goodsReceiptDao;

    public GoodsReceiptController(GoodsReceiptDao goodsReceiptDao) {
        this.goodsReceiptDao = goodsReceiptDao;
    }

    @GetMapping
    public List<GoodsReceipt> getAllGoodsReceipts() {
        return goodsReceiptDao.findAll();
    }
    @PostMapping
public GoodsReceipt createGoodsReceipt(@RequestBody GoodsReceipt goodsReceipt) {
    return goodsReceiptDao.save(goodsReceipt);
}
}