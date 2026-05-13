package com.sefinal.erp.purchasinginventory.dao;

import com.sefinal.erp.purchasinginventory.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderDao extends JpaRepository<PurchaseOrder, Integer> {
}