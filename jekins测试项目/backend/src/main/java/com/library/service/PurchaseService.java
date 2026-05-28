package com.library.service;

import com.library.common.PageResult;
import com.library.dto.ApprovalActionRequest;
import com.library.dto.PurchaseOrderRequest;
import com.library.entity.ApprovalRecord;
import com.library.entity.PurchaseOrder;

import java.util.List;

public interface PurchaseService {
    PageResult<PurchaseOrder> getPage(int page, int size, String status, Long deptId);
    PurchaseOrder getById(Long id);
    PurchaseOrder create(PurchaseOrderRequest request, Long applicantId);
    PurchaseOrder update(PurchaseOrderRequest request);
    void delete(Long id);
    PurchaseOrder submit(Long id);
    PurchaseOrder approve(Long id, ApprovalActionRequest request, Long approverId);
    void quickInstock(Long id);
    List<ApprovalRecord> getApprovalRecords(Long orderId);
}
