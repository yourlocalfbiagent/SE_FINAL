package com.app.se_final_sales.service.impl;

import com.app.se_final_sales.dto.ApprovalRequestDTO;
import com.app.se_final_sales.dto.ApprovalResponse;
import com.app.se_final_sales.dto.ApprovalReviewDTO;
import com.app.se_final_sales.entity.ApprovalRequest;
import com.app.se_final_sales.entity.SalesInvoice;
import com.app.se_final_sales.entity.User;
import com.app.se_final_sales.exception.ResourceNotFoundException;
import com.app.se_final_sales.mapper.ApprovalRequestMapper;
import com.app.se_final_sales.repository.ApprovalRequestRepository;
import com.app.se_final_sales.repository.SalesInvoiceRepository;
import com.app.se_final_sales.repository.UserRepository;
import com.app.se_final_sales.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final SalesInvoiceRepository salesInvoiceRepository;
    private final UserRepository userRepository;
    private final ApprovalRequestMapper approvalRequestMapper;

    @Override
    public ApprovalResponse submitForApproval(ApprovalRequestDTO request) {
        SalesInvoice invoice = salesInvoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " + request.getInvoiceId()));

        User requester = userRepository.findById(request.getRequestedById())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getRequestedById()));

        ApprovalRequest approval = approvalRequestMapper.toEntity(request);
        approval.setInvoice(invoice);
        approval.setRequestedBy(requester);
        approval.setStatus("PENDING");
        approval.setRequestedAt(LocalDateTime.now());

        return approvalRequestMapper.toResponse(approvalRequestRepository.save(approval));
    }

    @Override
    public ApprovalResponse reviewApproval(Long id, ApprovalReviewDTO review) {
        ApprovalRequest approval = approvalRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approval Request not found with ID: " + id));

        User reviewer = userRepository.findById(review.getReviewedById())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + review.getReviewedById()));

        approval.setReviewedBy(reviewer);
        approval.setStatus(review.getStatus());
        approval.setComments(review.getComments());
        approval.setReviewedAt(LocalDateTime.now());

        return approvalRequestMapper.toResponse(approvalRequestRepository.save(approval));
    }

    @Override
    public ApprovalResponse getApprovalById(Long id) {
        return approvalRequestRepository.findById(id)
                .map(approvalRequestMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Approval Request not found with ID: " + id));
    }

    @Override
    public List<ApprovalResponse> getApprovalsByInvoiceId(Long invoiceId) {
        return approvalRequestRepository.findAll().stream()
                .filter(a -> a.getInvoice().getInvoiceId().equals(invoiceId))
                .map(approvalRequestMapper::toResponse)
                .collect(Collectors.toList());
    }
}
