package com.app.se_final_sales.mapper;

import com.app.se_final_sales.dto.ApprovalRequestDTO;
import com.app.se_final_sales.dto.ApprovalResponse;
import com.app.se_final_sales.entity.ApprovalRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ApprovalRequestMapper {

    @Mapping(target = "invoice.invoiceId", source = "invoiceId")
    @Mapping(target = "requestedBy.userId", source = "requestedById")
    ApprovalRequest toEntity(ApprovalRequestDTO request);

    @Mapping(target = "invoiceId", source = "invoice.invoiceId")
    @Mapping(target = "invoiceNumber", source = "invoice.invoiceNumber")
    @Mapping(target = "requestedById", source = "requestedBy.userId")
    @Mapping(target = "requestedByEmail", source = "requestedBy.email")
    @Mapping(target = "reviewedById", source = "reviewedBy.userId")
    @Mapping(target = "reviewedByEmail", source = "reviewedBy.email")
    ApprovalResponse toResponse(ApprovalRequest entity);
}
