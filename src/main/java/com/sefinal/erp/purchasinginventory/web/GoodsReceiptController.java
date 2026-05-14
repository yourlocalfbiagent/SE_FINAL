package com.sefinal.erp.purchasinginventory.web;

import com.sefinal.erp.purchasinginventory.dao.GoodsReceiptDao;
import com.sefinal.erp.purchasinginventory.model.GoodsReceipt;
import com.sefinal.erp.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goods-receipts")
@Tag(name = "Goods Receipts", description = "Record and retrieve goods receipts")
@PreAuthorize("hasRole('ADMIN') or hasAuthority('PURCHASING.read')")
public class GoodsReceiptController {

    private final GoodsReceiptDao goodsReceiptDao;

    public GoodsReceiptController(GoodsReceiptDao goodsReceiptDao) {
        this.goodsReceiptDao = goodsReceiptDao;
    }

    @GetMapping
    @Operation(summary = "List all goods receipts")
    public List<GoodsReceipt> getAllGoodsReceipts() {
        return goodsReceiptDao.findByCompanyId(SecurityUtils.getCompanyId());
    }

    @PostMapping
    @Operation(summary = "Record a new goods receipt")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PURCHASING.create')")
    public GoodsReceipt createGoodsReceipt(@RequestBody GoodsReceipt goodsReceipt) {
        goodsReceipt.setCompanyId(SecurityUtils.getCompanyId());
        if (goodsReceipt.getReceiptNumber() == null || goodsReceipt.getReceiptNumber().isBlank())
            goodsReceipt.setReceiptNumber("GR-" + System.currentTimeMillis());
        if (goodsReceipt.getStatus() == null || goodsReceipt.getStatus().isBlank())
            goodsReceipt.setStatus("received");
        return goodsReceiptDao.save(goodsReceipt);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a goods receipt")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PURCHASING.update')")
    public ResponseEntity<GoodsReceipt> updateGoodsReceipt(@PathVariable Long id, @RequestBody GoodsReceipt goodsReceipt) {
        if (!goodsReceiptDao.existsById(id)) return ResponseEntity.notFound().build();
        goodsReceipt.setReceiptId(id);
        goodsReceipt.setCompanyId(SecurityUtils.getCompanyId());
        if (goodsReceipt.getReceiptNumber() == null || goodsReceipt.getReceiptNumber().isBlank())
            goodsReceipt.setReceiptNumber("GR-" + System.currentTimeMillis());
        if (goodsReceipt.getStatus() == null || goodsReceipt.getStatus().isBlank())
            goodsReceipt.setStatus("received");
        return ResponseEntity.ok(goodsReceiptDao.save(goodsReceipt));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a goods receipt")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('PURCHASING.delete')")
    public ResponseEntity<Void> deleteGoodsReceipt(@PathVariable Long id) {
        if (!goodsReceiptDao.existsById(id)) return ResponseEntity.notFound().build();
        goodsReceiptDao.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
