package com.app.se_final_sales.mapper;

import com.app.se_final_sales.dto.PaymentRequest;
import com.app.se_final_sales.dto.PaymentResponse;
import com.app.se_final_sales.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(target = "invoice.invoiceId", source = "invoiceId")
    Payment toEntity(PaymentRequest request);

    @Mapping(target = "invoiceId", source = "invoice.invoiceId")
    @Mapping(target = "invoiceNumber", source = "invoice.invoiceNumber")
    PaymentResponse toResponse(Payment entity);
}
