package com.app.se_final_sales.mapper;

import com.app.se_final_sales.dto.SalesInvoiceLineRequest;
import com.app.se_final_sales.dto.SalesInvoiceLineResponse;
import com.app.se_final_sales.dto.SalesInvoiceRequest;
import com.app.se_final_sales.dto.SalesInvoiceResponse;
import com.app.se_final_sales.entity.SalesInvoice;
import com.app.se_final_sales.entity.SalesInvoiceLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SalesInvoiceMapper {

    @Mapping(target = "salesOrder.salesOrderId", source = "salesOrderId")
    @Mapping(target = "partner.partnerId", source = "partnerId")
    @Mapping(target = "createdBy.userId", source = "createdById")
    SalesInvoice toEntity(SalesInvoiceRequest request);

    @Mapping(target = "salesOrderId", source = "salesOrder.salesOrderId")
    @Mapping(target = "partnerId", source = "partner.partnerId")
    @Mapping(target = "partnerName", source = "partner.partnerName")
    @Mapping(target = "createdById", source = "createdBy.userId")
    @Mapping(target = "createdByEmail", source = "createdBy.email")
    SalesInvoiceResponse toResponse(SalesInvoice entity);

    @Mapping(target = "product.productId", source = "productId")
    SalesInvoiceLine toEntity(SalesInvoiceLineRequest request);

    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productName", source = "product.productName")
    SalesInvoiceLineResponse toResponse(SalesInvoiceLine entity);
}
