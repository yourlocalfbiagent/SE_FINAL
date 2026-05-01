package com.app.se_final_sales.mapper;

import com.app.se_final_sales.dto.SalesReturnLineRequest;
import com.app.se_final_sales.dto.SalesReturnLineResponse;
import com.app.se_final_sales.dto.SalesReturnRequest;
import com.app.se_final_sales.dto.SalesReturnResponse;
import com.app.se_final_sales.entity.SalesReturn;
import com.app.se_final_sales.entity.SalesReturnLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SalesReturnMapper {

    @Mapping(target = "invoice.invoiceId", source = "invoiceId")
    @Mapping(target = "processedBy.userId", source = "processedById")
    SalesReturn toEntity(SalesReturnRequest request);

    @Mapping(target = "invoiceId", source = "invoice.invoiceId")
    @Mapping(target = "invoiceNumber", source = "invoice.invoiceNumber")
    @Mapping(target = "processedById", source = "processedBy.userId")
    @Mapping(target = "processedByEmail", source = "processedBy.email")
    SalesReturnResponse toResponse(SalesReturn entity);

    @Mapping(target = "product.productId", source = "productId")
    SalesReturnLine toEntity(SalesReturnLineRequest request);

    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productName", source = "product.productName")
    SalesReturnLineResponse toResponse(SalesReturnLine entity);
}
