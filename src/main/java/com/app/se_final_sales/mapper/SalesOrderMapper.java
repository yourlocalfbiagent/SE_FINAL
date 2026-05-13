package com.app.se_final_sales.mapper;

import com.app.se_final_sales.dto.SalesOrderLineRequest;
import com.app.se_final_sales.dto.SalesOrderLineResponse;
import com.app.se_final_sales.dto.SalesOrderRequest;
import com.app.se_final_sales.dto.SalesOrderResponse;
import com.app.se_final_sales.entity.SalesOrder;
import com.app.se_final_sales.entity.SalesOrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SalesOrderMapper {

    @Mapping(target = "partner.partnerId", source = "partnerId")
    @Mapping(target = "createdBy.userId", source = "createdById")
    SalesOrder toEntity(SalesOrderRequest request);

    @Mapping(target = "partnerId", source = "partner.partnerId")
    @Mapping(target = "partnerName", source = "partner.partnerName")
    @Mapping(target = "createdById", source = "createdBy.userId")
    @Mapping(target = "createdByEmail", source = "createdBy.email")
    SalesOrderResponse toResponse(SalesOrder entity);

    @Mapping(target = "product.productId", source = "productId")
    SalesOrderLine toEntity(SalesOrderLineRequest request);

    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productName", source = "product.productName")
    SalesOrderLineResponse toResponse(SalesOrderLine entity);
}
